package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DateKeyUtils
import com.example.model.WorkCategory
import com.example.ui.theme.Amber500
import com.example.ui.theme.Crimson500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Mint200
import com.example.ui.theme.Mint400
import com.example.viewmodel.TimerStatus
import com.example.viewmodel.TimerUiState
import kotlin.math.atan2
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveFocusDial(
    timerState: TimerUiState,
    todayMinutes: Int,
    dailyTargetMinutes: Int,
    hapticsEnabled: Boolean,
    onSelectMinutes: (Int) -> Unit,
    onSelectCategory: (WorkCategory) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStopAndLog: () -> Unit,
    onOpenShieldOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val isActive = timerState.status != TimerStatus.IDLE
    val plannedSeconds = (timerState.plannedMinutes * 60).coerceAtLeast(1)

    val dialProgress = if (isActive) {
        (timerState.elapsedSeconds.toFloat() / plannedSeconds.toFloat()).coerceIn(0.03f, 1f)
    } else {
        (timerState.plannedMinutes.toFloat() / 120f).coerceIn(0.08f, 1f)
    }
    val animatedProgress by animateFloatAsState(
        targetValue = dialProgress,
        animationSpec = spring(),
        label = "dialProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val trackColor by animateColorAsState(
        targetValue = if (isActive) Emerald700.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        label = "trackColor"
    )

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) Mint400.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (timerState.status == TimerStatus.RUNNING) {
                        Emerald500.copy(alpha = 0.16f)
                    } else if (timerState.status == TimerStatus.PAUSED) {
                        Amber500.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (timerState.status) {
                                        TimerStatus.RUNNING -> Mint400.copy(alpha = pulseAlpha + 0.35f)
                                        TimerStatus.PAUSED -> Amber500
                                        TimerStatus.IDLE -> MaterialTheme.colorScheme.primary
                                    }
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when (timerState.status) {
                                TimerStatus.RUNNING -> "FOCUS SHIELD ACTIVE"
                                TimerStatus.PAUSED -> "SESSION PAUSED"
                                TimerStatus.IDLE -> "STUDY MODE READY"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (timerState.status) {
                                TimerStatus.RUNNING -> Mint400
                                TimerStatus.PAUSED -> Amber500
                                TimerStatus.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Text(
                    text = "Today ${DateKeyUtils.formatDurationMinutes(todayMinutes)} / ${DateKeyUtils.formatDurationMinutes(dailyTargetMinutes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(18.dp))

            // Central Interactive Ring Dial
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .pointerInput(timerState.status) {
                        if (timerState.status != TimerStatus.IDLE) return@pointerInput
                        detectDragGestures { change, _ ->
                            change.consume()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = change.position.x - center.x
                            val dy = change.position.y - center.y
                            var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                            if (angleDeg < 0f) angleDeg += 360f
                            val mins = ((angleDeg / 360f) * 120f).roundToInt().coerceIn(5, 120)
                            val snapped = ((mins + 2) / 5) * 5
                            onSelectMinutes(snapped.coerceIn(5, 120))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 18.dp.toPx()
                    val inset = strokeWidth / 2f + 12.dp.toPx()
                    val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
                    val topLeft = Offset(inset, inset)

                    // Subtle outer halo glow when active
                    if (isActive) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Mint400.copy(alpha = 0.18f * pulseAlpha + 0.08f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension / 2f
                        )
                    }

                    // Background Track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Sweep Gradient Ring
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Emerald500,
                                Mint400,
                                Emerald600,
                                Mint400,
                                Emerald500
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Dial Inner Readout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isActive) {
                            DateKeyUtils.formatTimerSeconds(timerState.elapsedSeconds)
                        } else {
                            "${timerState.plannedMinutes}m"
                        },
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isActive) {
                            "Target ${timerState.plannedMinutes}m • ${timerState.selectedCategory.label}"
                        } else {
                            "Drag ring or tap preset"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Quick Duration Presets + Category Selector (Idle only)
            if (!isActive) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(25, 45, 60, 90).forEach { mins ->
                        val selected = timerState.plannedMinutes == mins
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectMinutes(mins)
                            },
                            label = { Text("${mins}m") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald500.copy(alpha = 0.22f),
                                selectedLabelColor = Mint400
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WorkCategory.entries.forEach { cat ->
                        val selected = timerState.selectedCategory == cat
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectCategory(cat) },
                            label = { Text(cat.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStart()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald500,
                        contentColor = Emerald950
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_study_mode_button")
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "START STUDY MODE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Active Controls: Pause/Resume & Stop & Log
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (timerState.status == TimerStatus.RUNNING) onPause() else onResume()
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Mint400.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("pause_resume_button")
                    ) {
                        Icon(
                            imageVector = if (timerState.status == TimerStatus.RUNNING) {
                                Icons.Rounded.Pause
                            } else {
                                Icons.Rounded.PlayArrow
                            },
                            contentDescription = null,
                            tint = Mint400
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (timerState.status == TimerStatus.RUNNING) "PAUSE" else "RESUME",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = {
                            if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStopAndLog()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = Emerald950
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("stop_and_log_button")
                    ) {
                        Icon(Icons.Rounded.Stop, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("STOP & LOG", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onOpenShieldOverlay,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Crimson500.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_shield_overlay_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = Mint400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Enter Immersive Focus Shield",
                        color = Mint200
                    )
                }
            }
        }
    }
}
