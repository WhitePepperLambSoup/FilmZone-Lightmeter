package com.example.meter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meter.model.AppLanguage
import com.example.meter.model.AppStrings
import com.example.meter.model.CameraLensType
import com.example.meter.model.ColorFilterMode
import com.example.meter.model.CompositionGridStyle
import com.example.meter.model.FilmAspectRatio
import com.example.meter.model.SpotMarker
import com.example.meter.model.ZoneLevel
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterBorder
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterEmerald
import com.example.ui.theme.MeterRed
import com.example.ui.theme.MeterTextMuted
import com.example.ui.theme.MeterTextPrimary
import com.example.ui.theme.MeterTextSecondary
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ViewfinderOverlay(
    modifier: Modifier = Modifier,
    spotNormX: Float,
    spotNormY: Float,
    aspectRatio: FilmAspectRatio,
    focalLengthMm: Int,
    isDigitalCropZoomEnabled: Boolean = true,
    cameraLens: CameraLensType = CameraLensType.MAIN_WIDE,
    isExperimentalMultiCameraEnabled: Boolean = false,
    opticsInfo: com.example.meter.model.CameraOpticsInfo = com.example.meter.camera.CameraOpticsDetector.createFallbackOptics(),
    hardwareZoomRatio: Float = 1.0f,
    hardwareMinZoomRatio: Float = 0.5f,
    gridStyle: CompositionGridStyle,
    colorFilter: ColorFilterMode,
    targetZone: ZoneLevel,
    liveEv100: Double,
    effectiveAperture: Double = 2.8,
    isHold: Boolean,
    isFalseColor: Boolean,
    isSpotMeteringActive: Boolean,
    isZoneSystemEnabled: Boolean = false,
    isDistanceModeEnabled: Boolean = false,
    isDistanceSpotLinkEnabled: Boolean = false,
    rangefinderEngineMode: com.example.meter.model.RangefinderEngineMode = com.example.meter.model.RangefinderEngineMode.AF_OPTICAL,
    measuredDistanceMeters: Float? = null,
    isDistanceInfinity: Boolean = false,
    isDistanceLowContrast: Boolean = false,
    isDistanceMeasuring: Boolean = false,
    distanceNormX: Float = 0.5f,
    distanceNormY: Float = 0.5f,
    inclinometerHorizontalDistMeters: Float? = null,
    inclinometerDirectDistMeters: Float? = null,
    inclinometerPitchDeg: Float = 0f,
    inclinometerRollDeg: Float = 0f,
    isPhoneLevel: Boolean = false,
    multiSpots: List<SpotMarker>,
    appLanguage: AppLanguage = AppLanguage.DEFAULT,
    onSpotMoved: (normX: Float, normY: Float) -> Unit,
    onTapToSetSpot: (normX: Float, normY: Float) -> Unit,
    onTriggerDistance: (normX: Float, normY: Float) -> Unit = { _, _ -> },
    onToggleDistanceSpotLink: () -> Unit = {},
    onResetToAverage: () -> Unit,
    onOpenFramelines: () -> Unit,
    onOpenSecondaryMenu: () -> Unit,
    onToggleZoneSystem: () -> Unit,
    onSwitchToUltraWide: () -> Unit = {},
    onFramelineBoundsChanged: (left: Float, top: Float, right: Float, bottom: Float) -> Unit
) {
    val s = AppStrings.get(appLanguage)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isDistanceModeEnabled, isDistanceSpotLinkEnabled) {
                detectTapGestures { offset ->
                    val normX = (offset.x / size.width).coerceIn(0.05f, 0.95f)
                    val normY = (offset.y / size.height).coerceIn(0.05f, 0.95f)
                    if (isDistanceModeEnabled) {
                        onTriggerDistance(normX, normY)
                        if (isDistanceSpotLinkEnabled) {
                            onTapToSetSpot(normX, normY)
                        }
                    } else {
                        onTapToSetSpot(normX, normY)
                    }
                }
            }
            .pointerInput(isDistanceModeEnabled) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val normX = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                    val normY = (change.position.y / size.height).coerceIn(0.05f, 0.95f)
                    if (isDistanceModeEnabled) {
                        onTriggerDistance(normX, normY)
                        if (isDistanceSpotLinkEnabled) {
                            onSpotMoved(normX, normY)
                        }
                    } else {
                        onSpotMoved(normX, normY)
                    }
                }
            }
            .testTag("viewfinder_canvas_area")
    ) {
        val totalWidth = constraints.maxWidth.toFloat()
        val totalHeight = constraints.maxHeight.toFloat()

        // 1. Calculate Base Film Format Aspect Ratio Bounds
        val targetRatio = aspectRatio.ratio
        val viewRatio = if (totalHeight > 0) totalWidth / totalHeight else 1f

        val outerFilmRect = remember(totalWidth, totalHeight, targetRatio, viewRatio) {
            val frameW: Float
            val frameH: Float
            if (viewRatio > targetRatio) {
                frameH = totalHeight * 0.92f
                frameW = frameH * targetRatio
            } else {
                frameW = totalWidth * 0.94f
                frameH = frameW / targetRatio
            }
            val left = (totalWidth - frameW) / 2f
            val top = (totalHeight - frameH) / 2f
            Rect(left, top, left + frameW, top + frameH)
        }

        // 2. Real Optical Calibration Calculations
        // Target 135-equivalent focal length based on physical film gate width
        val focal135EquivWidth = focalLengthMm.toFloat() * (36.0f / aspectRatio.gateWidthMm)
        // Base Portrait 35mm equivalent focal length of phone's main 1.0x sensor
        val effectivePortraitBaseFocal = if (opticsInfo.portraitEquivFocalMm > 0f) opticsInfo.portraitEquivFocalMm else 32.0f
        // Theoretical required zoom ratio referenced to phone's 1.0x portrait sensor
        val targetZoomRatio = focal135EquivWidth / effectivePortraitBaseFocal
        val minPossibleZoom = hardwareMinZoomRatio.coerceAtLeast(0.3f)
        val isFocalTooWide = targetZoomRatio < (minPossibleZoom * 0.98f)

        // When digital crop zoom is enabled:
        // - If within zoom range, hardware zoom fills the outer film gate perfectly (scale = 1.0)
        // - If wider than hardware min zoom, camera clamps to min zoom and framing box shrinks accurately!
        val framelineScale = if (isDigitalCropZoomEnabled) {
            if (targetZoomRatio < minPossibleZoom) {
                (targetZoomRatio / minPossibleZoom).coerceIn(0.15f, 1.0f)
            } else {
                1.0f
            }
        } else {
            targetZoomRatio.coerceIn(0.12f, 1.0f)
        }

        val calibratedFramelineRect = remember(outerFilmRect, framelineScale) {
            val fW = outerFilmRect.width * framelineScale
            val fH = outerFilmRect.height * framelineScale
            val left = outerFilmRect.left + (outerFilmRect.width - fW) / 2f
            val top = outerFilmRect.top + (outerFilmRect.height - fH) / 2f
            Rect(left, top, left + fW, top + fH)
        }

        // Notify parent of normalized frameline boundaries for accurate matrix metering
        LaunchedEffect(calibratedFramelineRect, totalWidth, totalHeight) {
            if (totalWidth > 0 && totalHeight > 0) {
                val nLeft = (calibratedFramelineRect.left / totalWidth).coerceIn(0f, 1f)
                val nTop = (calibratedFramelineRect.top / totalHeight).coerceIn(0f, 1f)
                val nRight = (calibratedFramelineRect.right / totalWidth).coerceIn(0f, 1f)
                val nBottom = (calibratedFramelineRect.bottom / totalHeight).coerceIn(0f, 1f)
                onFramelineBoundsChanged(nLeft, nTop, nRight, nBottom)
            }
        }

        // Canvas for Masks, Framelines & Composition Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maskColor = Color(0xEE0B0D0E)

            // Letterbox masks outside outerFilmRect
            if (outerFilmRect.top > 0) {
                drawRect(color = maskColor, topLeft = Offset(0f, 0f), size = Size(totalWidth, outerFilmRect.top))
            }
            if (outerFilmRect.bottom < totalHeight) {
                drawRect(color = maskColor, topLeft = Offset(0f, outerFilmRect.bottom), size = Size(totalWidth, totalHeight - outerFilmRect.bottom))
            }
            if (outerFilmRect.left > 0) {
                drawRect(color = maskColor, topLeft = Offset(0f, outerFilmRect.top), size = Size(outerFilmRect.left, outerFilmRect.height))
            }
            if (outerFilmRect.right < totalWidth) {
                drawRect(color = maskColor, topLeft = Offset(outerFilmRect.right, outerFilmRect.top), size = Size(totalWidth - outerFilmRect.right, outerFilmRect.height))
            }

            // Translucent Framing Mask outside calibrated framelines if scaled
            if (framelineScale < 0.99f) {
                val frameMaskColor = Color(0x77000000)
                // Top
                if (calibratedFramelineRect.top > outerFilmRect.top) {
                    drawRect(color = frameMaskColor, topLeft = outerFilmRect.topLeft, size = Size(outerFilmRect.width, calibratedFramelineRect.top - outerFilmRect.top))
                }
                // Bottom
                if (calibratedFramelineRect.bottom < outerFilmRect.bottom) {
                    drawRect(color = frameMaskColor, topLeft = Offset(outerFilmRect.left, calibratedFramelineRect.bottom), size = Size(outerFilmRect.width, outerFilmRect.bottom - calibratedFramelineRect.bottom))
                }
                // Left
                if (calibratedFramelineRect.left > outerFilmRect.left) {
                    drawRect(color = frameMaskColor, topLeft = Offset(outerFilmRect.left, calibratedFramelineRect.top), size = Size(calibratedFramelineRect.left - outerFilmRect.left, calibratedFramelineRect.height))
                }
                // Right
                if (calibratedFramelineRect.right < outerFilmRect.right) {
                    drawRect(color = frameMaskColor, topLeft = Offset(calibratedFramelineRect.right, calibratedFramelineRect.top), size = Size(outerFilmRect.right - calibratedFramelineRect.right, calibratedFramelineRect.height))
                }
            }

            // Outer Film Border (Subtle)
            drawRect(
                color = Color(0x44FFFFFF),
                topLeft = outerFilmRect.topLeft,
                size = outerFilmRect.size,
                style = Stroke(width = 1.dp.toPx())
            )

            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)

            // Calibrated Optical Framelines (Leica M illuminated bright frameline style)
            val framelineColor = if (isFocalTooWide) MeterAmber else Color(0xEEFFFFFF)
            val cornerLen = min(calibratedFramelineRect.width, calibratedFramelineRect.height) * 0.10f
            val bracketStroke = 2.dp.toPx()

            // Top-Left corner
            drawLine(framelineColor, Offset(calibratedFramelineRect.left, calibratedFramelineRect.top), Offset(calibratedFramelineRect.left + cornerLen, calibratedFramelineRect.top), bracketStroke)
            drawLine(framelineColor, Offset(calibratedFramelineRect.left, calibratedFramelineRect.top), Offset(calibratedFramelineRect.left, calibratedFramelineRect.top + cornerLen), bracketStroke)
            // Top-Right corner
            drawLine(framelineColor, Offset(calibratedFramelineRect.right, calibratedFramelineRect.top), Offset(calibratedFramelineRect.right - cornerLen, calibratedFramelineRect.top), bracketStroke)
            drawLine(framelineColor, Offset(calibratedFramelineRect.right, calibratedFramelineRect.top), Offset(calibratedFramelineRect.right, calibratedFramelineRect.top + cornerLen), bracketStroke)
            // Bottom-Left corner
            drawLine(framelineColor, Offset(calibratedFramelineRect.left, calibratedFramelineRect.bottom), Offset(calibratedFramelineRect.left + cornerLen, calibratedFramelineRect.bottom), bracketStroke)
            drawLine(framelineColor, Offset(calibratedFramelineRect.left, calibratedFramelineRect.bottom), Offset(calibratedFramelineRect.left, calibratedFramelineRect.bottom - cornerLen), bracketStroke)
            // Bottom-Right corner
            drawLine(framelineColor, Offset(calibratedFramelineRect.right, calibratedFramelineRect.bottom), Offset(calibratedFramelineRect.right - cornerLen, calibratedFramelineRect.bottom), bracketStroke)
            drawLine(framelineColor, Offset(calibratedFramelineRect.right, calibratedFramelineRect.bottom), Offset(calibratedFramelineRect.right, calibratedFramelineRect.bottom - cornerLen), bracketStroke)

            // Inner Frameline delicate bounding rectangle
            drawRect(
                color = framelineColor.copy(alpha = 0.35f),
                topLeft = calibratedFramelineRect.topLeft,
                size = calibratedFramelineRect.size,
                style = Stroke(width = 0.8.dp.toPx(), pathEffect = if (isFocalTooWide) dashEffect else null)
            )

            // Composition Grid inside Calibrated Frameline Rect
            val gridColor = Color(0x35FFFFFF)
            when (gridStyle) {
                CompositionGridStyle.RULE_OF_THIRDS -> {
                    val thirdW = calibratedFramelineRect.width / 3f
                    val thirdH = calibratedFramelineRect.height / 3f
                    drawLine(gridColor, Offset(calibratedFramelineRect.left + thirdW, calibratedFramelineRect.top), Offset(calibratedFramelineRect.left + thirdW, calibratedFramelineRect.bottom), 0.8.dp.toPx(), pathEffect = dashEffect)
                    drawLine(gridColor, Offset(calibratedFramelineRect.left + thirdW * 2, calibratedFramelineRect.top), Offset(calibratedFramelineRect.left + thirdW * 2, calibratedFramelineRect.bottom), 0.8.dp.toPx(), pathEffect = dashEffect)
                    drawLine(gridColor, Offset(calibratedFramelineRect.left, calibratedFramelineRect.top + thirdH), Offset(calibratedFramelineRect.right, calibratedFramelineRect.top + thirdH), 0.8.dp.toPx(), pathEffect = dashEffect)
                    drawLine(gridColor, Offset(calibratedFramelineRect.left, calibratedFramelineRect.top + thirdH * 2), Offset(calibratedFramelineRect.right, calibratedFramelineRect.top + thirdH * 2), 0.8.dp.toPx(), pathEffect = dashEffect)
                }
                CompositionGridStyle.GOLDEN_RATIO -> {
                    val phi1W = calibratedFramelineRect.width * 0.382f
                    val phi2W = calibratedFramelineRect.width * 0.618f
                    val phi1H = calibratedFramelineRect.height * 0.382f
                    val phi2H = calibratedFramelineRect.height * 0.618f
                    drawLine(gridColor, Offset(calibratedFramelineRect.left + phi1W, calibratedFramelineRect.top), Offset(calibratedFramelineRect.left + phi1W, calibratedFramelineRect.bottom), 0.8.dp.toPx(), pathEffect = dashEffect)
                    drawLine(gridColor, Offset(calibratedFramelineRect.left + phi2W, calibratedFramelineRect.top), Offset(calibratedFramelineRect.left + phi2W, calibratedFramelineRect.bottom), 0.8.dp.toPx(), pathEffect = dashEffect)
                    drawLine(gridColor, Offset(calibratedFramelineRect.left, calibratedFramelineRect.top + phi1H), Offset(calibratedFramelineRect.right, calibratedFramelineRect.top + phi1H), 0.8.dp.toPx(), pathEffect = dashEffect)
                    drawLine(gridColor, Offset(calibratedFramelineRect.left, calibratedFramelineRect.top + phi2H), Offset(calibratedFramelineRect.right, calibratedFramelineRect.top + phi2H), 0.8.dp.toPx(), pathEffect = dashEffect)
                }
                CompositionGridStyle.CENTER_CROSS -> {
                    val cx = calibratedFramelineRect.left + calibratedFramelineRect.width / 2f
                    val cy = calibratedFramelineRect.top + calibratedFramelineRect.height / 2f
                    val crossLen = min(calibratedFramelineRect.width, calibratedFramelineRect.height) * 0.18f
                    drawLine(Color(0x66FFFFFF), Offset(cx - crossLen, cy), Offset(cx + crossLen, cy), 1.dp.toPx())
                    drawLine(Color(0x66FFFFFF), Offset(cx, cy - crossLen), Offset(cx, cy + crossLen), 1.dp.toPx())
                }
                CompositionGridStyle.DIAGONAL -> {
                    drawLine(gridColor, Offset(calibratedFramelineRect.left, calibratedFramelineRect.top), Offset(calibratedFramelineRect.right, calibratedFramelineRect.bottom), 0.8.dp.toPx(), pathEffect = dashEffect)
                    drawLine(gridColor, Offset(calibratedFramelineRect.right, calibratedFramelineRect.top), Offset(calibratedFramelineRect.left, calibratedFramelineRect.bottom), 0.8.dp.toPx(), pathEffect = dashEffect)
                }
                CompositionGridStyle.NONE -> {}
            }

            // Center-weighted guide when Average Metering is active
            if (!isSpotMeteringActive) {
                val cx = calibratedFramelineRect.left + calibratedFramelineRect.width / 2f
                val cy = calibratedFramelineRect.top + calibratedFramelineRect.height / 2f
                val avgRadius = min(calibratedFramelineRect.width, calibratedFramelineRect.height) * 0.22f
                drawCircle(
                    color = Color(0x5500E5FF),
                    center = Offset(cx, cy),
                    radius = avgRadius,
                    style = Stroke(width = 1.dp.toPx(), pathEffect = dashEffect)
                )
            }

            // Multi-spot connection lines
            if (multiSpots.size >= 2) {
                for (i in 0 until multiSpots.size - 1) {
                    val p1 = Offset(multiSpots[i].normX * totalWidth, multiSpots[i].normY * totalHeight)
                    val p2 = Offset(multiSpots[i + 1].normX * totalWidth, multiSpots[i + 1].normY * totalHeight)
                    drawLine(color = Color(0xAA7DD3FC), start = p1, end = p2, strokeWidth = 1.2.dp.toPx(), pathEffect = dashEffect)
                }
            }
        }

        // Multi-Spot Saved Markers
        multiSpots.forEach { spot ->
            val spotPixelX = spot.normX * totalWidth
            val spotPixelY = spot.normY * totalHeight

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (spotPixelX - 16.dp.toPx()).roundToInt(),
                            (spotPixelY - 16.dp.toPx()).roundToInt()
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xCC1A1C1E), CircleShape)
                        .border(1.2.dp, MeterCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Z${spot.assignedZone.roman}",
                        color = MeterCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Spot Reticle Target (Active when Spot Metering is On)
        if (isSpotMeteringActive) {
            val activePixelX = spotNormX * totalWidth
            val activePixelY = spotNormY * totalHeight

            Canvas(
                modifier = Modifier
                    .size(68.dp)
                    .offset {
                        IntOffset(
                            (activePixelX - 34.dp.toPx()).roundToInt(),
                            (activePixelY - 34.dp.toPx()).roundToInt()
                        )
                    }
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val ringRadius = 18.dp.toPx()
                val reticleColor = if (isHold) MeterCyan else Color(0xEEFFFFFF)

                // Spot Ring
                drawCircle(color = reticleColor, radius = ringRadius, style = Stroke(width = 1.5.dp.toPx()))
                // Center Pin
                drawCircle(color = MeterRed, radius = 2.5.dp.toPx())

                // 4 axis crosshairs
                val tickLen = 5.dp.toPx()
                val tickGap = ringRadius + 2.dp.toPx()
                drawLine(reticleColor, Offset(center.x, center.y - tickGap), Offset(center.x, center.y - tickGap - tickLen), 1.2.dp.toPx())
                drawLine(reticleColor, Offset(center.x, center.y + tickGap), Offset(center.x, center.y + tickGap + tickLen), 1.2.dp.toPx())
                drawLine(reticleColor, Offset(center.x - tickGap, center.y), Offset(center.x - tickGap - tickLen, center.y), 1.2.dp.toPx())
                drawLine(reticleColor, Offset(center.x + tickGap, center.y), Offset(center.x + tickGap + tickLen, center.y), 1.2.dp.toPx())
            }

            // Zone Placement Pill
            if (isZoneSystemEnabled) {
                Box(
                    modifier = Modifier
                        .offset {
                            val badgeOffsetX = activePixelX - 28.dp.toPx()
                            val badgeOffsetY = activePixelY + 24.dp.toPx()
                            IntOffset(
                                badgeOffsetX.roundToInt().coerceIn(10, (totalWidth - 90.dp.toPx()).roundToInt()),
                                badgeOffsetY.roundToInt().coerceIn(10, (totalHeight - 40.dp.toPx()).roundToInt())
                            )
                        }
                        .background(Color(0xD9000000), RoundedCornerShape(6.dp))
                        .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ZONE ${targetZone.roman}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Distance / Rangefinder Focus Target (Active when Distance Mode is On)
        if (isDistanceModeEnabled) {
            val distPixelX = distanceNormX * totalWidth
            val distPixelY = distanceNormY * totalHeight

            // Leica / Contax Coincidence Rangefinder Split-Image Box
            Canvas(
                modifier = Modifier
                    .size(68.dp)
                    .offset {
                        IntOffset(
                            (distPixelX - 34.dp.toPx()).roundToInt(),
                            (distPixelY - 34.dp.toPx()).roundToInt()
                        )
                    }
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val reticleColor = when {
                    isDistanceMeasuring -> MeterAmberBright
                    isDistanceLowContrast -> MeterAmber
                    else -> MeterEmerald
                }

                // Precision corner brackets (Outer 44dp box)
                val outerBox = 44.dp.toPx()
                val cornerLen = 10.dp.toPx()
                val left = center.x - outerBox / 2
                val right = center.x + outerBox / 2
                val top = center.y - outerBox / 2
                val bot = center.y + outerBox / 2
                val strokeW = 1.5.dp.toPx()

                // Top-Left
                drawLine(reticleColor, Offset(left, top), Offset(left + cornerLen, top), strokeW)
                drawLine(reticleColor, Offset(left, top), Offset(left, top + cornerLen), strokeW)
                // Top-Right
                drawLine(reticleColor, Offset(right, top), Offset(right - cornerLen, top), strokeW)
                drawLine(reticleColor, Offset(right, top), Offset(right, top + cornerLen), strokeW)
                // Bottom-Left
                drawLine(reticleColor, Offset(left, bot), Offset(left + cornerLen, bot), strokeW)
                drawLine(reticleColor, Offset(left, bot), Offset(left, bot - cornerLen), strokeW)
                // Bottom-Right
                drawLine(reticleColor, Offset(right, bot), Offset(right - cornerLen, bot), strokeW)
                drawLine(reticleColor, Offset(right, bot), Offset(right, bot - cornerLen), strokeW)

                // Inner Rangefinder Coincidence Split Patch (20dp)
                val innerBox = 20.dp.toPx()
                drawRect(
                    color = reticleColor.copy(alpha = 0.15f),
                    topLeft = Offset(center.x - innerBox / 2, center.y - innerBox / 2),
                    size = Size(innerBox, innerBox)
                )
                drawRect(
                    color = reticleColor,
                    topLeft = Offset(center.x - innerBox / 2, center.y - innerBox / 2),
                    size = Size(innerBox, innerBox),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Micro Center Dot
                drawCircle(
                    color = reticleColor,
                    radius = 1.8.dp.toPx(),
                    center = center
                )
            }

            // Floating Rangefinder HUD & Depth-of-Field Info Card
            Box(
                modifier = Modifier
                    .offset {
                        val cardWidth = 210.dp.toPx()
                        val badgeOffsetX = (distPixelX - cardWidth / 2)
                        val badgeOffsetY = (distPixelY + 38.dp.toPx())
                        IntOffset(
                            badgeOffsetX.roundToInt().coerceIn(10, (totalWidth - cardWidth - 10.dp.toPx()).roundToInt()),
                            badgeOffsetY.roundToInt().coerceIn(10, (totalHeight - 75.dp.toPx()).roundToInt())
                        )
                    }
                    .background(Color(0xF20A1420), RoundedCornerShape(10.dp))
                    .border(1.2.dp, if (isDistanceLowContrast) MeterAmber else MeterEmerald, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    // Line 1: Target Distance Readout
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when {
                                isDistanceMeasuring -> "🎯 ${s.distanceMeasuring}"
                                isDistanceInfinity -> "🎯 ${s.infinityHyperfocal}"
                                measuredDistanceMeters != null -> {
                                    val meters = measuredDistanceMeters
                                    val feet = meters * 3.28084f
                                    String.format(Locale.US, "🎯 %.2fm · %.1fft", meters, feet)
                                }
                                isDistanceLowContrast -> "⚠️ ${s.lowContrastHint}"
                                else -> "🎯 ${s.tapToMeasure}"
                            },
                            color = if (isDistanceLowContrast) MeterAmberBright else MeterEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Line 2: Real-Time Depth of Field (DoF) & Hyperfocal
                    if (measuredDistanceMeters != null && !isDistanceInfinity) {
                        val dof = com.example.meter.calculator.ExposureCalculator.calculateDepthOfField(
                            focalLengthMm = focalLengthMm.toFloat(),
                            apertureN = effectiveAperture.toFloat(),
                            subjectDistanceMeters = measuredDistanceMeters
                        )
                        val nearStr = String.format(Locale.US, "%.1fm", dof.nearLimitMeters)
                        val farStr = if (dof.farLimitMeters != null) String.format(Locale.US, "%.1fm", dof.farLimitMeters) else "∞"
                        val hyperStr = String.format(Locale.US, "%.1fm", dof.hyperfocalMeters)

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "景深: $nearStr ~ $farStr · 超焦距: $hyperStr",
                            color = MeterTextSecondary,
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else if (isDistanceLowContrast) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "请对准被摄物边缘高反差区域",
                            color = MeterTextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // Top Floating Controls Capsule with Exact Focal & Zoom Confirmation
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp, start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Frameline & Real-time Focal Verification Pill
            val effectiveDisplayZoom = targetZoomRatio.coerceAtLeast(minPossibleZoom)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC121314))
                    .border(0.8.dp, if (isFocalTooWide) MeterAmber else Color(0x44FFFFFF), RoundedCornerShape(6.dp))
                    .clickable { onOpenFramelines() }
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = "Framelines",
                    tint = if (isFocalTooWide) MeterAmber else MeterCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (aspectRatio == FilmAspectRatio.FORMAT_135) {
                        "${focalLengthMm}mm (${String.format(Locale.US, "%.2fx", effectiveDisplayZoom)})"
                    } else {
                        "${aspectRatio.subLabel.take(5)} · ${focalLengthMm}mm (~${focal135EquivWidth.roundToInt()}mm · ${String.format(Locale.US, "%.2fx", effectiveDisplayZoom)})"
                    },
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Distance Spot-Link Toggle (Visible when in Distance Mode)
            if (isDistanceModeEnabled) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDistanceSpotLinkEnabled) MeterEmerald.copy(alpha = 0.25f) else Color(0xCC121314))
                        .border(
                            0.8.dp,
                            if (isDistanceSpotLinkEnabled) MeterEmerald else Color(0x44FFFFFF),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onToggleDistanceSpotLink() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("toggle_distance_spot_link_btn")
                ) {
                    Text(
                        text = if (isDistanceSpotLinkEnabled) s.linkSpotDistanceOn else s.linkSpotDistanceOff,
                        color = if (isDistanceSpotLinkEnabled) MeterEmerald else MeterTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Grid Indicator
            if (gridStyle != CompositionGridStyle.NONE) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xCC121314))
                        .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                        .clickable { onOpenFramelines() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = gridStyle.shortName,
                        color = MeterCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Out-of-FOV Warning Pill when target focal length exceeds hardware minimum zoom
        if (isFocalTooWide) {
            val minHardwareFocalMm = (minPossibleZoom * effectivePortraitBaseFocal * (aspectRatio.gateWidthMm / 36.0f)).roundToInt()
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset {
                        IntOffset(0, (outerFilmRect.top + 38.dp.toPx()).roundToInt())
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xEE2A1208))
                    .border(1.dp, MeterAmber, RoundedCornerShape(16.dp))
                    .clickable { onSwitchToUltraWide() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("out_of_fov_warning_pill"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "FOV Warning",
                    tint = MeterAmber,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = String.format(s.outOfFovWarning, focalLengthMm, minHardwareFocalMm),
                    color = MeterAmberBright,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Top-Right: Zone System Status Pill & Menu Pill
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Zone System Toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isZoneSystemEnabled) MeterAmber.copy(alpha = 0.25f) else Color(0xCC121314))
                .border(
                    width = 0.8.dp,
                    color = if (isZoneSystemEnabled) MeterAmber else Color(0x33FFFFFF),
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable { onToggleZoneSystem() }
                .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isZoneSystemEnabled) MeterAmber else MeterTextMuted, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isZoneSystemEnabled) s.zoneOn else s.zoneOff,
                        color = if (isZoneSystemEnabled) MeterAmberBright else MeterTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Menu Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xEE1A1C1E))
                    .border(0.8.dp, MeterCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .clickable { onOpenSecondaryMenu() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("viewfinder_secondary_menu_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Menu",
                        tint = MeterCyan,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = s.menu,
                        color = MeterCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bottom: Metering Mode & Exit Spot Metering Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset {
                    IntOffset(
                        0,
                        (outerFilmRect.bottom - 36.dp.toPx()).roundToInt()
                    )
                }
        ) {
            if (isSpotMeteringActive) {
                // Prominent Exit Spot Metering Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xEE12181F))
                        .border(1.2.dp, MeterCyan, RoundedCornerShape(20.dp))
                        .clickable { onResetToAverage() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("exit_spot_metering_btn"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "Spot Active",
                        tint = MeterCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${s.spotMeter} EV ${String.format(Locale.US, "%.1f", liveEv100)}",
                        color = MeterCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x3300E5FF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit Spot",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = s.exitSpotMeter,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Standard Average Metering Indicator
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xDD121314))
                        .border(0.8.dp, MeterBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusWeak,
                        contentDescription = "Matrix Average",
                        tint = MeterAmber,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = s.matrixAveragePrompt,
                        color = MeterTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
