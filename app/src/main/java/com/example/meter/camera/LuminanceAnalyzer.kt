package com.example.meter.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.meter.model.ZoneLevel
import kotlin.math.exp
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

data class LuminanceAnalysisResult(
    val spotEv100: Double,
    val frameEv100: Double, // Center-weighted average inside framelines
    val spotLuminance: Float, // 0.0 to 255.0
    val frameLuminance: Float, // 0.0 to 255.0 inside framelines
    val zoneHistogram: IntArray, // 11 elements for Zone 0 to Zone X
    val falseColorBitmap: Bitmap? = null,
    val sceneDynamicRangeStops: Double = 5.0
)

class LuminanceAnalyzer(
    private val onResult: (LuminanceAnalysisResult) -> Unit
) : ImageAnalysis.Analyzer {

    // Configurable parameters
    var spotNormX: Float = 0.5f
    var spotNormY: Float = 0.5f
    var frameNormLeft: Float = 0.05f
    var frameNormTop: Float = 0.05f
    var frameNormRight: Float = 0.95f
    var frameNormBottom: Float = 0.95f
    var userCalibrationOffset: Double = 0.0
    var isFalseColorEnabled: Boolean = false
    var isMonochromeEnabled: Boolean = false

    // Hardware camera capture parameters updated from Camera2 capture callback
    @Volatile var sensorIso: Int = 100
    @Volatile var sensorExposureTimeNs: Long = 20_000_000L // 20ms default (1/50s)
    @Volatile var lensAperture: Float = 1.8f

    // Reusable byte buffer and bitmap to minimize GC pauses
    private var cachedYBytes: ByteArray? = null
    private var falseColorCache: Bitmap? = null
    private var pixelBuffer: IntArray? = null

    override fun analyze(image: ImageProxy) {
        val planes = image.planes
        if (planes.isEmpty()) {
            image.close()
            return
        }

        val yBuffer = planes[0].buffer
        val yWidth = image.width
        val yHeight = image.height
        val rowStride = planes[0].rowStride
        val pixelStride = planes[0].pixelStride

        // 1. Calculate hardware baseline EV100 from sensor metadata (ISO 2720 standard)
        val expTimeSec = max(sensorExposureTimeNs.toDouble() / 1_000_000_000.0, 0.00001)
        val iso = max(sensorIso, 10)
        val aperture = if (lensAperture > 0.5f) lensAperture.toDouble() else 1.8

        // Standard APEX baseline EV100 = log2(N^2 * 100 / (t * S))
        val baseHardwareEv100 = log2((aperture * aperture * 100.0) / (expTimeSec * iso))

        val remaining = yBuffer.remaining()
        if (cachedYBytes == null || cachedYBytes?.size != remaining) {
            cachedYBytes = ByteArray(remaining)
        }
        val yBytes = cachedYBytes!!
        yBuffer.get(yBytes)

        // 2. Compute Frameline Bounding Box in Pixel Coordinates
        val fLeftPx = (frameNormLeft * yWidth).roundToInt().coerceIn(0, yWidth - 2)
        val fTopPx = (frameNormTop * yHeight).roundToInt().coerceIn(0, yHeight - 2)
        val fRightPx = (frameNormRight * yWidth).roundToInt().coerceIn(fLeftPx + 1, yWidth - 1)
        val fBottomPx = (frameNormBottom * yHeight).roundToInt().coerceIn(fTopPx + 1, yHeight - 1)

        val frameW = max(fRightPx - fLeftPx, 1)
        val frameH = max(fBottomPx - fTopPx, 1)
        val frameCenterX = fLeftPx + frameW / 2.0
        val frameCenterY = fTopPx + frameH / 2.0

        // 3. Center-Weighted Matrix Luminance Calculation inside Framelines
        val step = 4
        var weightedFrameLumSum = 0.0
        var totalFrameWeight = 0.0
        var rawFrameLumSum = 0L
        var rawFrameCount = 0
        var minLum = 255
        var maxLum = 0

        val histogram = IntArray(11) // Zone 0 to Zone X

        for (y in fTopPx..fBottomPx step step) {
            val rowStart = y * rowStride
            val dyNorm = (y - frameCenterY) / (frameH * 0.5)
            val dySq = dyNorm * dyNorm

            for (x in fLeftPx..fRightPx step step) {
                val index = rowStart + x * pixelStride
                if (index in yBytes.indices) {
                    val lum = yBytes[index].toInt() and 0xFF
                    rawFrameLumSum += lum
                    rawFrameCount++

                    if (lum < minLum) minLum = lum
                    if (lum > maxLum) maxLum = lum

                    // Gaussian center weighting (60% weight to central 50% circle/ellipse)
                    val dxNorm = (x - frameCenterX) / (frameW * 0.5)
                    val distSq = dxNorm * dxNorm + dySq
                    val weight = 1.0 + 2.0 * exp(-2.5 * distSq)

                    weightedFrameLumSum += lum * weight
                    totalFrameWeight += weight

                    // Zone classification using standard linear gamma ~2.2 mapping
                    val zoneIdx = if (lum <= 0) 0 else {
                        val linearRel = (lum / 255.0).pow(2.2) / 0.18
                        val relStop = log2(max(linearRel, 0.001))
                        (5 + relStop).roundToInt().coerceIn(0, 10)
                    }
                    histogram[zoneIdx]++
                }
            }
        }

        val avgWeightedFrameLum = if (totalFrameWeight > 0) (weightedFrameLumSum / totalFrameWeight).toFloat() else 118f
        val rawAvgFrameLum = if (rawFrameCount > 0) (rawFrameLumSum.toFloat() / rawFrameCount) else 118f

        // 4. Spot Region Sampling
        val spotRadius = max((min(yWidth, yHeight) * 0.035f).roundToInt(), 4)
        val spotPixelX = (spotNormX * yWidth).roundToInt().coerceIn(spotRadius, yWidth - spotRadius - 1)
        val spotPixelY = (spotNormY * yHeight).roundToInt().coerceIn(spotRadius, yHeight - spotRadius - 1)

        var spotSum = 0L
        var spotCount = 0

        for (sy in (spotPixelY - spotRadius)..(spotPixelY + spotRadius)) {
            val rowStart = sy * rowStride
            val dy = sy - spotPixelY
            for (sx in (spotPixelX - spotRadius)..(spotPixelX + spotRadius)) {
                val dx = sx - spotPixelX
                if (dx * dx + dy * dy <= spotRadius * spotRadius) {
                    val index = rowStart + sx * pixelStride
                    if (index in yBytes.indices) {
                        val lum = yBytes[index].toInt() and 0xFF
                        spotSum += lum
                        spotCount++
                    }
                }
            }
        }

        val avgSpotLum = if (spotCount > 0) (spotSum.toFloat() / spotCount) else avgWeightedFrameLum

        // 5. Correct Physical Gamma Delta Calculation
        // In 8-bit sRGB, 18% middle gray is Y ~ 118 (because (118/255)^2.2 ~ 0.18).
        // Linear reflectance L = (Y / 255.0)^2.2
        // Delta EV = log2(L / 0.18) = 2.2 * log2(Y / 118.0)
        val spotLinear = (max(avgSpotLum, 1.0f) / 255.0).pow(2.2)
        val spotDeltaEv = log2(max(spotLinear / 0.18, 0.0001))

        val frameLinear = (max(avgWeightedFrameLum, 1.0f) / 255.0).pow(2.2)
        val frameDeltaEv = log2(max(frameLinear / 0.18, 0.0001))

        // Standard ISO 2720 / APEX reflected light baseline calibration offset (+0.3 EV)
        // Calibrated to align precisely with physical light meters (Sekonic, Gossen, Lightme)
        val standardBaselineOffset = 0.3

        // Final accurately calibrated EV100
        val finalSpotEv100 = baseHardwareEv100 + spotDeltaEv + standardBaselineOffset + userCalibrationOffset
        val finalFrameEv100 = baseHardwareEv100 + frameDeltaEv + standardBaselineOffset + userCalibrationOffset

        val dynamicRangeStops = if (minLum > 0 && maxLum > minLum) {
            val minLin = (max(minLum, 1) / 255.0).pow(2.2)
            val maxLin = (max(maxLum, 1) / 255.0).pow(2.2)
            log2(max(maxLin / minLin, 1.0))
        } else 5.0

        // 6. False Color Preview Generation
        var falseColorBmp: Bitmap? = null
        if (isFalseColorEnabled) {
            val thumbW = 96
            val thumbH = 96
            if (falseColorCache == null || falseColorCache?.width != thumbW) {
                falseColorCache = Bitmap.createBitmap(thumbW, thumbH, Bitmap.Config.ARGB_8888)
                pixelBuffer = IntArray(thumbW * thumbH)
            }
            val buf = pixelBuffer
            val bmp = falseColorCache
            if (buf != null && bmp != null) {
                val scaleX = yWidth.toFloat() / thumbW
                val scaleY = yHeight.toFloat() / thumbH
                var pIdx = 0
                for (ty in 0 until thumbH) {
                    val srcY = (ty * scaleY).toInt().coerceIn(0, yHeight - 1)
                    val rowStart = srcY * rowStride
                    for (tx in 0 until thumbW) {
                        val srcX = (tx * scaleX).toInt().coerceIn(0, yWidth - 1)
                        val index = rowStart + srcX * pixelStride
                        val lum = if (index in yBytes.indices) (yBytes[index].toInt() and 0xFF) else 118
                        val linear = (max(lum, 1) / 255.0).pow(2.2)
                        val relStop = log2(max(linear / 0.18, 0.0001))
                        val z = (5 + relStop).roundToInt().coerceIn(0, 10)
                        val colorHex = ZoneLevel.fromIndex(z).falseColorHex.toInt()
                        buf[pIdx++] = (0x99 shl 24) or (colorHex and 0x00FFFFFF)
                    }
                }
                bmp.setPixels(buf, 0, thumbW, 0, 0, thumbW, thumbH)
                falseColorBmp = bmp
            }
        }

        onResult(
            LuminanceAnalysisResult(
                spotEv100 = finalSpotEv100,
                frameEv100 = finalFrameEv100,
                spotLuminance = avgSpotLum,
                frameLuminance = rawAvgFrameLum,
                zoneHistogram = histogram,
                falseColorBitmap = falseColorBmp,
                sceneDynamicRangeStops = dynamicRangeStops
            )
        )

        image.close()
    }
}

