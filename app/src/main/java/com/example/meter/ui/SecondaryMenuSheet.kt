package com.example.meter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meter.model.AppLanguage
import com.example.meter.model.AppStrings
import com.example.meter.model.CameraLensType
import com.example.meter.model.CameraOpticsInfo
import com.example.meter.model.ColorFilterMode
import com.example.meter.model.CompositionGridStyle
import com.example.meter.model.MeteringMode
import com.example.meter.model.PhysicalLensInfo
import com.example.meter.model.RangefinderEngineMode
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterBorder
import com.example.ui.theme.MeterBorderSubtle
import com.example.ui.theme.MeterCardBg
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterDarkSurface
import com.example.ui.theme.MeterEmerald
import com.example.ui.theme.MeterTextMuted
import com.example.ui.theme.MeterTextPrimary
import com.example.ui.theme.MeterTextSecondary
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryMenuSheet(
    calibrationOffset: Double,
    meteringMode: MeteringMode,
    isSpotMeteringActive: Boolean,
    isExperimentalMultiCameraEnabled: Boolean,
    selectedCameraLens: CameraLensType,
    selectedCameraId: String? = null,
    selectedColorFilter: ColorFilterMode,
    isZoneSystemEnabled: Boolean,
    isFalseColorEnabled: Boolean,
    isDistanceModeEnabled: Boolean = false,
    isDistanceSpotLinkEnabled: Boolean = false,
    rangefinderEngineMode: RangefinderEngineMode = RangefinderEngineMode.INCLINOMETER,
    userHeightMeters: Float = 1.50f,
    afDistanceCalibrationScale: Float = 1.0f,
    gridStyle: CompositionGridStyle,
    isTorchOn: Boolean,
    appLanguage: AppLanguage = AppLanguage.DEFAULT,
    opticsInfo: CameraOpticsInfo = com.example.meter.camera.CameraOpticsDetector.createFallbackOptics(),
    manualBaseFocal: Float? = null,
    onSetCalibrationOffset: (Double) -> Unit,
    onSetMeteringMode: (MeteringMode) -> Unit,
    onExitSpotMetering: () -> Unit,
    onToggleDistanceMode: () -> Unit = {},
    onToggleDistanceSpotLink: () -> Unit = {},
    onSelectRangefinderEngineMode: (RangefinderEngineMode) -> Unit = {},
    onSetUserHeight: (Float) -> Unit = {},
    onSetAfDistanceCalibrationScale: (Float) -> Unit = {},
    onCalibrateAfAtOneMeter: () -> Unit = {},
    onToggleMultiCamera: () -> Unit,
    onSelectCameraLens: (CameraLensType) -> Unit,
    onSelectPhysicalLens: (PhysicalLensInfo) -> Unit = {},
    onSelectColorFilter: (ColorFilterMode) -> Unit,
    onToggleZoneSystem: () -> Unit,
    onToggleFalseColor: () -> Unit,
    onSelectGridStyle: (CompositionGridStyle) -> Unit,
    onToggleTorch: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit = {},
    onSetManualBaseFocal: (Float) -> Unit = {},
    onResetManualBaseFocal: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val s = AppStrings.get(appLanguage)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MeterDarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MeterAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Menu",
                            tint = MeterAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = s.menu,
                            color = MeterTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${s.calibrationOffset} · ${s.language} · ${s.cameraOptics}",
                            color = MeterTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MeterTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 0. Language Selection Section (10 Supported Languages)
            MenuSectionTitle(title = s.language, icon = Icons.Default.Language)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MeterCardBg)
                    .border(1.dp, MeterBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AppLanguage.entries) { lang ->
                        val isSelected = appLanguage == lang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MeterAmber.copy(alpha = 0.25f) else Color(0x22FFFFFF)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MeterAmber else Color(0x33FFFFFF),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectLanguage(lang) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("language_option_${lang.code}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = lang.nativeName,
                                    color = if (isSelected) MeterAmberBright else MeterTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = lang.displayName,
                                    color = MeterTextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Metering Mode Selection & Spot Metering Exit
            MenuSectionTitle(title = s.matrixMeter, icon = Icons.Default.CenterFocusStrong)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MeterCardBg)
                    .border(1.dp, MeterBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MeteringMode.entries.forEach { mode ->
                            val isSelected = meteringMode == mode && (mode != MeteringMode.SPOT || isSpotMeteringActive)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MeterAmber.copy(alpha = 0.2f) else Color(0x22FFFFFF)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MeterAmber else Color(0x33FFFFFF),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSetMeteringMode(mode) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = mode.shortName,
                                        color = if (isSelected) MeterAmberBright else MeterTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (appLanguage == AppLanguage.SIMPLIFIED_CHINESE) mode.labelZh else mode.shortName,
                                        color = MeterTextSecondary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    // Exit spot metering quick action button
                    if (isSpotMeteringActive) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x3300E5FF))
                                .border(1.dp, MeterCyan, RoundedCornerShape(8.dp))
                                .clickable { onExitSpotMetering() }
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RestartAlt, contentDescription = "Exit Spot", tint = MeterCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = s.spotMeter,
                                    color = MeterCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = s.reset,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Rangefinder Distance Measurement Mode
            MenuSectionTitle(title = s.rangefinder, icon = Icons.Default.CenterFocusStrong)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MeterCardBg)
                    .border(1.dp, MeterBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = s.rangefinder, color = MeterTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = s.tapToMeasure, color = MeterTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isDistanceModeEnabled,
                            onCheckedChange = { onToggleDistanceMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MeterEmerald,
                                checkedTrackColor = MeterEmerald.copy(alpha = 0.3f),
                                uncheckedThumbColor = MeterTextMuted,
                                uncheckedTrackColor = MeterBorder
                            )
                        )
                    }

                    if (isDistanceModeEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "${s.rangefinder} + ${s.spotMeter}", color = MeterTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = s.tapToMeasure, color = MeterTextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isDistanceSpotLinkEnabled,
                                onCheckedChange = { onToggleDistanceSpotLink() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MeterCyan,
                                    checkedTrackColor = MeterCyan.copy(alpha = 0.3f),
                                    uncheckedThumbColor = MeterTextMuted,
                                    uncheckedTrackColor = MeterBorder
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Hardware Exposure Calibration Module
            MenuSectionTitle(title = s.calibrationOffset, icon = Icons.Default.Settings)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MeterCardBg)
                    .border(1.dp, MeterBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = s.calibrationOffset,
                                color = MeterTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = s.calibrationDesc,
                                color = MeterTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = if (calibrationOffset > 0) "+${String.format(Locale.US, "%.1f", calibrationOffset)} EV" else "${String.format(Locale.US, "%.1f", calibrationOffset)} EV",
                            color = MeterAmberBright,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = calibrationOffset.toFloat().coerceIn(-6.0f, 6.0f),
                        onValueChange = {
                            val rounded = (it * 10).roundToInt() / 10.0
                            onSetCalibrationOffset(rounded)
                        },
                        valueRange = -6.0f..6.0f,
                        steps = 119,
                        colors = SliderDefaults.colors(
                            thumbColor = MeterAmberBright,
                            activeTrackColor = MeterAmber,
                            inactiveTrackColor = MeterBorder
                        ),
                        modifier = Modifier.testTag("secondary_menu_calibration_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            MenuCalibPill("-1.0") { onSetCalibrationOffset((calibrationOffset - 1.0).coerceIn(-6.0, 6.0)) }
                            MenuCalibPill("-0.5") { onSetCalibrationOffset((calibrationOffset - 0.5).coerceIn(-6.0, 6.0)) }
                            MenuCalibPill("-0.1") { onSetCalibrationOffset((calibrationOffset - 0.1).coerceIn(-6.0, 6.0)) }
                        }

                        Text(
                            text = s.reset,
                            color = MeterCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onSetCalibrationOffset(0.0) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            MenuCalibPill("+0.1") { onSetCalibrationOffset((calibrationOffset + 0.1).coerceIn(-6.0, 6.0)) }
                            MenuCalibPill("+0.5") { onSetCalibrationOffset((calibrationOffset + 0.5).coerceIn(-6.0, 6.0)) }
                            MenuCalibPill("+1.0") { onSetCalibrationOffset((calibrationOffset + 1.0).coerceIn(-6.0, 6.0)) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Multi-Camera Hardware Switching & Physical Sub-Camera Detection
            MenuSectionTitle(title = s.cameraOptics, icon = Icons.Default.CameraAlt)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MeterCardBg)
                    .border(1.dp, MeterBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    // Hardware Detection Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Camera2 ${s.cameraOptics}",
                                color = MeterCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Physical Focal: ${String.format(Locale.US, "%.2f", opticsInfo.physicalFocalLengthMm)}mm (${String.format(Locale.US, "%.1f", opticsInfo.sensorWidthMm)}×${String.format(Locale.US, "%.1f", opticsInfo.sensorHeightMm)}mm)",
                                color = MeterTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x3300E5FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (opticsInfo.isAutoDetected) "HARDWARE" else "OPTICS",
                                color = MeterCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2 Equivalent numbers side by side (Landscape vs Portrait)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33000000))
                                .padding(8.dp)
                        ) {
                            Text(s.landscapeEquiv, color = MeterTextMuted, fontSize = 10.sp)
                            Text(
                                "${String.format(Locale.US, "%.1f", opticsInfo.landscapeEquivFocalMm)}mm",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("FOV ~${opticsInfo.portraitVFOV.roundToInt()}°", color = MeterTextSecondary, fontSize = 9.sp)
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33000000))
                                .border(1.dp, MeterAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(s.portraitEquiv, color = MeterAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${String.format(Locale.US, "%.1f", opticsInfo.portraitEquivFocalMm)}mm",
                                color = MeterAmberBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("H-FOV ~${opticsInfo.portraitHFOV.roundToInt()}°", color = MeterAmber, fontSize = 9.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Detected Physical Sub-Cameras List
                    Text(
                        text = "Detected Physical Lenses (${opticsInfo.availableLenses.size}):",
                        color = MeterTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(opticsInfo.availableLenses) { physicalLens ->
                            val isSelected = (selectedCameraId == physicalLens.cameraId) || 
                                             (selectedCameraId == null && selectedCameraLens == physicalLens.lensType)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MeterCyan.copy(alpha = 0.25f) else Color(0x22FFFFFF))
                                    .border(1.dp, if (isSelected) MeterCyan else Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                    .clickable { 
                                        onSelectPhysicalLens(physicalLens)
                                        onSelectCameraLens(physicalLens.lensType)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${physicalLens.displayName} (${physicalLens.lensType.zoomRatio}x)",
                                        color = if (isSelected) MeterCyan else MeterTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "ID ${physicalLens.cameraId} · ${String.format(Locale.US, "%.1f", physicalLens.landscapeEquivFocalMm)}mm",
                                        color = MeterTextSecondary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Base focal calibration slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${s.cameraOptics} (${String.format(Locale.US, "%.1f", opticsInfo.landscapeEquivFocalMm)}mm)",
                            color = MeterTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (manualBaseFocal != null) {
                            Text(
                                text = s.reset,
                                color = MeterAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onResetManualBaseFocal() }
                            )
                        }
                    }

                    Slider(
                        value = opticsInfo.landscapeEquivFocalMm.coerceIn(20f, 35f),
                        onValueChange = { onSetManualBaseFocal(it) },
                        valueRange = 20f..35f,
                        colors = SliderDefaults.colors(
                            thumbColor = MeterCyan,
                            activeTrackColor = MeterCyan,
                            inactiveTrackColor = MeterBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Multi-camera hardware switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.lensSwitch,
                                color = MeterTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ultra-Wide (0.6x), Main (1x), Telephoto (2x/3x/5x)",
                                color = MeterTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = isExperimentalMultiCameraEnabled,
                            onCheckedChange = { onToggleMultiCamera() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MeterCyan,
                                checkedTrackColor = MeterCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = MeterTextMuted,
                                uncheckedTrackColor = MeterBorder
                            )
                        )
                    }

                    if (isExperimentalMultiCameraEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(CameraLensType.entries) { lens ->
                                val isSelected = selectedCameraLens == lens
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MeterCyan.copy(alpha = 0.2f) else Color(0x22FFFFFF))
                                        .border(1.dp, if (isSelected) MeterCyan else Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                        .clickable { onSelectCameraLens(lens) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (appLanguage == AppLanguage.SIMPLIFIED_CHINESE) lens.labelZh else lens.labelEn,
                                        color = if (isSelected) MeterCyan else MeterTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Color Filters & B&W Simulation
            MenuSectionTitle(title = s.colorFilters, icon = Icons.Default.ColorLens)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MeterCardBg)
                    .border(1.dp, MeterBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ColorFilterMode.entries) { filter ->
                        val isSelected = selectedColorFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MeterAmber.copy(alpha = 0.25f) else Color(0x22FFFFFF))
                                .border(1.dp, if (isSelected) MeterAmber else Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                .clickable { onSelectColorFilter(filter) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (appLanguage == AppLanguage.SIMPLIFIED_CHINESE) filter.labelZh else filter.labelEn,
                                    color = if (isSelected) MeterAmberBright else MeterTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (filter.filterFactorStops > 0) "+${filter.filterFactorStops.toInt()} EV" else "0 EV",
                                    color = MeterTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Ansel Adams Zone System & False Color
            MenuSectionTitle(title = s.zoneSystem, icon = Icons.Default.Tonality)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MeterCardBg)
                    .border(1.dp, MeterBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = s.zoneSystem, color = MeterTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Zone 0 ~ Zone X (11 Zones)", color = MeterTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isZoneSystemEnabled,
                            onCheckedChange = { onToggleZoneSystem() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MeterAmberBright,
                                checkedTrackColor = MeterAmber.copy(alpha = 0.3f),
                                uncheckedThumbColor = MeterTextMuted,
                                uncheckedTrackColor = MeterBorder
                            )
                        )
                    }

                    if (isZoneSystemEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = s.falseColor, color = MeterTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = "IRE Heatmap", color = MeterTextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isFalseColorEnabled,
                                onCheckedChange = { onToggleFalseColor() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MeterCyan,
                                    checkedTrackColor = MeterCyan.copy(alpha = 0.3f),
                                    uncheckedThumbColor = MeterTextMuted,
                                    uncheckedTrackColor = MeterBorder
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7. Rangefinder & Distance Measurement Settings
            MenuSectionTitle(title = s.rangefinder, icon = Icons.Default.CenterFocusStrong)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MeterCardBg)
                    .border(1.dp, MeterBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    // Rangefinder Master Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = s.rangefinder, color = MeterTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isDistanceModeEnabled) s.distanceOn else s.distanceOff,
                                color = if (isDistanceModeEnabled) MeterEmerald else MeterTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isDistanceModeEnabled,
                            onCheckedChange = { onToggleDistanceMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MeterEmerald,
                                checkedTrackColor = MeterEmerald.copy(alpha = 0.3f),
                                uncheckedThumbColor = MeterTextMuted,
                                uncheckedTrackColor = MeterBorder
                            )
                        )
                    }

                    if (isDistanceModeEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // AF Distance Calibration Scale
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = s.afCalibrationScale, color = MeterTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format(Locale.US, "%.2fx", afDistanceCalibrationScale),
                                color = MeterAmberBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = afDistanceCalibrationScale,
                            onValueChange = { onSetAfDistanceCalibrationScale(it) },
                            valueRange = 0.40f..2.50f,
                            steps = 20,
                            colors = SliderDefaults.colors(
                                thumbColor = MeterAmberBright,
                                activeTrackColor = MeterAmber,
                                inactiveTrackColor = MeterBorder
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MeterAmber.copy(alpha = 0.2f))
                                .border(1.dp, MeterAmber, RoundedCornerShape(8.dp))
                                .clickable { onCalibrateAfAtOneMeter() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎯 ${s.calibrateAfOneMeter}",
                                color = MeterAmberBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Spot Link Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "联动点测光", color = MeterTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = if (isDistanceSpotLinkEnabled) s.linkSpotDistanceOn else s.linkSpotDistanceOff, color = MeterTextSecondary, fontSize = 10.sp)
                            }
                            Switch(
                                checked = isDistanceSpotLinkEnabled,
                                onCheckedChange = { onToggleDistanceSpotLink() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MeterEmerald,
                                    checkedTrackColor = MeterEmerald.copy(alpha = 0.3f),
                                    uncheckedThumbColor = MeterTextMuted,
                                    uncheckedTrackColor = MeterBorder
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 8. Composition Grid Guides
            MenuSectionTitle(title = s.gridGuides, icon = Icons.Default.GridOn)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MeterCardBg)
                    .border(1.dp, MeterBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CompositionGridStyle.entries) { grid ->
                        val isSelected = gridStyle == grid
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MeterCyan.copy(alpha = 0.25f) else Color(0x22FFFFFF))
                                .border(1.dp, if (isSelected) MeterCyan else Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                .clickable { onSelectGridStyle(grid) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (appLanguage == AppLanguage.SIMPLIFIED_CHINESE) grid.labelZh else grid.name,
                                color = if (isSelected) MeterCyan else MeterTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 8. Flashlight / Torch
            MenuSectionTitle(title = s.torch, icon = Icons.Default.FlashOn)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MeterCardBg)
                    .border(1.dp, MeterBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = s.torch, color = MeterTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "AF Light & Low Light Metering", color = MeterTextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isTorchOn,
                        onCheckedChange = { onToggleTorch() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MeterAmberBright,
                            checkedTrackColor = MeterAmber.copy(alpha = 0.3f),
                            uncheckedThumbColor = MeterTextMuted,
                            uncheckedTrackColor = MeterBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MenuSectionTitle(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = MeterAmber, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = MeterAmber,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun MenuCalibPill(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x33FFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
