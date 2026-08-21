package com.example.meter.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meter.model.AppLanguage
import com.example.meter.model.AppStrings
import com.example.meter.model.ZoneLevel
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterBorder
import com.example.ui.theme.MeterCardBg
import com.example.ui.theme.MeterDarkSurface
import com.example.ui.theme.MeterTextPrimary
import com.example.ui.theme.MeterTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneGuideSheet(
    appLanguage: AppLanguage = AppLanguage.DEFAULT,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isZh = appLanguage == AppLanguage.SIMPLIFIED_CHINESE || appLanguage == AppLanguage.TRADITIONAL_CHINESE

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
                        imageVector = Icons.Default.Tonality,
                        contentDescription = "Zone System Guide",
                        tint = MeterAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "安塞尔·亚当斯 分区曝光指南" else "Ansel Adams Zone System Guide",
                        color = MeterTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MeterTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Photography Axiom Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MeterCardBg, RoundedCornerShape(8.dp))
                    .border(1.dp, MeterAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = if (isZh) "“针对阴影曝光，针对高光显影”" else "\"Expose for the shadows, develop for the highlights\"",
                        color = MeterAmberBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isZh) {
                            "分区系统将黑白与彩色摄影影调划分为11个区域(Zone 0 至 Zone X)。测光表默认将测光点还原为Zone V(18%中灰)。通过在测光表中指定测光点所在的Zone，可精准控制曝光参数。"
                        } else {
                            "The Zone System divides photographic tones into 11 zones (Zone 0 to Zone X). By default, meters assume middle gray (Zone V). Placing your meter spot on a target zone shifts exposure accordingly."
                        },
                        color = MeterTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 11-Zone detailed list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(ZoneLevel.entries) { zone ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MeterCardBg, RoundedCornerShape(8.dp))
                            .border(1.dp, MeterBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Tone Box
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(zone.previewColor, RoundedCornerShape(6.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = zone.roman,
                                    color = if (zone.index in 0..4) Color.White else Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Zone ${zone.roman}: ${if (isZh) zone.titleZh else zone.titleEn}",
                                        color = MeterTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (zone.relativeEv > 0) "+${zone.relativeEv.toInt()} EV" else if (zone.relativeEv == 0.0) "0 EV" else "${zone.relativeEv.toInt()} EV",
                                        color = MeterAmber,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = if (isZh) zone.descriptionZh else zone.descriptionEn,
                                    color = MeterTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
