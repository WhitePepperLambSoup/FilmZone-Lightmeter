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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meter.model.AppLanguage
import com.example.meter.model.AppStrings
import com.example.meter.model.COMMON_FOCAL_PRESETS
import com.example.meter.model.CameraLensType
import com.example.meter.model.ColorFilterMode
import com.example.meter.model.CompositionGridStyle
import com.example.meter.model.FilmAspectRatio
import com.example.meter.model.FocalLengthChoice
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterBlack
import com.example.ui.theme.MeterBorder
import com.example.ui.theme.MeterCardBg
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterDarkSurface
import com.example.ui.theme.MeterTextMuted
import com.example.ui.theme.MeterTextPrimary
import com.example.ui.theme.MeterTextSecondary
import java.util.Locale
import kotlin.math.atan
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FramelineSettingsSheet(
    selectedAspectRatio: FilmAspectRatio,
    focalLengthMm: Int,
    gridStyle: CompositionGridStyle,
    selectedColorFilter: ColorFilterMode,
    isExperimentalMultiCameraEnabled: Boolean,
    selectedCameraLens: CameraLensType,
    opticsInfo: com.example.meter.model.CameraOpticsInfo = com.example.meter.camera.CameraOpticsDetector.createFallbackOptics(),
    manualBaseFocal: Float? = null,
    appLanguage: AppLanguage = AppLanguage.DEFAULT,
    onSelectAspectRatio: (FilmAspectRatio) -> Unit,
    onSelectCustomFocalLength: (Int) -> Unit,
    onSelectGridStyle: (CompositionGridStyle) -> Unit,
    onSelectColorFilter: (ColorFilterMode) -> Unit,
    onToggleMultiCamera: () -> Unit,
    onSelectCameraLens: (CameraLensType) -> Unit,
    onSetManualBaseFocal: (Float) -> Unit = {},
    onResetManualBaseFocal: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCustomInputDialog by remember { mutableStateOf(false) }
    val s = AppStrings.get(appLanguage)
    val isZh = appLanguage == AppLanguage.SIMPLIFIED_CHINESE || appLanguage == AppLanguage.TRADITIONAL_CHINESE

    // Calculate approximate 35mm horizontal FOV based on phone portrait orientation:
    val fovDeg = remember(focalLengthMm) {
        val rad = 2.0 * atan(36.0 / (2.0 * focalLengthMm.toDouble()))
        Math.toDegrees(rad)
    }

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
                    Icon(
                        imageVector = Icons.Default.AspectRatio,
                        contentDescription = "Framelines",
                        tint = MeterAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "画幅框线与自定义焦距" else "${s.framelines} & Focal Length",
                        color = MeterTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MeterTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Custom Focal Length Controller (Hero Module)
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
                                text = if (isZh) "镜头焦距 (35mm 等效)" else "Focal Length (35mm Equiv)",
                                color = MeterTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(Locale.US, "FOV: %.1f° · %s", fovDeg, getFocalCategory(focalLengthMm, isZh)),
                                color = MeterTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Big Focal Length Value with Direct Edit Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33FFA000))
                                .clickable { showCustomInputDialog = true }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${focalLengthMm}mm",
                                color = MeterAmberBright,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Custom Focal",
                                tint = MeterAmber,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Continuous Slider for Smooth Adjustment
                    Slider(
                        value = focalLengthMm.toFloat().coerceIn(15f, 200f),
                        onValueChange = { onSelectCustomFocalLength(it.roundToInt()) },
                        valueRange = 15f..200f,
                        colors = SliderDefaults.colors(
                            thumbColor = MeterAmberBright,
                            activeTrackColor = MeterAmber,
                            inactiveTrackColor = MeterBorder
                        ),
                        modifier = Modifier.testTag("focal_length_slider")
                    )

                    // Quick Stepper Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StepperPill(label = "-10") { onSelectCustomFocalLength(focalLengthMm - 10) }
                            StepperPill(label = "-1") { onSelectCustomFocalLength(focalLengthMm - 1) }
                        }

                        Text(
                            text = if (isZh) "自由自定义任意焦距" else "Freely customize focal length",
                            color = MeterTextMuted,
                            fontSize = 10.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StepperPill(label = "+1") { onSelectCustomFocalLength(focalLengthMm + 1) }
                            StepperPill(label = "+10") { onSelectCustomFocalLength(focalLengthMm + 10) }
                        }
                    }
                }
            }

            // 1.5 Real Camera Optical Analysis & Ground-Truth Calibration Card
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xEE12181F))
                    .border(1.dp, MeterCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Optics",
                                tint = MeterCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isZh) "相机硬件光学真值检测与标定" else s.cameraOptics,
                                color = MeterCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (opticsInfo.isAutoDetected) Color(0x3300E5FF) else Color(0x33FFA000))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (opticsInfo.isAutoDetected) (if (isZh) "Camera2 实时检测" else "Camera2 Live") else (if (isZh) "标准光学模型" else "Fallback Model"),
                                color = if (opticsInfo.isAutoDetected) MeterCyan else MeterAmberBright,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Optics Parameter Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Landscape Base
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33000000))
                                .padding(8.dp)
                        ) {
                            Text(s.landscapeEquiv, color = MeterTextMuted, fontSize = 9.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${String.format(Locale.US, "%.1f", opticsInfo.landscapeEquivFocalMm)}mm",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("FOV ~${opticsInfo.portraitVFOV.roundToInt()}°", color = MeterTextSecondary, fontSize = 9.sp)
                        }

                        // Portrait Actual FOV
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33000000))
                                .border(1.dp, MeterAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(s.portraitEquiv, color = MeterAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${String.format(Locale.US, "%.1f", opticsInfo.portraitEquivFocalMm)}mm",
                                color = MeterAmberBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("H-FOV ~${opticsInfo.portraitHFOV.roundToInt()}°", color = MeterAmber, fontSize = 9.sp)
                        }

                        // Physical Focal & Sensor
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33000000))
                                .padding(8.dp)
                        ) {
                            Text(if (isZh) "物理焦距 / 传感器" else "Physical Sensor", color = MeterTextMuted, fontSize = 9.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${String.format(Locale.US, "%.2f", opticsInfo.physicalFocalLengthMm)}mm",
                                color = MeterTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("${String.format(Locale.US, "%.1f", opticsInfo.sensorWidthMm)}×${String.format(Locale.US, "%.1f", opticsInfo.sensorHeightMm)}mm", color = MeterTextSecondary, fontSize = 9.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Manual Calibration Offset Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isZh) "主摄基准等效微调 (${String.format(Locale.US, "%.1f", opticsInfo.landscapeEquivFocalMm)}mm)" else "Base Focal Fine-tune (${String.format(Locale.US, "%.1f", opticsInfo.landscapeEquivFocalMm)}mm)",
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
                        ),
                        modifier = Modifier.testTag("base_focal_calibration_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Common Focal Length Presets
            Text(
                text = if (isZh) "经典常用焦段快捷选择" else "Focal Length Presets",
                color = MeterTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(COMMON_FOCAL_PRESETS) { preset ->
                    val isSelected = preset.focalMm == focalLengthMm
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MeterAmber else MeterCardBg)
                            .border(1.dp, if (isSelected) MeterAmber else MeterBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelectCustomFocalLength(preset.focalMm) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("focal_preset_${preset.focalMm}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isZh) preset.nameZh else preset.nameEn,
                                color = if (isSelected) MeterBlack else MeterTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isZh) preset.categoryZh.take(4) else preset.categoryEn.take(10),
                                color = if (isSelected) MeterBlack.copy(alpha = 0.75f) else MeterTextMuted,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Film Format & Aspect Ratio
            Text(
                text = s.formatGate,
                color = MeterTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FilmAspectRatio.entries.chunked(2).forEach { rowList ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowList.forEach { format ->
                            val isSelected = format == selectedAspectRatio
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MeterAmber.copy(alpha = 0.15f) else MeterCardBg)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) MeterAmber else MeterBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectAspectRatio(format) }
                                    .padding(vertical = 9.dp, horizontal = 10.dp)
                                    .testTag("aspect_ratio_${format.name}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isZh) format.labelZh else format.labelEn,
                                            color = if (isSelected) MeterAmberBright else MeterTextPrimary,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = format.subLabel,
                                            color = MeterTextMuted,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(MeterAmber, RoundedCornerShape(4.dp))
                                        )
                                    }
                                }
                            }
                        }
                        if (rowList.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Composition Grid
            Text(
                text = s.gridGuides,
                color = MeterTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompositionGridStyle.entries.forEach { style ->
                    val isSelected = style == gridStyle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MeterCyan.copy(alpha = 0.15f) else MeterCardBg)
                            .border(1.dp, if (isSelected) MeterCyan else MeterBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelectGridStyle(style) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = style.shortName,
                                color = if (isSelected) MeterCyan else MeterTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isZh) style.labelZh.take(2) else style.labelEn.take(4),
                                color = MeterTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Experimental Multi-Camera / Physical Lens Switching
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MeterCardBg)
                    .border(1.dp, MeterBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isZh) "实验性：多摄像头物理切换" else s.lensSwitch,
                                    color = MeterTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0x3300E5FF), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("EXP", color = MeterCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = if (isZh) "开启后根据焦段自动或手动切换手机超广角/长焦摄像头 (需设备支持)" else "Switch between Ultra-Wide / Main / Telephoto lenses directly",
                                color = MeterTextSecondary,
                                fontSize = 11.sp
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
                            ),
                            modifier = Modifier.testTag("toggle_multi_camera_switch")
                        )
                    }

                    if (isExperimentalMultiCameraEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = s.lensSwitch,
                            color = MeterTextMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CameraLensType.entries.forEach { lens ->
                                val isSelected = lens == selectedCameraLens
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) MeterCyan.copy(alpha = 0.2f) else Color(0x33000000))
                                        .border(1.dp, if (isSelected) MeterCyan else MeterBorder, RoundedCornerShape(6.dp))
                                        .clickable { onSelectCameraLens(lens) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isZh) lens.labelZh.substringBefore(" ") else lens.labelEn.substringBefore(" "),
                                        color = if (isSelected) MeterCyan else MeterTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Color Filter Simulations
            Text(
                text = s.colorFilters,
                color = MeterTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ColorFilterMode.entries) { filter ->
                    val isSelected = filter == selectedColorFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MeterAmber.copy(alpha = 0.2f) else MeterCardBg)
                            .border(1.dp, if (isSelected) MeterAmber else MeterBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelectColorFilter(filter) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (filter.filterColor != Color.Transparent) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(filter.filterColor, RoundedCornerShape(5.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Column {
                                Text(
                                    text = if (isZh) filter.labelZh else filter.labelEn,
                                    color = if (isSelected) MeterAmberBright else MeterTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (filter.filterFactorStops > 0) {
                                    Text(
                                        text = "+${filter.filterFactorStops.toInt()} Stop",
                                        color = MeterAmber,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Custom Focal Length Number Input Dialog
    if (showCustomInputDialog) {
        var inputMmText by remember { mutableStateOf(focalLengthMm.toString()) }

        AlertDialog(
            onDismissRequest = { showCustomInputDialog = false },
            title = {
                Text(if (isZh) "输入自定义焦距 (mm)" else "Custom Focal Length (mm)", color = MeterTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = if (isZh) "请输入任意镜头焦距 (10mm ~ 1200mm 35mm等效)：" else "Enter focal length (10mm ~ 1200mm):",
                        color = MeterTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inputMmText,
                        onValueChange = { inputMmText = it.filter { ch -> ch.isDigit() } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val mm = inputMmText.toIntOrNull()
                                if (mm != null && mm in 10..1200) {
                                    onSelectCustomFocalLength(mm)
                                    showCustomInputDialog = false
                                }
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = MeterBlack,
                            unfocusedContainerColor = MeterBlack,
                            focusedIndicatorColor = MeterAmber,
                            unfocusedIndicatorColor = MeterBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_focal_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mm = inputMmText.toIntOrNull()
                        if (mm != null && mm in 10..1200) {
                            onSelectCustomFocalLength(mm)
                            showCustomInputDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MeterAmber,
                        contentColor = MeterBlack
                    )
                ) {
                    Text(if (isZh) "确认设置" else "Confirm", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomInputDialog = false }) {
                    Text(if (isZh) "取消" else "Cancel", color = MeterTextSecondary)
                }
            },
            containerColor = MeterDarkSurface
        )
    }
}

@Composable
private fun StepperPill(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x33FFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
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

private fun getFocalCategory(focalMm: Int, isZh: Boolean): String {
    return if (isZh) {
        when {
            focalMm < 24 -> "超广角透视"
            focalMm in 24..34 -> "风光/广角"
            focalMm in 35..49 -> "人文纪实"
            focalMm in 50..69 -> "标准标头"
            focalMm in 70..105 -> "人像定焦"
            focalMm in 106..199 -> "中远摄特写"
            else -> "超远摄特写"
        }
    } else {
        when {
            focalMm < 24 -> "Ultra Wide"
            focalMm in 24..34 -> "Wide Angle"
            focalMm in 35..49 -> "Street / Reportage"
            focalMm in 50..69 -> "Standard 50mm"
            focalMm in 70..105 -> "Portrait Prime"
            focalMm in 106..199 -> "Telephoto"
            else -> "Super Telephoto"
        }
    }
}
