package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.DateKeyUtils
import com.example.model.WorkCategory
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.RoseError
import com.example.viewmodel.TimerStatus
import com.example.viewmodel.TimerUiState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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
    onExtendTimer: (Int) -> Unit,
    modifier: Modifier = Modifier,
    linkedGoalTitle: String? = null
) {
    val haptics = LocalHapticFeedback.current
    val isActive = timerState.status != TimerStatus.IDLE
    val plannedSeconds = (timerState.plannedMinutes * 60).coerceAtLeast(1)
    var showCustomTimeDialog by remember { mutableStateOf(false) }

    // Progress maps up to 180 minutes in idle, or elapsed/planned when active
    val dialProgress = if (isActive) {
        (timerState.elapsedSeconds.toFloat() / plannedSeconds.toFloat()).coerceIn(0.02f, 1f)
    } else {
        (timerState.plannedMinutes.toFloat() / 180f).coerceIn(0.03f, 1f)
    }
    val animatedProgress by animateFloatAsState(
        targetValue = dialProgress,
        animationSpec = spring(),
        label = "dialProgress"
    )

    // Breathing Ambient Glow Pulse (21st.dev kinetic design)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (timerState.status == TimerStatus.RUNNING) 0.15f else 0.05f,
        targetValue = if (timerState.status == TimerStatus.RUNNING) 0.38f else 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseRadiusFactor by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val progressColor = if (timerState.status == TimerStatus.PAUSED) AmberStreak else ForestGreen

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isActive) 4.dp else 1.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) ForestGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Status Indicator + Target Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (timerState.status) {
                                    TimerStatus.RUNNING -> ForestGreen
                                    TimerStatus.PAUSED -> AmberStreak
                                    TimerStatus.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                }
                            )
                    )
                    Text(
                        text = when (timerState.status) {
                            TimerStatus.RUNNING -> "Study Mode Active"
                            TimerStatus.PAUSED -> "Study Mode Paused"
                            TimerStatus.IDLE -> "Study Mode Timer"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = when (timerState.status) {
                            TimerStatus.RUNNING -> ForestGreen
                            TimerStatus.PAUSED -> AmberStreak
                            TimerStatus.IDLE -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "${DateKeyUtils.formatDurationMinutes(todayMinutes)} / ${DateKeyUtils.formatDurationMinutes(dailyTargetMinutes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Central Tactile Arc Dial with 5m to 180m range (270° Sweep Gauge)
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
                            var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angleDeg < 0f) angleDeg += 360f

                            // 270 deg sweep starts at 135 deg and ends at 405 deg (45 deg)
                            val shiftedAngle = (angleDeg - 135f).let { if (it < 0f) it + 360f else it }
                            if (shiftedAngle <= 270f) {
                                val fraction = (shiftedAngle / 270f).coerceIn(0.02f, 1f)
                                val mins = (fraction * 180f).roundToInt().coerceIn(5, 180)
                                val snapped = ((mins + 2) / 5) * 5
                                val finalMins = snapped.coerceIn(5, 180)
                                if (finalMins != timerState.plannedMinutes && hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onSelectMinutes(finalMins)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val inset = strokeWidth / 2f + 10.dp.toPx()
                    val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
                    val topLeft = Offset(inset, inset)
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val center = Offset(centerX, centerY)
                    val arcRadius = (size.width - inset * 2f) / 2f

                    // Ethereal Breathing Aura Glow (when timer is running)
                    if (timerState.status == TimerStatus.RUNNING) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    progressColor.copy(alpha = pulseAlpha),
                                    progressColor.copy(alpha = pulseAlpha * 0.3f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = (size.width / 2f) * pulseRadiusFactor
                            ),
                            radius = (size.width / 2f) * pulseRadiusFactor,
                            center = center
                        )
                    }

                    // 270° Recessed Track (starts at 135°, sweeps 270°)
                    drawArc(
                        color = trackColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Swiss Precision Chronometer Tick Marks (18 ticks along 270° sweep)
                    for (tickIdx in 0..18) {
                        val tickAngleDeg = 135f + tickIdx * (270f / 18f)
                        val tickAngleRad = Math.toRadians(tickAngleDeg.toDouble())
                        val isMajor = tickIdx % 3 == 0
                        val tickLen = if (isMajor) 7.dp.toPx() else 3.5.dp.toPx()
                        val rOuter = arcRadius - strokeWidth / 2f - 4.dp.toPx()
                        val rInner = rOuter - tickLen

                        val startX = centerX + rOuter * cos(tickAngleRad).toFloat()
                        val startY = centerY + rOuter * sin(tickAngleRad).toFloat()
                        val endX = centerX + rInner * cos(tickAngleRad).toFloat()
                        val endY = centerY + rInner * sin(tickAngleRad).toFloat()

                        val tickFraction = tickIdx.toFloat() / 18f
                        val tickPassed = tickFraction <= animatedProgress
                        drawLine(
                            color = if (tickPassed) progressColor.copy(alpha = 0.75f) else trackColor.copy(alpha = 0.5f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (isMajor) 2.dp.toPx() else 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Active Progress Arc
                    val activeSweep = (animatedProgress * 270f).coerceIn(2f, 270f)
                    drawArc(
                        color = progressColor,
                        startAngle = 135f,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Glowing Terminator Knob
                    val knobAngleDeg = 135f + activeSweep
                    val knobAngleRad = Math.toRadians(knobAngleDeg.toDouble())
                    val knobX = centerX + arcRadius * cos(knobAngleRad).toFloat()
                    val knobY = centerY + arcRadius * sin(knobAngleRad).toFloat()
                    val knobCenter = Offset(knobX, knobY)

                    // Outer halo glow
                    drawCircle(
                        color = progressColor.copy(alpha = 0.35f),
                        radius = strokeWidth * 0.95f,
                        center = knobCenter
                    )
                    // Knob body
                    drawCircle(
                        color = progressColor,
                        radius = strokeWidth * 0.65f,
                        center = knobCenter
                    )
                    // Inner core dot
                    drawCircle(
                        color = Color.White,
                        radius = strokeWidth * 0.25f,
                        center = knobCenter
                    )
                }

                // Inner HUD Readout
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Session State Status Chip
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (timerState.status) {
                                            TimerStatus.RUNNING -> ForestGreen
                                            TimerStatus.PAUSED -> AmberStreak
                                            TimerStatus.IDLE -> ForestGreen
                                        }
                                    )
                            )
                            Text(
                                text = when (timerState.status) {
                                    TimerStatus.RUNNING -> "FOCUSING"
                                    TimerStatus.PAUSED -> "PAUSED"
                                    TimerStatus.IDLE -> "READY"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (timerState.status) {
                                    TimerStatus.RUNNING -> ForestGreen
                                    TimerStatus.PAUSED -> AmberStreak
                                    TimerStatus.IDLE -> ForestGreen
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    val displayText = if (isActive) {
                        DateKeyUtils.formatTimerSeconds(timerState.elapsedSeconds)
                    } else {
                        val hrs = timerState.plannedMinutes / 60
                        val mins = timerState.plannedMinutes % 60
                        if (hrs > 0 && mins > 0) "${hrs}h ${mins}m"
                        else if (hrs > 0) "${hrs}h"
                        else "${mins}m"
                    }
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))

                    if (!linkedGoalTitle.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = ForestGreen)
                                Text(
                                    text = linkedGoalTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (isActive) {
                                "${timerState.selectedCategory.label} • Target ${timerState.plannedMinutes}m"
                            } else {
                                "Drag dial • Range 5m to 3h"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Presets and Categories (Idle State)
            if (!isActive) {
                // Preset Sprints Header Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRESET SPRINTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Selected: ${timerState.plannedMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = ForestGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Presets Flex row with Custom chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(15, 25, 45, 60, 90, 120).forEach { mins ->
                        val selected = timerState.plannedMinutes == mins
                        val label = if (mins >= 60) {
                            if (mins % 60 == 0) "${mins / 60}h" else "${mins / 60}h ${mins % 60}m"
                        } else "${mins}m"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) ForestGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (selected) ForestGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectMinutes(mins)
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) DarkBackground else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Custom Button chip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { showCustomTimeDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Custom", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Category Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WorkCategory.entries.forEach { cat ->
                        val selected = timerState.selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) ForestGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, if (selected) ForestGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier.clickable { onSelectCategory(cat) }
                        ) {
                            Text(
                                text = cat.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStart()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = DarkBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("start_study_mode_button")
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Start Study Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Active Timer: Extend Timer options (+5m, +15m, +30m)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Extend:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    listOf(5, 15, 30).forEach { extMins ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onExtendTimer(extMins)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = ForestGreen)
                                Spacer(Modifier.width(2.dp))
                                Text("${extMins}m", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Pause/Resume & Finish & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (timerState.status == TimerStatus.RUNNING) onPause() else onResume()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pause_resume_button")
                    ) {
                        Icon(
                            imageVector = if (timerState.status == TimerStatus.RUNNING) {
                                Icons.Rounded.Pause
                            } else {
                                Icons.Rounded.PlayArrow
                            },
                            contentDescription = null,
                            tint = if (timerState.status == TimerStatus.RUNNING) AmberStreak else ForestGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (timerState.status == TimerStatus.RUNNING) "Pause" else "Resume",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = {
                            if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStopAndLog()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestGreen,
                            contentColor = DarkBackground
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("stop_and_log_button")
                    ) {
                        Icon(Icons.Rounded.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Finish & Save",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onOpenShieldOverlay,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ForestGreen
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("open_shield_overlay_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fullscreen,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Zen Fullscreen Mode",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    if (showCustomTimeDialog) {
        CustomTimerDialog(
            currentMinutes = timerState.plannedMinutes,
            onDismiss = { showCustomTimeDialog = false },
            onConfirm = { mins ->
                onSelectMinutes(mins)
                showCustomTimeDialog = false
            }
        )
    }
}

@Composable
fun CustomTimerDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(currentMinutes / 60) }
    var minutes by remember { mutableIntStateOf(currentMinutes % 60) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set Custom Timer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Configure custom study time from 5 minutes to 3 hours (180 min).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Hours Slider
                Text(
                    text = "Hours: $hours hr",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = hours.toFloat(),
                    onValueChange = { hours = it.roundToInt().coerceIn(0, 3) },
                    valueRange = 0f..3f,
                    steps = 2,
                    colors = SliderDefaults.colors(thumbColor = ForestGreen, activeTrackColor = ForestGreen)
                )

                // Minutes Slider
                Text(
                    text = "Minutes: $minutes min",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = minutes.toFloat(),
                    onValueChange = { minutes = (it / 5).roundToInt() * 5 },
                    valueRange = 0f..55f,
                    steps = 10,
                    colors = SliderDefaults.colors(thumbColor = ForestGreen, activeTrackColor = ForestGreen)
                )

                val calculatedTotal = (hours * 60 + minutes).coerceIn(5, 180)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ForestGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Total Study Duration: $calculatedTotal minutes",
                        style = MaterialTheme.typography.titleSmall,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(calculatedTotal) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestGreen,
                            contentColor = DarkBackground
                        )
                    ) {
                        Text("Apply Timer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
