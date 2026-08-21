package com.example.meter.ui

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.util.Log
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.meter.camera.LuminanceAnalysisResult
import com.example.meter.camera.LuminanceAnalyzer
import com.example.meter.model.CameraLensType
import com.example.meter.model.ColorFilterMode
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.pow

@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    targetCameraId: String? = null,
    targetCameraLens: CameraLensType = CameraLensType.MAIN_WIDE,
    spotNormX: Float = 0.5f,
    spotNormY: Float = 0.5f,
    frameNormLeft: Float = 0.05f,
    frameNormTop: Float = 0.05f,
    frameNormRight: Float = 0.95f,
    frameNormBottom: Float = 0.95f,
    calibrationOffset: Double = 0.0,
    isFalseColorEnabled: Boolean = false,
    isTorchOn: Boolean = false,
    colorFilter: ColorFilterMode = ColorFilterMode.NONE,
    simulatedExposureShiftStops: Double = 0.0,
    zoomFactor: Float = 1.0f,
    focusTriggerPoint: Pair<Float, Float>? = null,
    onDistanceResult: (distanceMeters: Float?, isInfinity: Boolean, isLowContrast: Boolean) -> Unit = { _, _, _ -> },
    onZoomStateChanged: (actualZoom: Float, minZoom: Float, maxZoom: Float) -> Unit = { _, _, _ -> },
    onLuminanceResult: (LuminanceAnalysisResult) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // Dedicated high-performance luminance analyzer instance
    val analyzer = remember {
        LuminanceAnalyzer { result ->
            onLuminanceResult(result)
        }
    }

    // Keep analyzer parameters synchronously up-to-date with Compose state
    LaunchedEffect(
        spotNormX, spotNormY,
        frameNormLeft, frameNormTop, frameNormRight, frameNormBottom,
        calibrationOffset, isFalseColorEnabled
    ) {
        analyzer.spotNormX = spotNormX
        analyzer.spotNormY = spotNormY
        analyzer.frameNormLeft = frameNormLeft
        analyzer.frameNormTop = frameNormTop
        analyzer.frameNormRight = frameNormRight
        analyzer.frameNormBottom = frameNormBottom
        analyzer.userCalibrationOffset = calibrationOffset
        analyzer.isFalseColorEnabled = isFalseColorEnabled
    }

    // Bind or switch physical camera when targetCameraId / targetCameraLens / previewView is ready
    LaunchedEffect(targetCameraId, targetCameraLens, previewViewRef, cameraProviderRef) {
        val previewView = previewViewRef ?: return@LaunchedEffect
        val provider = cameraProviderRef ?: return@LaunchedEffect

        try {
            provider.unbindAll()

            val diopterWindow = java.util.Collections.synchronizedList(mutableListOf<Float>())
            var lastConfirmedDistance: Float? = null

            val captureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    result.get(CaptureResult.SENSOR_EXPOSURE_TIME)?.let { analyzer.sensorExposureTimeNs = it }
                    result.get(CaptureResult.SENSOR_SENSITIVITY)?.let { analyzer.sensorIso = it }
                    result.get(CaptureResult.LENS_APERTURE)?.let { analyzer.lensAperture = it }

                    // Read Lens Focus Distance in Diopters (1/meters) and AF State
                    val focusDistDiopters = result.get(CaptureResult.LENS_FOCUS_DISTANCE)
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)

                    if (focusDistDiopters != null) {
                        val isScanning = (afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN ||
                                          afState == CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN)
                        val isFocused = (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                                         afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED)
                        val isNotFocused = (afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED ||
                                            afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED)

                        if (isScanning) {
                            // Active search in progress: keep reading stable without flashing
                            return
                        }

                        if (focusDistDiopters > 0.025f) { // Finite distance (< 40 meters)
                            synchronized(diopterWindow) {
                                diopterWindow.add(focusDistDiopters)
                                if (diopterWindow.size > 7) {
                                    diopterWindow.removeAt(0)
                                }
                                val sorted = diopterWindow.sorted()
                                val medianDiopter = sorted[sorted.size / 2]
                                val distM = (1.0f / medianDiopter).coerceIn(0.12f, 45.0f)
                                lastConfirmedDistance = distM
                                onDistanceResult(distM, false, isNotFocused)
                            }
                        } else {
                            // Lens reporting <= 0.025 diopters (> 40m / Infinity)
                            if (isFocused) {
                                // True optical infinity confirmed by AF lock
                                synchronized(diopterWindow) {
                                    diopterWindow.clear()
                                }
                                lastConfirmedDistance = null
                                onDistanceResult(null, true, false)
                            } else if (isNotFocused) {
                                // Low-contrast search failure that defaulted to 0 diopter: DO NOT claim infinity!
                                onDistanceResult(lastConfirmedDistance, false, true)
                            } else {
                                // Passive / inactive: only report infinity if several consecutive frames agree
                                synchronized(diopterWindow) {
                                    diopterWindow.add(focusDistDiopters)
                                    if (diopterWindow.size > 7) diopterWindow.removeAt(0)
                                    if (diopterWindow.size >= 5 && diopterWindow.all { it < 0.025f }) {
                                        lastConfirmedDistance = null
                                        onDistanceResult(null, true, false)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val previewBuilder = Preview.Builder()
            Camera2Interop.Extender(previewBuilder).setSessionCaptureCallback(captureCallback)
            val preview = previewBuilder.build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysisBuilder = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)

            Camera2Interop.Extender(analysisBuilder).setSessionCaptureCallback(captureCallback)
            val imageAnalysis = analysisBuilder.build().also {
                it.setAnalyzer(cameraExecutor, analyzer)
            }

            // Attempt to bind specifically to target camera ID if available, otherwise DEFAULT_BACK_CAMERA
            val selector = if (targetCameraId != null) {
                try {
                    CameraSelector.Builder()
                        .addCameraFilter { cameraInfos ->
                            val matched = cameraInfos.filter { info ->
                                try {
                                    val c2 = Camera2CameraInfo.from(info)
                                    c2.cameraId == targetCameraId
                                } catch (_: Exception) {
                                    false
                                }
                            }
                            if (matched.isNotEmpty()) matched else cameraInfos
                        }
                        .build()
                } catch (_: Exception) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            val boundCam = try {
                provider.bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis)
            } catch (e: Exception) {
                Log.w("CameraPreviewView", "Specific camera ID bind failed, falling back to default back camera", e)
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
            }

            cameraInstance = boundCam

            // Set physical zoom factor directly based on calculated optical FOV ratio
            val zoomState = boundCam.cameraInfo.zoomState.value
            val maxZ = zoomState?.maxZoomRatio ?: 10.0f
            val minZ = zoomState?.minZoomRatio ?: 0.5f
            val targetZ = zoomFactor.coerceIn(minZ, maxZ)
            boundCam.cameraControl.setZoomRatio(targetZ)
            onZoomStateChanged(targetZ, minZ, maxZ)

            applyColorFilterToView(previewView, colorFilter, simulatedExposureShiftStops)
        } catch (exc: Exception) {
            Log.e("CameraPreviewView", "Camera binding failed", exc)
        }
    }

    // Torch control
    LaunchedEffect(isTorchOn, cameraInstance) {
        cameraInstance?.let { cam ->
            try {
                if (cam.cameraInfo.hasFlashUnit()) {
                    cam.cameraControl.enableTorch(isTorchOn)
                }
            } catch (e: Exception) {
                Log.w("CameraPreviewView", "Torch error: ${e.message}")
            }
        }
    }

    // Dynamic Zoom Control for Digital Crop and Lens Focal Adjustments
    LaunchedEffect(zoomFactor, cameraInstance) {
        val cam = cameraInstance ?: return@LaunchedEffect
        try {
            val zoomState = cam.cameraInfo.zoomState.value
            val maxZ = zoomState?.maxZoomRatio ?: 10.0f
            val minZ = zoomState?.minZoomRatio ?: 0.5f
            val targetZ = zoomFactor.coerceIn(minZ, maxZ)
            cam.cameraControl.setZoomRatio(targetZ)
            onZoomStateChanged(targetZ, minZ, maxZ)
        } catch (e: Exception) {
            Log.w("CameraPreviewView", "Failed to adjust zoom ratio: ${e.message}")
        }
    }

    // Dynamic Autofocus Trigger for Distance / Rangefinder measurement
    LaunchedEffect(focusTriggerPoint, cameraInstance, previewViewRef) {
        val point = focusTriggerPoint ?: return@LaunchedEffect
        val cam = cameraInstance ?: return@LaunchedEffect
        val view = previewViewRef ?: return@LaunchedEffect
        try {
            val factory = view.meteringPointFactory
            // Multi-point adaptive AF: Main target + cross pattern neighbors to capture local edges on low-contrast objects
            val pMain = factory.createPoint(point.first * view.width, point.second * view.height, 0.22f)
            val pLeft = factory.createPoint(((point.first - 0.04f).coerceIn(0.05f, 0.95f)) * view.width, point.second * view.height, 0.15f)
            val pRight = factory.createPoint(((point.first + 0.04f).coerceIn(0.05f, 0.95f)) * view.width, point.second * view.height, 0.15f)
            val pTop = factory.createPoint(point.first * view.width, ((point.second - 0.04f).coerceIn(0.05f, 0.95f)) * view.height, 0.15f)
            val pBottom = factory.createPoint(point.first * view.width, ((point.second + 0.04f).coerceIn(0.05f, 0.95f)) * view.height, 0.15f)

            val action = androidx.camera.core.FocusMeteringAction.Builder(
                pMain,
                androidx.camera.core.FocusMeteringAction.FLAG_AF or androidx.camera.core.FocusMeteringAction.FLAG_AE
            )
            .addPoint(pLeft, androidx.camera.core.FocusMeteringAction.FLAG_AF)
            .addPoint(pRight, androidx.camera.core.FocusMeteringAction.FLAG_AF)
            .addPoint(pTop, androidx.camera.core.FocusMeteringAction.FLAG_AF)
            .addPoint(pBottom, androidx.camera.core.FocusMeteringAction.FLAG_AF)
            .setAutoCancelDuration(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()

            cam.cameraControl.startFocusAndMetering(action)
        } catch (e: Exception) {
            Log.w("CameraPreviewView", "Focus and metering action error: ${e.message}")
        }
    }

    // Apply color filters & real-time exposure preview brightness simulation
    LaunchedEffect(colorFilter, simulatedExposureShiftStops, previewViewRef) {
        val view = previewViewRef ?: return@LaunchedEffect
        applyColorFilterToView(view, colorFilter, simulatedExposureShiftStops)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                previewViewRef = this

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        cameraProviderRef = cameraProviderFuture.get()
                    } catch (e: Exception) {
                        Log.e("CameraPreviewView", "Error initializing camera provider", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

/**
 * Applies color matrix shaders and dynamic simulated exposure shifts to the Camera Viewfinder
 */
private fun applyColorFilterToView(
    previewView: PreviewView,
    mode: ColorFilterMode,
    exposureShiftStops: Double
) {
    // 1. Base Exposure Shift (brightness scale = 2^(stops))
    val brightnessMultiplier = 2.0.pow(exposureShiftStops.coerceIn(-6.0, 6.0)).toFloat()

    val exposureMatrix = ColorMatrix().apply {
        setScale(brightnessMultiplier, brightnessMultiplier, brightnessMultiplier, 1.0f)
    }

    val finalMatrix = ColorMatrix()

    when (mode) {
        ColorFilterMode.NONE -> {
            finalMatrix.set(exposureMatrix)
        }
        ColorFilterMode.BW_STANDARD -> {
            val bwMatrix = ColorMatrix().apply { setSaturation(0.0f) }
            finalMatrix.setConcat(exposureMatrix, bwMatrix)
        }
        ColorFilterMode.BW_YELLOW -> {
            val bw = ColorMatrix().apply { setSaturation(0.0f) }
            val yellowTint = ColorMatrix(floatArrayOf(
                1.15f * brightnessMultiplier, 0f, 0f, 0f, 0f,
                0f, 1.10f * brightnessMultiplier, 0f, 0f, 0f,
                0f, 0f, 0.70f * brightnessMultiplier, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            finalMatrix.setConcat(yellowTint, bw)
        }
        ColorFilterMode.BW_ORANGE -> {
            val bw = ColorMatrix().apply { setSaturation(0.0f) }
            val orangeTint = ColorMatrix(floatArrayOf(
                1.30f * brightnessMultiplier, 0f, 0f, 0f, 0f,
                0f, 0.95f * brightnessMultiplier, 0f, 0f, 0f,
                0f, 0f, 0.50f * brightnessMultiplier, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            finalMatrix.setConcat(orangeTint, bw)
        }
        ColorFilterMode.BW_RED -> {
            val bw = ColorMatrix().apply { setSaturation(0.0f) }
            val redTint = ColorMatrix(floatArrayOf(
                1.60f * brightnessMultiplier, 0f, 0f, 0f, 0f,
                0f, 0.70f * brightnessMultiplier, 0f, 0f, 0f,
                0f, 0f, 0.35f * brightnessMultiplier, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            finalMatrix.setConcat(redTint, bw)
        }
        ColorFilterMode.BW_GREEN -> {
            val bw = ColorMatrix().apply { setSaturation(0.0f) }
            val greenTint = ColorMatrix(floatArrayOf(
                0.75f * brightnessMultiplier, 0f, 0f, 0f, 0f,
                0f, 1.40f * brightnessMultiplier, 0f, 0f, 0f,
                0f, 0f, 0.75f * brightnessMultiplier, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            finalMatrix.setConcat(greenTint, bw)
        }
    }

    val filter = ColorMatrixColorFilter(finalMatrix)
    val paint = android.graphics.Paint().apply { colorFilter = filter }
    previewView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, paint)
}
