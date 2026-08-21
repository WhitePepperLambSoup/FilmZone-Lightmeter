package com.example.meter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meter.calculator.ExposureCalculator
import com.example.meter.model.AppLanguage
import com.example.meter.model.AppStrings
import com.example.meter.model.PriorityMode
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterBlack
import com.example.ui.theme.MeterBorder
import com.example.ui.theme.MeterCardBg
import com.example.ui.theme.MeterCardElevated
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterDarkSurface
import com.example.ui.theme.MeterEmerald
import com.example.ui.theme.MeterOrange
import com.example.ui.theme.MeterRed
import com.example.ui.theme.MeterTextMuted
import com.example.ui.theme.MeterTextPrimary
import com.example.ui.theme.MeterTextSecondary
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ExposureControlDials(
    modifier: Modifier = Modifier,
    ev100: Double,
    evForIso: Double,
    lux: Double,
    iso: Int,
    selectedAperture: Double,
    selectedShutterSec: Double,
    recommendedAperture: Double,
    recommendedShutterSec: Double,
    reciprocityCompensatedShutterSec: Double,
    reciprocityStopsAdded: Double,
    priorityMode: PriorityMode,
    isHold: Boolean,
    exposureCompEv: Double,
    ndStops: Double,
    appLanguage: AppLanguage = AppLanguage.DEFAULT,
    onSetAperture: (Double) -> Unit,
    onSetShutter: (Double) -> Unit,
    onSetIso: (Int) -> Unit,
    onSetPriorityMode: (PriorityMode) -> Unit,
    onToggleHold: () -> Unit,
    onSetExposureComp: (Double) -> Unit,
    onSetNdStops: (Double) -> Unit,
    onLogShot: () -> Unit,
    onOpenReciprocityTimer: () -> Unit
) {
    val s = AppStrings.get(appLanguage)
    val actualShutter = if (priorityMode == PriorityMode.SHUTTER) selectedShutterSec else recommendedShutterSec
    val actualAperture = if (priorityMode == PriorityMode.APERTURE) selectedAperture else recommendedAperture
    val isLongExposure = actualShutter >= 1.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MeterBlack)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Top Gauge Row: Large EV Readout and Priority Mode & Hold Control
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Main EV readout (Fixed layout to avoid jumping or pushing siblings)
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format(Locale.US, "EV %.1f", evForIso),
                        color = if (isHold) MeterCyan else Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format(Locale.US, "(EV100 %.1f)", ev100),
                        color = MeterTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                Text(
                    text = String.format(Locale.US, "%.0f LUX · %.1f FC", lux, ExposureCalculator.luxToFootCandles(lux)),
                    color = MeterTextMuted,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.3.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Action Cluster: Priority Pills + Hold Button (Fixed width & isolated)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Quick Priority Mode Selector Pills (Clean Minimalist Capsule)
                Row(
                    modifier = Modifier
                        .background(MeterCardBg, RoundedCornerShape(50))
                        .border(1.dp, com.example.ui.theme.MeterBorderSubtle, RoundedCornerShape(50))
                        .padding(2.5.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf(
                        PriorityMode.APERTURE to "A",
                        PriorityMode.SHUTTER to "S",
                        PriorityMode.ISO_LOCK to "ISO"
                    ).forEach { (mode, label) ->
                        val isSelected = priorityMode == mode
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) com.example.ui.theme.MeterAccentPrimary else Color.Transparent,
                                    RoundedCornerShape(50)
                                )
                                .clickable { onSetPriorityMode(mode) }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                .testTag("priority_mode_${mode.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) com.example.ui.theme.MeterAccentOnPrimary else MeterTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Hold / Lock Button (Sleek Circle)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isHold) MeterCyan.copy(alpha = 0.2f) else MeterCardBg,
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (isHold) MeterCyan else com.example.ui.theme.MeterBorderSubtle,
                            CircleShape
                        )
                        .clickable { onToggleHold() }
                        .testTag("hold_exposure_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isHold) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Hold Reading",
                        tint = if (isHold) MeterCyan else Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Analog EV Needle / Gauge Bar (Clean Minimalist Ticks)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(MeterCardBg, RoundedCornerShape(8.dp))
                .border(1.dp, com.example.ui.theme.MeterBorderSubtle, RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val minEv = -2.0
                val maxEv = 18.0
                val range = maxEv - minEv

                // Draw subtle tick marks
                for (ev in -2..18 step 2) {
                    val norm = ((ev - minEv) / range).toFloat().coerceIn(0f, 1f)
                    val x = norm * w
                    drawLine(
                        color = Color(0x33FFFFFF),
                        start = Offset(x, h * 0.45f),
                        end = Offset(x, h),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Needle for current EV
                val needleNorm = ((evForIso - minEv) / range).toFloat().coerceIn(0f, 1f)
                val needleX = needleNorm * w
                drawLine(
                    color = if (isHold) MeterCyan else MeterRed,
                    start = Offset(needleX, 0f),
                    end = Offset(needleX, h),
                    strokeWidth = 2.dp.toPx()
                )
                drawCircle(
                    color = if (isHold) MeterCyan else MeterRed,
                    radius = 2.5.dp.toPx(),
                    center = Offset(needleX, h / 2)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Exposure Parameter Steppers (Aperture, Shutter Speed, ISO)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Aperture Stepper
            ExposureStepperCard(
                modifier = Modifier.weight(1f),
                title = s.apertureLabel,
                valueStr = ExposureCalculator.formatAperture(actualAperture),
                isLocked = priorityMode == PriorityMode.APERTURE,
                onPrevious = {
                    val currentIdx = ExposureCalculator.APERTURES.indexOfFirst { kotlin.math.abs(it - selectedAperture) < 0.05 }
                    if (currentIdx > 0) {
                        onSetAperture(ExposureCalculator.APERTURES[currentIdx - 1])
                    }
                },
                onNext = {
                    val currentIdx = ExposureCalculator.APERTURES.indexOfFirst { kotlin.math.abs(it - selectedAperture) < 0.05 }
                    if (currentIdx < ExposureCalculator.APERTURES.size - 1 && currentIdx >= 0) {
                        onSetAperture(ExposureCalculator.APERTURES[currentIdx + 1])
                    }
                },
                testTagPrefix = "aperture"
            )

            // 2. Shutter Speed Stepper
            ExposureStepperCard(
                modifier = Modifier.weight(1f),
                title = s.shutterLabel,
                valueStr = ExposureCalculator.formatShutter(actualShutter),
                isLocked = priorityMode == PriorityMode.SHUTTER,
                isCompensated = isLongExposure && reciprocityStopsAdded > 0.1,
                compensatedValueStr = if (isLongExposure) ExposureCalculator.formatShutter(reciprocityCompensatedShutterSec) else null,
                onPrevious = {
                    val currentIdx = ExposureCalculator.SHUTTER_SPEEDS.indexOfFirst { kotlin.math.abs(it - selectedShutterSec) < 0.00001 }
                    if (currentIdx > 0) {
                        onSetShutter(ExposureCalculator.SHUTTER_SPEEDS[currentIdx - 1])
                    }
                },
                onNext = {
                    val currentIdx = ExposureCalculator.SHUTTER_SPEEDS.indexOfFirst { kotlin.math.abs(it - selectedShutterSec) < 0.00001 }
                    if (currentIdx < ExposureCalculator.SHUTTER_SPEEDS.size - 1 && currentIdx >= 0) {
                        onSetShutter(ExposureCalculator.SHUTTER_SPEEDS[currentIdx + 1])
                    }
                },
                testTagPrefix = "shutter"
            )

            // 3. ISO Stepper
            ExposureStepperCard(
                modifier = Modifier.weight(1f),
                title = s.isoLabel,
                valueStr = "$iso",
                isLocked = true,
                onPrevious = {
                    val currentIdx = ExposureCalculator.ISO_VALUES.indexOf(iso)
                    if (currentIdx > 0) {
                        onSetIso(ExposureCalculator.ISO_VALUES[currentIdx - 1])
                    }
                },
                onNext = {
                    val currentIdx = ExposureCalculator.ISO_VALUES.indexOf(iso)
                    if (currentIdx < ExposureCalculator.ISO_VALUES.size - 1 && currentIdx >= 0) {
                        onSetIso(ExposureCalculator.ISO_VALUES[currentIdx + 1])
                    }
                },
                testTagPrefix = "iso"
            )
        }

        // Long exposure Reciprocity Failure notification bar if shutter > 1s
        if (isLongExposure) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A1705), RoundedCornerShape(8.dp))
                    .border(1.dp, MeterOrange, RoundedCornerShape(8.dp))
                    .clickable { onOpenReciprocityTimer() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("reciprocity_banner_btn")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Reciprocity Timer",
                            tint = MeterOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = s.reciprocityBannerTitle,
                                color = MeterOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(
                                    Locale.US,
                                    s.reciprocityBannerFormat,
                                    ExposureCalculator.formatShutter(actualShutter),
                                    ExposureCalculator.formatShutter(reciprocityCompensatedShutterSec),
                                    reciprocityStopsAdded
                                ),
                                color = MeterTextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(MeterOrange, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = s.openTimer,
                            color = MeterBlack,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Exposure Compensation & ND Filter & Quick Log Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Exposure Compensation (-3 to +3 EV)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MeterCardBg, RoundedCornerShape(50))
                    .border(1.dp, com.example.ui.theme.MeterBorderSubtle, RoundedCornerShape(50))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "EV",
                    color = MeterTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp)
                )
                IconButton(
                    onClick = { onSetExposureComp((exposureCompEv - 0.33).coerceIn(-3.0, 3.0)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease EV", tint = MeterTextPrimary, modifier = Modifier.size(13.dp))
                }
                Text(
                    text = if (exposureCompEv > 0) "+${String.format(Locale.US, "%.1f", exposureCompEv)}" else String.format(Locale.US, "%.1f", exposureCompEv),
                    color = if (exposureCompEv != 0.0) MeterAmber else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(
                    onClick = { onSetExposureComp((exposureCompEv + 0.33).coerceIn(-3.0, 3.0)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase EV", tint = MeterTextPrimary, modifier = Modifier.size(13.dp))
                }
            }

            // ND Filter Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MeterCardBg, RoundedCornerShape(50))
                    .border(1.dp, com.example.ui.theme.MeterBorderSubtle, RoundedCornerShape(50))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ND",
                    color = MeterTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp)
                )
                listOf(0.0 to "0", 3.0 to "8", 6.0 to "64", 10.0 to "1K").forEach { (stops, label) ->
                    val isSelected = ndStops == stops
                    Box(
                        modifier = Modifier
                            .background(if (isSelected) com.example.ui.theme.MeterAccentPrimary else Color.Transparent, RoundedCornerShape(50))
                            .clickable { onSetNdStops(stops) }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) com.example.ui.theme.MeterAccentOnPrimary else MeterTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Log Shot Button (Minimalist Pill)
            Box(
                modifier = Modifier
                    .background(com.example.ui.theme.MeterAccentPrimary, RoundedCornerShape(50))
                    .clickable { onLogShot() }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("log_shot_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Log Shot",
                        tint = com.example.ui.theme.MeterAccentOnPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = s.logShot,
                        color = com.example.ui.theme.MeterAccentOnPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ExposureStepperCard(
    modifier: Modifier = Modifier,
    title: String,
    valueStr: String,
    isLocked: Boolean,
    isCompensated: Boolean = false,
    compensatedValueStr: String? = null,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    testTagPrefix: String
) {
    Column(
        modifier = modifier
            .background(MeterCardBg, RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isCompensated) MeterOrange else if (isLocked) com.example.ui.theme.MeterAccentPrimary.copy(alpha = 0.8f) else com.example.ui.theme.MeterBorderSubtle,
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = MeterTextSecondary,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Main value display
        Text(
            text = valueStr,
            color = if (isLocked) com.example.ui.theme.MeterAccentPrimary else MeterTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        if (isCompensated && compensatedValueStr != null) {
            Text(
                text = "➔ $compensatedValueStr",
                color = MeterOrange,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Stepper Buttons (< and >)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MeterCardElevated, CircleShape)
                    .clickable { onPrevious() }
                    .testTag("${testTagPrefix}_prev_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous",
                    tint = MeterTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MeterCardElevated, CircleShape)
                    .clickable { onNext() }
                    .testTag("${testTagPrefix}_next_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next",
                    tint = MeterTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
