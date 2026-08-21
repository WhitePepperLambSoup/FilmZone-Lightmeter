package com.example.meter.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meter.model.MeteringLog
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterBlack
import com.example.ui.theme.MeterBorder
import com.example.ui.theme.MeterCardBg
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterDarkSurface
import com.example.ui.theme.MeterOrange
import com.example.ui.theme.MeterRed
import com.example.ui.theme.MeterTextMuted
import com.example.ui.theme.MeterTextPrimary
import com.example.ui.theme.MeterTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShotLogSheet(
    logs: List<MeteringLog>,
    onDeleteLog: (Long) -> Unit,
    onClearAllLogs: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun copyAllLogsToClipboard() {
        if (logs.isEmpty()) return
        val sb = StringBuilder()
        sb.append("=== 胶片测光手记 (${logs.size} 张) ===\n\n")
        logs.reversed().forEachIndexed { index, item ->
            val frameNum = index + 1
            sb.append("【#${frameNum}】 ${item.filmName} (ISO ${item.iso})\n")
            sb.append("  • 曝光参数: ${item.apertureStr} · ${item.shutterStr}")
            if (item.compensatedShutterStr != item.shutterStr) {
                sb.append(" (倒易率实拍: ${item.compensatedShutterStr})")
            }
            sb.append("\n")
            sb.append("  • 测光值: EV ${String.format(Locale.US, "%.1f", item.currentEv)} (EV100: ${String.format(Locale.US, "%.1f", item.ev100)}) · 定标 Zone ${item.targetZone.roman}\n")
            sb.append("  • 焦距: ${item.focalLengthMm}mm · 时间: ${dateFormat.format(Date(item.timestampMs))}\n")
            if (item.note.isNotBlank()) {
                sb.append("  • 备注: ${item.note}\n")
            }
            sb.append("\n")
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Film Meter Logs", sb.toString())
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "已复制全部手记至剪贴板", Toast.LENGTH_SHORT).show()
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
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Shot Log",
                        tint = MeterAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "底片曝光手记簿 (${logs.size} 张)",
                        color = MeterTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MeterTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Bar: Copy & Clear
            if (logs.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { copyAllLogsToClipboard() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x3300E5FF),
                            contentColor = MeterCyan
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("导出全部手记", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onClearAllLogs() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MeterRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("清空手记", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "暂无曝光记录",
                            color = MeterTextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "在测光界面点击「记录」按钮\n程序将自动记录当前底片的光圈、快门、EV、分区定标与焦距",
                            color = MeterTextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(logs, key = { _, item -> item.id }) { index, item ->
                        val frameNumber = logs.size - index

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MeterCardBg, RoundedCornerShape(8.dp))
                                .border(1.dp, MeterBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                                .testTag("log_item_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Frame number badge
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x33FFA000))
                                            .border(1.dp, MeterAmber, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#$frameNumber",
                                            color = MeterAmberBright,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.filmName,
                                                color = MeterAmberBright,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "ISO ${item.iso}",
                                                color = MeterTextSecondary,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${item.focalLengthMm}mm",
                                                color = MeterCyan,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        Text(
                                            text = "${item.apertureStr}  ·  ${item.shutterStr}  ·  EV ${String.format(Locale.US, "%.1f", item.currentEv)}",
                                            color = MeterTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )

                                        if (item.compensatedShutterStr != item.shutterStr) {
                                            Text(
                                                text = "长曝倒易率补偿实拍: ${item.compensatedShutterStr}",
                                                color = MeterOrange,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Text(
                                            text = "定标: Zone ${item.targetZone.roman} · ${timeFormat.format(Date(item.timestampMs))}",
                                            color = MeterTextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDeleteLog(item.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MeterRed.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
