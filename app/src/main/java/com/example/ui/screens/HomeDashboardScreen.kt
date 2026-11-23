package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.model.DailyGoal
import com.example.model.DateKeyUtils
import com.example.model.TaskStatus
import com.example.model.WorkCategory
import com.example.ui.components.AddBlockedAppDialog
import com.example.ui.components.InteractiveFocusDial
import com.example.ui.components.bioTechGridPattern
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.RoseError
import com.example.util.InstalledAppsHelper
import com.example.viewmodel.DashboardSummary
import com.example.viewmodel.TimerUiState
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeDashboardScreen(
    summary: DashboardSummary,
    timerState: TimerUiState,
    onSelectMinutes: (Int) -> Unit,
    onSelectCategory: (WorkCategory) -> Unit,
    onStartStudyMode: () -> Unit,
    onPauseStudyMode: () -> Unit,
    onStopAndLog: () -> Unit,
    onOpenShieldOverlay: () -> Unit,
    onExtendTimer: (Int) -> Unit,
    onOpenAddGoal: () -> Unit,
    onToggleGoal: (DailyGoal) -> Unit,
    onDeleteGoal: (String) -> Unit,
    onStartTaskStudy: (DailyGoal) -> Unit,
    onUpdateBlockedApps: (List<String>) -> Unit,
    onNavigateToTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val today = DateKeyUtils.todayKey()
    val todayGoals = summary.goals.filter { it.dateKey == today }
    val completedCount = todayGoals.count { it.completed || it.resolvedStatus() == TaskStatus.COMPLETED }
    val haptics = LocalHapticFeedback.current
    var showAddAppDialog by remember { mutableStateOf(false) }

    // Active Task Detection
    val currentMinute = remember {
        val cal = Calendar.getInstance()
        cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }
    val activeTask = remember(todayGoals, currentMinute) {
        todayGoals.firstOrNull { goal ->
            !goal.completed && goal.resolvedStatus() == TaskStatus.PENDING &&
                    currentMinute >= goal.startMinuteOfDay &&
                    currentMinute <= (goal.startMinuteOfDay + goal.targetMinutes)
        } ?: todayGoals.firstOrNull { goal ->
            !goal.completed && goal.resolvedStatus() == TaskStatus.PENDING &&
                    goal.startMinuteOfDay in currentMinute..(currentMinute + 30)
        }
    }

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
            // Header: Clean Date and Streak Pill (no freeze counts)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = DateKeyUtils.formatDateHeader(today),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Daily Focus & Study Command",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Consecutive Day Streak Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AmberStreak.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, AmberStreak.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = AmberStreak,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${summary.currentStreak} Day Streak",
                                style = MaterialTheme.typography.labelMedium,
                                color = AmberStreak,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Active Task Banner Alert
            if (activeTask != null) {
                item {
                    val isRunningNow = currentMinute >= activeTask.startMinuteOfDay &&
                            currentMinute <= (activeTask.startMinuteOfDay + activeTask.targetMinutes)
                    val endMinute = activeTask.startMinuteOfDay + activeTask.targetMinutes

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ForestGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ForestGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bolt,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (isRunningNow) "ACTIVE TASK NOW" else "UPCOMING TASK",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                        Text(
                                            text = "• ${DateKeyUtils.formatMinuteOfDay(activeTask.startMinuteOfDay.toInt())} - ${DateKeyUtils.formatMinuteOfDay(endMinute.toInt())}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = activeTask.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (activeTask.description.isNotBlank()) {
                                        Text(
                                            text = activeTask.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            Button(
                                onClick = { onStartTaskStudy(activeTask) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForestGreen,
                                    contentColor = DarkBackground
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Study Now", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Central Study Mode Dial
            item {
                InteractiveFocusDial(
                    timerState = timerState,
                    todayMinutes = summary.todayFocusMinutes,
                    dailyTargetMinutes = summary.profile.dailyTargetMinutes.toInt(),
                    hapticsEnabled = summary.profile.hapticsEnabled,
                    onSelectMinutes = onSelectMinutes,
                    onSelectCategory = onSelectCategory,
                    onStart = onStartStudyMode,
                    onPause = onPauseStudyMode,
                    onResume = onStartStudyMode,
                    onStopAndLog = onStopAndLog,
                    onOpenShieldOverlay = onOpenShieldOverlay,
                    onExtendTimer = onExtendTimer
                )
            }

            // Today's Tasks Vault Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Today's Tasks",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (todayGoals.isEmpty()) "No tasks scheduled today"
                                    else "$completedCount of ${todayGoals.size} completed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = onOpenAddGoal,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ForestGreen,
                                        contentColor = DarkBackground
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add Task", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        if (todayGoals.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            val completionRatio = completedCount.toFloat() / todayGoals.size.toFloat()
                            LinearProgressIndicator(
                                progress = { completionRatio },
                                color = ForestGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )

                            Spacer(Modifier.height(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                todayGoals.forEach { goal ->
                                    GoalRowCard(
                                        goal = goal,
                                        onToggle = {
                                            if (summary.profile.hapticsEnabled) {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                            onToggleGoal(goal)
                                        },
                                        onDelete = { onDeleteGoal(goal.id) }
                                    )
                                }
                            }
                        } else {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "No tasks set for today.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Button(
                                        onClick = onNavigateToTasks,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ForestGreen.copy(alpha = 0.15f),
                                            contentColor = ForestGreen
                                        ),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Open Schedule", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Study Mode Blocked Apps Quick Card (with Real App Icons)
            item {
                val blockedApps = summary.profile.shieldedApps
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(ForestGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Shield,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Study Mode Lockdown",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${blockedApps.size} apps blocked during sessions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { showAddAppDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForestGreen.copy(alpha = 0.15f),
                                    contentColor = ForestGreen
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        if (blockedApps.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                blockedApps.forEach { appName ->
                                    val appInfo = remember(appName) { InstalledAppsHelper.getAppInfo(context, appName) }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (appInfo.iconBitmap != null) {
                                                Image(
                                                    bitmap = appInfo.iconBitmap,
                                                    contentDescription = appName,
                                                    modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp))
                                                )
                                                Spacer(Modifier.width(6.dp))
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Rounded.Shield,
                                                    contentDescription = null,
                                                    tint = ForestGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                            }
                                            Text(
                                                text = appInfo.appName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            IconButton(
                                                onClick = {
                                                    val updated = blockedApps.filterNot { it.equals(appName, ignoreCase = true) }
                                                    onUpdateBlockedApps(updated)
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Close,
                                                    contentDescription = "Remove $appName",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No apps blocked. Tap '+ Add' above to block social and distracting apps during Study Mode.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddAppDialog) {
        AddBlockedAppDialog(
            currentBlockedApps = summary.profile.shieldedApps,
            onDismiss = { showAddAppDialog = false },
            onAddApp = { newApp ->
                if (summary.profile.shieldedApps.none { it.equals(newApp, ignoreCase = true) }) {
                    onUpdateBlockedApps(summary.profile.shieldedApps + newApp)
                }
            }
        )
    }
}

@Composable
private fun GoalRowCard(
    goal: DailyGoal,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = goal.completed || goal.resolvedStatus() == TaskStatus.COMPLETED

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isCompleted) {
            ForestGreen.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) ForestGreen.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = if (isCompleted) "Completed" else "Pending",
                    tint = if (isCompleted) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${DateKeyUtils.formatMinuteOfDay(goal.startMinuteOfDay.toInt())} • ${goal.targetMinutes}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• ${goal.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ForestGreen
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
