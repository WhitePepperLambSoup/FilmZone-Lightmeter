package com.example.meter.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build
import android.util.Log
import com.example.meter.model.CameraLensType
import com.example.meter.model.CameraOpticsInfo
import com.example.meter.model.PhysicalLensInfo
import java.util.Locale
import kotlin.math.atan
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object CameraOpticsDetector {

    private const val TAG = "CameraOpticsDetector"

    /**
     * Inspects device cameras via Camera2 API and calculates true physical focal lengths,
     * sensor dimensions, and equivalent focal lengths in both Landscape and Portrait orientations.
     */
    fun detectBackCameraOptics(
        context: Context,
        manualLandscapeBaseFocalMm: Float? = null,
        targetCameraId: String? = null
    ): CameraOpticsInfo {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return createFallbackOptics(manualLandscapeBaseFocalMm)

        try {
            val cameraIds = cameraManager.cameraIdList
            val backCameras = mutableListOf<PhysicalLensInfo>()
            val processedIds = mutableSetOf<String>()

            fun processCameraCharacteristics(id: String, chars: CameraCharacteristics, isPhysicalSubCamera: Boolean) {
                if (processedIds.contains(id)) return
                processedIds.add(id)

                val facing = chars.get(CameraCharacteristics.LENS_FACING) ?: return
                if (facing != CameraCharacteristics.LENS_FACING_BACK) return

                val focalLengths = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                val sensorSize = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                val orientation = chars.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90

                if (focalLengths != null && focalLengths.isNotEmpty() && sensorSize != null) {
                    for (physFocal in focalLengths) {
                        val sW = max(sensorSize.width, sensorSize.height) // Sensor long side (mm)
                        val sH = min(sensorSize.width, sensorSize.height) // Sensor short side (mm)

                        // Full-frame 135 film gate: 36mm x 24mm
                        // Landscape 35mm equivalent: physFocal * (36.0 / sW)
                        val rawLandEquiv = if (sW > 0) physFocal * (36.0f / sW) else 24.0f
                        val landEquiv = if (manualLandscapeBaseFocalMm != null && !isPhysicalSubCamera && rawLandEquiv in 20f..35f) {
                            manualLandscapeBaseFocalMm
                        } else {
                            rawLandEquiv
                        }

                        // Portrait 35mm equivalent (when phone is held vertically, the short side sH is horizontal):
                        // physFocal * (36.0 / sH) = landEquiv * (sW / sH)
                        val aspect = if (sH > 0) sW / sH else 4f / 3f
                        val portEquiv = landEquiv * aspect

                        // Angular FOV in Portrait orientation (degrees)
                        val hfovPort = (2.0 * atan((sH / (2.0 * physFocal)).toDouble()) * (180.0 / Math.PI)).toFloat()
                        val vfovPort = (2.0 * atan((sW / (2.0 * physFocal)).toDouble()) * (180.0 / Math.PI)).toFloat()

                        val lensType = when {
                            landEquiv < 20f -> CameraLensType.ULTRA_WIDE
                            landEquiv in 20f..35f -> CameraLensType.MAIN_WIDE
                            landEquiv in 36f..65f -> CameraLensType.TELEPHOTO_2X
                            landEquiv in 66f..100f -> CameraLensType.TELEPHOTO_3X
                            else -> CameraLensType.TELEPHOTO_5X
                        }

                        val namePrefix = when (lensType) {
                            CameraLensType.ULTRA_WIDE -> "超广角"
                            CameraLensType.MAIN_WIDE -> "主摄"
                            CameraLensType.TELEPHOTO_2X -> "2x长焦"
                            CameraLensType.TELEPHOTO_3X -> "3x长焦"
                            CameraLensType.TELEPHOTO_5X -> "5x潜望长焦"
                        }

                        val info = PhysicalLensInfo(
                            cameraId = id,
                            facing = facing,
                            physicalFocalLengthMm = physFocal,
                            sensorWidthMm = sW,
                            sensorHeightMm = sH,
                            landscapeEquivFocalMm = landEquiv,
                            portraitEquivFocalMm = portEquiv,
                            lensType = lensType,
                            displayName = "$namePrefix ${String.format(Locale.US, "%.1f", physFocal)}mm (横${landEquiv.roundToInt()}mm / 竖${portEquiv.roundToInt()}mm)"
                        )
                        backCameras.add(info)
                    }
                }
            }

            for (id in cameraIds) {
                val chars = cameraManager.getCameraCharacteristics(id)
                processCameraCharacteristics(id, chars, false)

                // Check physical sub-cameras of logical multi-cameras (Android 9.0+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    try {
                        val physicalIds = chars.physicalCameraIds
                        for (pId in physicalIds) {
                            try {
                                val pChars = cameraManager.getCameraCharacteristics(pId)
                                processCameraCharacteristics(pId, pChars, true)
                            } catch (_: Exception) {}
                        }
                    } catch (_: Exception) {}
                }
            }

            // Deduplicate and sort lenses by focal length (UW -> Main -> Tele)
            val distinctLenses = backCameras.distinctBy { 
                "${it.cameraId}_${(it.physicalFocalLengthMm * 10).roundToInt()}" 
            }.sortedBy { it.landscapeEquivFocalMm }

            val targetLens = if (targetCameraId != null) {
                distinctLenses.find { it.cameraId == targetCameraId }
            } else {
                distinctLenses.find { it.lensType == CameraLensType.MAIN_WIDE } ?: distinctLenses.firstOrNull()
            } ?: distinctLenses.firstOrNull()

            return if (targetLens != null) {
                val hfov = (2.0 * atan((targetLens.sensorHeightMm / (2.0 * targetLens.physicalFocalLengthMm)).toDouble()) * (180.0 / Math.PI)).toFloat()
                val vfov = (2.0 * atan((targetLens.sensorWidthMm / (2.0 * targetLens.physicalFocalLengthMm)).toDouble()) * (180.0 / Math.PI)).toFloat()
                CameraOpticsInfo(
                    cameraId = targetLens.cameraId,
                    physicalFocalLengthMm = targetLens.physicalFocalLengthMm,
                    sensorWidthMm = targetLens.sensorWidthMm,
                    sensorHeightMm = targetLens.sensorHeightMm,
                    sensorOrientation = 90,
                    nativeSensorAspectRatio = targetLens.sensorWidthMm / targetLens.sensorHeightMm,
                    landscapeEquivFocalMm = targetLens.landscapeEquivFocalMm,
                    portraitEquivFocalMm = targetLens.portraitEquivFocalMm,
                    portraitHFOV = hfov,
                    portraitVFOV = vfov,
                    availableLenses = distinctLenses,
                    isAutoDetected = true
                )
            } else {
                createFallbackOptics(manualLandscapeBaseFocalMm)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exception reading Camera2 characteristics", e)
            return createFallbackOptics(manualLandscapeBaseFocalMm)
        }
    }

    fun createFallbackOptics(manualLandscapeBaseFocalMm: Float? = null): CameraOpticsInfo {
        val baseLand = manualLandscapeBaseFocalMm ?: 24.0f
        val portEquiv = baseLand * (4.0f / 3.0f) // 24 * 1.3333 = 32mm
        val fallbackLenses = listOf(
            PhysicalLensInfo("0", CameraCharacteristics.LENS_FACING_BACK, 2.2f, 6.4f, 4.8f, 14f, 18.6f, CameraLensType.ULTRA_WIDE, "超广角 0.6x (14mm / 18mm)"),
            PhysicalLensInfo("0", CameraCharacteristics.LENS_FACING_BACK, 4.4f, 6.4f, 4.8f, 24f, 32.0f, CameraLensType.MAIN_WIDE, "主摄 1.0x (24mm / 32mm)"),
            PhysicalLensInfo("0", CameraCharacteristics.LENS_FACING_BACK, 8.8f, 6.4f, 4.8f, 50f, 66.6f, CameraLensType.TELEPHOTO_2X, "长焦 2.0x (50mm / 66mm)"),
            PhysicalLensInfo("0", CameraCharacteristics.LENS_FACING_BACK, 13.2f, 6.4f, 4.8f, 75f, 100.0f, CameraLensType.TELEPHOTO_3X, "人像 3.0x (75mm / 100mm)")
        )
        return CameraOpticsInfo(
            cameraId = "0",
            physicalFocalLengthMm = 4.4f,
            sensorWidthMm = 6.4f,
            sensorHeightMm = 4.8f,
            sensorOrientation = 90,
            nativeSensorAspectRatio = 4f / 3f,
            landscapeEquivFocalMm = baseLand,
            portraitEquivFocalMm = portEquiv,
            portraitHFOV = 58.7f,
            portraitVFOV = 73.7f,
            availableLenses = fallbackLenses,
            isAutoDetected = false
        )
    }
}
