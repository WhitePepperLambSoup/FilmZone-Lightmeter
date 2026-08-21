package com.example.meter.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meter.calculator.ExposureCalculator
import com.example.meter.model.FilmDatabase
import com.example.meter.model.FilmStock
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciprocityTimerSheet(
    currentFilm: FilmStock,
    meteredShutterSec: Double,
    compensatedShutterSec: Double,
    stopsAdded: Double,
    isTimerRunning: Boolean,
    timerTotalSec: Double,
    timerRemainingSec: Double,
    isTimerCompleted: Boolean,
    onFilmSelected: (FilmStock) -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
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
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Reciprocity",
                        tint = MeterOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "胶片倒易率补偿与长曝计时",
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

            // Big Circular Exposure Timer Display
            val total = if (timerTotalSec > 0) timerTotalSec else compensatedShutterSec
            val remaining = if (timerTotalSec > 0) timerRemainingSec else compensatedShutterSec
            val progress = if (total > 0) (remaining / total).toFloat().coerceIn(0f, 1f) else 1f
            val animatedProgress by animateFloatAsState(targetValue = progress, label = "timer_progress")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circular Timer Arc
                Canvas(modifier = Modifier.size(160.dp)) {
                    val strokeW = 10.dp.toPx()
                    val arcSize = Size(size.width - strokeW, size.height - strokeW)
                    val arcTopLeft = Offset(strokeW / 2, strokeW / 2)

                    // Background track
                    drawArc(
                        color = Color(0xFF242833),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    // Active Countdown Arc
                    val arcColor = if (isTimerCompleted) MeterEmerald else MeterOrange
                    drawArc(
                        color = arcColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                // Inner Time Countdown Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isTimerCompleted) {
                        Text(
                            text = "曝光完成!",
                            color = MeterEmerald,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "可关闭快门",
                            color = MeterTextSecondary,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = ExposureCalculator.formatShutter(remaining),
                            color = MeterOrange,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "测光: ${ExposureCalculator.formatShutter(meteredShutterSec)} (+${String.format(Locale.US, "%.1f", stopsAdded)} EV)",
                            color = MeterTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Timer Controls: Start / Pause / Reset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = onResetTimer,
                    modifier = Modifier
                        .size(46.dp)
                        .background(MeterCardBg, CircleShape)
                        .border(1.dp, MeterBorder, CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = MeterTextPrimary)
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Start/Pause Button
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(if (isTimerRunning) MeterRed else MeterOrange, CircleShape)
                        .clickable {
                            if (isTimerRunning) onPauseTimer() else onStartTimer()
                        }
                        .testTag("timer_play_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = MeterBlack,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Film Stock Selector Title
            Text(
                text = "选择胶卷型号 (自动调用倒易率失效曲线)",
                color = MeterAmber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            // List of Film Stocks
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(FilmDatabase.allFilms) { film ->
                    val isSelected = film.id == currentFilm.id
                    val (compTime, compStops) = ExposureCalculator.calculateReciprocity(film, meteredShutterSec)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) MeterOrange.copy(alpha = 0.15f) else MeterCardBg,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) MeterOrange else MeterBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onFilmSelected(film) }
                            .padding(10.dp)
                            .testTag("film_item_${film.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = film.name,
                                        color = if (isSelected) MeterOrange else MeterTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ISO ${film.defaultIso}",
                                        color = MeterAmberBright,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = film.descriptionZh,
                                    color = MeterTextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }

                            // Compensated time preview
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = ExposureCalculator.formatShutter(compTime),
                                    color = MeterOrange,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (compStops > 0.05) "+${String.format(Locale.US, "%.1f", compStops)} EV" else "无失效",
                                    color = MeterTextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
