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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meter.model.CameraLensType
import com.example.meter.model.MeteringMode
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
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    calibrationOffset: Double,
    meteringMode: MeteringMode,
    isExperimentalMultiCameraEnabled: Boolean,
    onSetCalibrationOffset: (Double) -> Unit,
    onSetMeteringMode: (MeteringMode) -> Unit,
    onToggleMultiCamera: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MeterAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "测光表校准与高级设置",
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

            // 1. Calibration Offset Slider Card
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
                                text = "摄像头测光硬件基准微调",
                                color = MeterTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "已自动集成 Lightme / 苹果测光表与 ISO 2720 校准",
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
                        value = calibrationOffset.toFloat().coerceIn(-4.0f, 4.0f),
                        onValueChange = { 
                            val rounded = (it * 10).roundToInt() / 10.0
                            onSetCalibrationOffset(rounded) 
                        },
                        valueRange = -4.0f..4.0f,
                        steps = 79, // 0.1 step
                        colors = SliderDefaults.colors(
                            thumbColor = MeterAmberBright,
                            activeTrackColor = MeterAmber,
                            inactiveTrackColor = MeterBorder
                        ),
                        modifier = Modifier.testTag("calibration_slider")
                    )

                    // Quick Stepper and Calibration Presets Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CalibPill(label = "-0.5") { onSetCalibrationOffset((calibrationOffset - 0.5).coerceIn(-4.0, 4.0)) }
                            CalibPill(label = "-0.1") { onSetCalibrationOffset((calibrationOffset - 0.1).coerceIn(-4.0, 4.0)) }
                        }

                        Text(
                            text = "重置 0.0",
                            color = MeterCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onSetCalibrationOffset(0.0) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CalibPill(label = "+0.1") { onSetCalibrationOffset((calibrationOffset + 0.1).coerceIn(-4.0, 4.0)) }
                            CalibPill(label = "+0.5") { onSetCalibrationOffset((calibrationOffset + 0.5).coerceIn(-4.0, 4.0)) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Experimental Multi-Camera Toggle in Settings
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
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "实验性：多摄像头物理切换",
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
                                Text("实验", color = MeterCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "在支持多后置摄像头的设备上自动或手动切换物理镜头 (0.6x/1x/3x)",
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
                        modifier = Modifier.testTag("settings_multi_camera_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Metering Mode Selection
            Text(
                text = "测光采样算法",
                color = MeterAmber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MeteringMode.entries.forEach { mode ->
                    val isSelected = meteringMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MeterAmber.copy(alpha = 0.2f) else MeterCardBg
                            )
                            .border(
                                1.dp,
                                if (isSelected) MeterAmber else MeterBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSetMeteringMode(mode) }
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = mode.shortName,
                                color = if (isSelected) MeterAmberBright else MeterTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mode.labelZh,
                                color = MeterTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calibration & Accuracy tips
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MeterCardBg)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "测光基准与物理模型说明：",
                        color = MeterTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• 本测光表已根据 ISO 2720 / ISO 12232 标准及苹果 Lightme 测光算法完成出厂基准标定 (+2.0 EV 手机 ISP 增益逆映射)。\n• 取景帧亮度采用 Gamma 2.2 线性反变换精确求解物理反射率。\n• Ansel Adams 分区曝光支持 Zone 0 ～ Zone X 十一级影调实时定标与灰卡补偿。",
                        color = MeterTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CalibPill(label: String, onClick: () -> Unit) {
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
