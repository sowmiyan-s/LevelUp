package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DailyGoal
import com.example.model.DateKeyUtils
import com.example.model.GoalTier
import com.example.model.TaskStatus
import com.example.model.WorkSession
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.RescheduleTaskDialog
import com.example.ui.components.bioTechGridPattern
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.RoseError
import com.example.ui.theme.SkyFreeze
import com.example.viewmodel.DashboardSummary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TasksScreen(
    summary: DashboardSummary,
    onAddTask: (title: String, description: String, durationMinutes: Int, startMinute: Int, category: String, tier: GoalTier) -> Unit,
    onUpdateTaskStatus: (taskId: String, status: TaskStatus) -> Unit,
    onRescheduleTask: (taskId: String, newDateKey: String, newStartMinute: Int) -> Unit,
    onDeleteTask: (taskId: String) -> Unit,
    onStartStudyWithTask: (DailyGoal) -> Unit,
    onOpenAiPlanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDateKey by remember { mutableStateOf(DateKeyUtils.todayKey()) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToReschedule by remember { mutableStateOf<DailyGoal?>(null) }
    val haptics = LocalHapticFeedback.current

    val dayTasks = remember(summary.goals, selectedDateKey) {
        summary.goals
            .filter { it.dateKey == selectedDateKey }
            .sortedBy { it.startMinuteOfDay }
    }

    val completedCount = dayTasks.count { it.completed || it.resolvedStatus() == TaskStatus.COMPLETED }
    val failedCount = dayTasks.count { it.resolvedStatus() == TaskStatus.FAILED }
    val pendingCount = dayTasks.count { it.resolvedStatus() == TaskStatus.PENDING && !it.completed }

    Box(
        modifier = modifier
            .fillMaxSize()
            .bioTechGridPattern(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title & Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tasks Schedule",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Plan, track, and complete daily study blocks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // AI Planner Button
                        Button(
                            onClick = onOpenAiPlanner,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreen.copy(alpha = 0.18f),
                                contentColor = ForestGreen
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Plan with AI", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }

                        // Add Task Button
                        Button(
                            onClick = { showAddTaskDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreen,
                                contentColor = DarkBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Task", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Date Navigation Strip
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { selectedDateKey = DateKeyUtils.shiftDays(selectedDateKey, -1) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = "Previous day",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = DateKeyUtils.formatDateHeader(selectedDateKey),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (selectedDateKey != DateKeyUtils.todayKey()) {
                                Text(
                                    text = "Tap to jump to today",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ForestGreen,
                                    modifier = Modifier.clickable { selectedDateKey = DateKeyUtils.todayKey() }
                                )
                            }
                        }

                        IconButton(
                            onClick = { selectedDateKey = DateKeyUtils.shiftDays(selectedDateKey, 1) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = "Next day",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Daily Stats Strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskMetricCard("Total", "${dayTasks.size}", MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
                    TaskMetricCard("Completed", "$completedCount", ForestGreen, Modifier.weight(1f))
                    TaskMetricCard("Pending", "$pendingCount", AmberStreak, Modifier.weight(1f))
                    TaskMetricCard("Failed", "$failedCount", RoseError, Modifier.weight(1f))
                }
            }

            // Tasks List
            if (dayTasks.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(36.dp))
                            Text(
                                text = "No tasks scheduled for this day",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Add tasks manually or let AI Plan Architect create an optimized study roadmap.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onOpenAiPlanner,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = DarkBackground)
                                ) {
                                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Plan with AI", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { showAddTaskDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Add Manually")
                                }
                            }
                        }
                    }
                }
            } else {
                items(dayTasks) { task ->
                    TaskScheduleCard(
                        task = task,
                        onMarkComplete = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onUpdateTaskStatus(task.id, TaskStatus.COMPLETED)
                        },
                        onMarkFailed = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onUpdateTaskStatus(task.id, TaskStatus.FAILED)
                        },
                        onReschedule = { taskToReschedule = task },
                        onDelete = { onDeleteTask(task.id) },
                        onStartStudy = { onStartStudyWithTask(task) }
                    )
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            initialDateKey = selectedDateKey,
            onDismiss = { showAddTaskDialog = false },
            onSave = { title, description, durationMinutes, startMinute, category, tier ->
                onAddTask(title, description, durationMinutes, startMinute, category, tier)
                showAddTaskDialog = false
            }
        )
    }

    val rescheduleTarget = taskToReschedule
    if (rescheduleTarget != null) {
        RescheduleTaskDialog(
            task = rescheduleTarget,
            onDismiss = { taskToReschedule = null },
            onReschedule = { newDateKey, newStartMinute ->
                onRescheduleTask(rescheduleTarget.id, newDateKey, newStartMinute)
                taskToReschedule = null
            }
        )
    }
}

@Composable
private fun TaskMetricCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskScheduleCard(
    task: DailyGoal,
    onMarkComplete: () -> Unit,
    onMarkFailed: () -> Unit,
    onReschedule: () -> Unit,
    onDelete: () -> Unit,
    onStartStudy: () -> Unit
) {
    val status = task.resolvedStatus()
    val isDone = status == TaskStatus.COMPLETED || task.completed
    val isFailed = status == TaskStatus.FAILED
    val isRescheduled = status == TaskStatus.RESCHEDULED
    val isPending = status == TaskStatus.PENDING && !task.completed

    val endMinute = task.startMinuteOfDay + task.targetMinutes

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = when {
                isDone -> ForestGreen.copy(alpha = 0.5f)
                isFailed -> RoseError.copy(alpha = 0.5f)
                isRescheduled -> SkyFreeze.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Time Badge, Category, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Time Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "${DateKeyUtils.formatMinuteOfDay(task.startMinuteOfDay.toInt())} - ${DateKeyUtils.formatMinuteOfDay(endMinute.toInt())} (${task.targetMinutes}m)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = "• ${task.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ForestGreen,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Status Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        isDone -> ForestGreen.copy(alpha = 0.15f)
                        isFailed -> RoseError.copy(alpha = 0.15f)
                        isRescheduled -> SkyFreeze.copy(alpha = 0.15f)
                        else -> AmberStreak.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = when {
                            isDone -> "✓ Completed"
                            isFailed -> "✕ Failed"
                            isRescheduled -> "↺ Rescheduled"
                            else -> "• Pending"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isDone -> ForestGreen
                            isFailed -> RoseError
                            isRescheduled -> SkyFreeze
                            else -> AmberStreak
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Title & Description
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (task.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isPending) {
                        // Mark Complete
                        Button(
                            onClick = onMarkComplete,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreen,
                                contentColor = DarkBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Done", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        // Mark Failed
                        OutlinedButton(
                            onClick = onMarkFailed,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, RoseError.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = null, tint = RoseError, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Fail", color = RoseError, style = MaterialTheme.typography.labelSmall)
                        }

                        // Reschedule
                        OutlinedButton(
                            onClick = onReschedule,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Rounded.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Reschedule", style = MaterialTheme.typography.labelSmall)
                        }

                        // Study Now
                        Button(
                            onClick = onStartStudy,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreen.copy(alpha = 0.15f),
                                contentColor = ForestGreen
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("Study", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    } else if (isDone || isFailed) {
                        TextButton(
                            onClick = onReschedule,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Rounded.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Reschedule", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
