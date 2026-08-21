package com.example.meter.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meter.model.AppLanguage
import com.example.meter.model.AppStrings
import com.example.meter.model.SpotMarker
import com.example.meter.model.ZoneLevel
import com.example.ui.theme.MeterAmber
import com.example.ui.theme.MeterAmberBright
import com.example.ui.theme.MeterBlack
import com.example.ui.theme.MeterBorder
import com.example.ui.theme.MeterCardBg
import com.example.ui.theme.MeterCyan
import com.example.ui.theme.MeterDarkSurface
import com.example.ui.theme.MeterEmerald
import com.example.ui.theme.MeterRed
import com.example.ui.theme.MeterTextMuted
import com.example.ui.theme.MeterTextPrimary
import com.example.ui.theme.MeterTextSecondary
import java.util.Locale
import kotlin.math.abs

@Composable
fun ZoneSystemPanel(
    modifier: Modifier = Modifier,
    selectedZone: ZoneLevel,
    naturalZone: ZoneLevel = ZoneLevel.ZONE_V,
    naturalDeltaEv: Double = 0.0,
    placementShiftEv: Double = 0.0,
    zoneHistogram: IntArray,
    multiSpots: List<SpotMarker>,
    isFalseColor: Boolean,
    appLanguage: AppLanguage = AppLanguage.DEFAULT,
    onZoneSelected: (ZoneLevel) -> Unit,
    onAlignNatural: () -> Unit = {},
    onToggleFalseColor: () -> Unit,
    onOpenZoneGuide: () -> Unit
) {
    val scrollState = rememberScrollState()
    val s = AppStrings.get(appLanguage)
    val isZh = appLanguage == AppLanguage.SIMPLIFIED_CHINESE || appLanguage == AppLanguage.TRADITIONAL_CHINESE

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MeterDarkSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Header with active Zone summary & quick action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tonality,
                    contentDescription = "Zone System",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = s.zoneSystemTitle,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Align with natural detected zone button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selectedZone == naturalZone) Color(0xFF0F2D1F) else Color(0xFF1E293B))
                        .border(1.dp, if (selectedZone == naturalZone) MeterEmerald else MeterCyan, RoundedCornerShape(50))
                        .clickable { onAlignNatural() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("align_natural_zone_btn")
                ) {
                    Text(
                        text = if (selectedZone == naturalZone) String.format(s.matchedNaturalZone, naturalZone.roman) else String.format(s.alignNaturalZone, naturalZone.roman),
                        color = if (selectedZone == naturalZone) MeterEmerald else MeterCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // False Color Map Toggle
                Box(
                    modifier = Modifier
                        .background(
                            if (isFalseColor) com.example.ui.theme.MeterAccentPrimary else MeterCardBg,
                            RoundedCornerShape(50)
                        )
                        .border(
                            1.dp,
                            if (isFalseColor) com.example.ui.theme.MeterAccentPrimary else com.example.ui.theme.MeterBorderSubtle,
                            RoundedCornerShape(50)
                        )
                        .clickable { onToggleFalseColor() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("toggle_false_color_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "False Color",
                            tint = if (isFalseColor) com.example.ui.theme.MeterAccentOnPrimary else MeterTextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFalseColor) "${s.falseColor}: ON" else "${s.falseColor}: OFF",
                            color = if (isFalseColor) com.example.ui.theme.MeterAccentOnPrimary else MeterTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Guide Info Icon
                IconButton(
                    onClick = onOpenZoneGuide,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Zone Guide",
                        tint = MeterTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 11-Zone Horizontal Interactive Selector Bar (Zone 0 to Zone X)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val totalHist = maxOf(zoneHistogram.sum(), 1)

            ZoneLevel.entries.forEach { zone ->
                val isSelected = zone == selectedZone
                val isNatural = zone == naturalZone
                val histCount = zoneHistogram.getOrNull(zone.index) ?: 0
                val histHeightRatio = (histCount.toFloat() / totalHist * 3.5f).coerceIn(0.1f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(36.dp)
                        .clickable { onZoneSelected(zone) }
                        .testTag("zone_button_${zone.index}")
                ) {
                    // Zone Preview Block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .background(
                                zone.previewColor,
                                RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                            .border(
                                width = if (isSelected) 2.dp else if (isNatural) 1.2.dp else 0.8.dp,
                                color = if (isSelected) MeterAmberBright else if (isNatural) MeterCyan else com.example.ui.theme.MeterBorderSubtle,
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = zone.roman,
                            color = if (zone.index in 0..4) Color.White else Color.Black,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Mini Tone Histogram Distribution Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(MeterCardBg, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(histHeightRatio)
                                .height(6.dp)
                                .background(
                                    if (isSelected) MeterAmberBright else if (isNatural) MeterCyan else Color(0x66FFFFFF),
                                    RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)
                                )
                        )
                    }

                    // Relative EV Stop Tag
                    Text(
                        text = if (zone.relativeEv > 0) "+${zone.relativeEv.toInt()}" else if (zone.relativeEv == 0.0) "0" else "${zone.relativeEv.toInt()}",
                        color = if (isSelected) MeterAmberBright else if (isNatural) MeterCyan else MeterTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected || isNatural) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Active Zone Explanation Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .background(MeterCardBg, RoundedCornerShape(10.dp))
                .border(1.dp, com.example.ui.theme.MeterBorderSubtle, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isZh) "当前定标: Zone ${selectedZone.roman} [${selectedZone.titleZh}]" else "Target Zone: Zone ${selectedZone.roman} [${selectedZone.titleEn}]",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val deltaFromNatural = naturalDeltaEv - selectedZone.relativeEv
                        Text(
                            text = if (abs(deltaFromNatural) < 0.15) (if (isZh) "实测匹配 · 0 EV 变化" else "Matched · 0 EV Shift")
                                   else if (deltaFromNatural > 0) String.format(Locale.US, if (isZh) "整体压暗 -%.1f EV" else "Darken -%.1f EV", deltaFromNatural) 
                                   else String.format(Locale.US, if (isZh) "整体提亮 +%.1f EV" else "Brighten +%.1f EV", -deltaFromNatural),
                            color = if (abs(deltaFromNatural) < 0.15) MeterEmerald else if (deltaFromNatural > 0) MeterCyan else MeterAmberBright,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val zoneTitle = if (isZh) selectedZone.descriptionZh else selectedZone.descriptionEn
                    Text(
                        text = if (isZh) "实测自然归属: Zone ${naturalZone.roman} (${if (naturalDeltaEv > 0) "+${String.format(Locale.US, "%.1f", naturalDeltaEv)}" else String.format(Locale.US, "%.1f", naturalDeltaEv)} EV) · $zoneTitle"
                               else "Natural Reading: Zone ${naturalZone.roman} (${if (naturalDeltaEv > 0) "+${String.format(Locale.US, "%.1f", naturalDeltaEv)}" else String.format(Locale.US, "%.1f", naturalDeltaEv)} EV) · $zoneTitle",
                        color = MeterTextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Multi-spot Contrast & Dynamic Range Indicator (if multiple spots are metered)
        if (multiSpots.size >= 2) {
            val minEvSpot = multiSpots.minByOrNull { it.measuredEv100 }
            val maxEvSpot = multiSpots.maxByOrNull { it.measuredEv100 }
            if (minEvSpot != null && maxEvSpot != null) {
                val contrastStops = abs(maxEvSpot.measuredEv100 - minEvSpot.measuredEv100)
                val devAdvice = if (isZh) {
                    when {
                        contrastStops < 4.0 -> "低反差场景 (推荐 N+1 增感迫冲显影)"
                        contrastStops <= 6.5 -> "标准反差 (推荐 N 正常显影)"
                        else -> "大光比场景 (推荐 N-1 / N-2 压光减感显影)"
                    }
                } else {
                    when {
                        contrastStops < 4.0 -> "Low Contrast Scene (Recommend N+1 Push Development)"
                        contrastStops <= 6.5 -> "Normal Contrast (Recommend N Standard Development)"
                        else -> "High Dynamic Range (Recommend N-1 / N-2 Pull Development)"
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .background(Color(0xFF0C4A6E).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .border(1.dp, MeterCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format(Locale.US, if (isZh) "光比跨度: %.1f 挡 (%s ↔ %s)" else "Range: %.1f Stops (%s ↔ %s)", contrastStops, minEvSpot.assignedZone.roman, maxEvSpot.assignedZone.roman),
                            color = MeterCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = devAdvice,
                            color = MeterTextPrimary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
