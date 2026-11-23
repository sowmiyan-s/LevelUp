package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
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
    onExtendTimer: (Int) -> Unit,
    modifier: Modifier = Modifier
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

            // Central Tactile Arc Dial with 5m to 180m range
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .pointerInput(timerState.status) {
                        if (timerState.status != TimerStatus.IDLE) return@pointerInput
                        detectDragGestures { change, _ ->
                            change.consume()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = change.position.x - center.x
                            val dy = change.position.y - center.y
                            var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                            if (angleDeg < 0f) angleDeg += 360f
                            // Scale 360 deg to 180 minutes (minimum 5m, step 5m)
                            val mins = ((angleDeg / 360f) * 180f).roundToInt().coerceIn(5, 180)
                            val snapped = ((mins + 2) / 5) * 5
                            val finalMins = snapped.coerceIn(5, 180)
                            if (finalMins != timerState.plannedMinutes && hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onSelectMinutes(finalMins)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val inset = strokeWidth / 2f + 8.dp.toPx()
                    val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
                    val topLeft = Offset(inset, inset)

                    // Track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Progress
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner Readout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

            Spacer(Modifier.height(16.dp))

            // Presets and Categories (Idle State)
            if (!isActive) {
                // Radio/Quick selector chips
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
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectMinutes(mins)
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreen.copy(alpha = 0.2f),
                                selectedLabelColor = ForestGreen
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                selectedBorderColor = ForestGreen,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Custom Time button
                OutlinedButton(
                    onClick = { showCustomTimeDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Custom Duration (hr / min)", style = MaterialTheme.typography.labelMedium)
                }

                Spacer(Modifier.height(10.dp))

                // Category Chips
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
                            label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreen.copy(alpha = 0.2f),
                                selectedLabelColor = ForestGreen
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                selectedBorderColor = ForestGreen,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
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
