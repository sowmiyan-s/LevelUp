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
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.model.AiCoachState
import com.example.model.AiPlanSuggestion
import com.example.model.AiSuggestedGoal
import com.example.model.DailyGoal
import com.example.model.DateKeyUtils
import com.example.model.GoalTier
import com.example.model.UiState
import com.example.model.WorkCategory
import com.example.ui.components.HeroAiSection
import com.example.ui.components.InteractiveFocusDial
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldSuperDay
import com.example.ui.theme.RoseError
import com.example.ui.theme.SkyFreeze
import com.example.viewmodel.DashboardSummary
import com.example.viewmodel.TimerStatus
import com.example.viewmodel.TimerUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeDashboardScreen(
    summary: DashboardSummary,
    timerState: TimerUiState,
    briefingState: AiCoachState,
    planState: UiState<AiPlanSuggestion?>?,
    onSelectMinutes: (Int) -> Unit,
    onSelectCategory: (WorkCategory) -> Unit,
    onStartStudyMode: () -> Unit,
    onPauseStudyMode: () -> Unit,
    onStopAndLog: () -> Unit,
    onOpenShieldOverlay: () -> Unit,
    onOpenAddGoal: () -> Unit,
    onToggleGoal: (DailyGoal) -> Unit,
    onDeleteGoal: (String) -> Unit,
    onGenerateBriefing: () -> Unit,
    onGeneratePlan: (String) -> Unit,
    onAdoptSuggestedGoal: (AiSuggestedGoal) -> Unit,
    onClearPlan: () -> Unit,
    onOpenCoachChat: () -> Unit,
    onNavigateToShieldSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = DateKeyUtils.todayKey()
    val todayGoals = summary.goals.filter { it.dateKey == today }
    val essentialGoals = todayGoals.filter { it.resolvedTier() == GoalTier.ESSENTIAL }
    val optionalGoals = todayGoals.filter { it.resolvedTier() == GoalTier.OPTIONAL }
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Status Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Daily Focus & Goals",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Streak Flame Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AmberStreak.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, AmberStreak.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = "Current streak",
                                    tint = AmberStreak,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${summary.currentStreak}d",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AmberStreak,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Freeze Shield Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SkyFreeze.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SkyFreeze.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AcUnit,
                                    contentDescription = "Available streak freezes",
                                    tint = SkyFreeze,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${summary.profile.streakFreezes}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SkyFreeze,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Super Day Banner
            item {
                AnimatedVisibility(visible = summary.isSuperDayToday) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GoldSuperDay.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, GoldSuperDay.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = GoldSuperDay,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Super Day Achieved",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldSuperDay,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "All Essential and Bonus goals completed for today.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // AI Focus Coach Section
            item {
                HeroAiSection(
                    briefingState = briefingState,
                    planState = planState,
                    onGenerateBriefing = onGenerateBriefing,
                    onGeneratePlan = onGeneratePlan,
                    onAdoptSuggestedGoal = onAdoptSuggestedGoal,
                    onClearPlan = onClearPlan,
                    onOpenCoachChat = onOpenCoachChat
                )
            }

            // Focus Ring Dial
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
                    onOpenShieldOverlay = onOpenShieldOverlay
                )
            }

            // Focus Shield Status Card
            item {
                val shieldedCount = summary.profile.shieldedApps.size
                val isShieldLive = timerState.status == TimerStatus.RUNNING
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (isShieldLive) ForestGreen.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isShieldLive) ForestGreen.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Shield,
                                        contentDescription = null,
                                        tint = if (isShieldLive) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isShieldLive) {
                                            "Focus Shield Active: $shieldedCount Apps"
                                        } else {
                                            "Focus Shield Armed ($shieldedCount Apps)"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (isShieldLive) {
                                            "Distraction barrier engaged during focus session"
                                        } else {
                                            "Engages automatically when focus session starts"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = onNavigateToShieldSettings,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = "Configure Shield Apps",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (summary.profile.shieldedApps.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                summary.profile.shieldedApps.forEach { app ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = app,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Today's Goals Section Header + Ratio Bar
            item {
                val essDone = essentialGoals.count { it.completed }
                val essTotal = essentialGoals.size
                val ratioColor = when {
                    summary.isSuperDayToday -> GoldSuperDay
                    essTotal > 0 && essDone == essTotal -> ForestGreen
                    summary.todayCompletionRatio >= 0.5f -> AmberStreak
                    essTotal > 0 -> RoseError
                    else -> ForestGreen
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Goals",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (essTotal > 0) {
                                    "Essential: $essDone of $essTotal (${(summary.todayCompletionRatio * 100).toInt()}%)"
                                } else {
                                    "Plan your essential & bonus tasks"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onOpenAddGoal,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreen,
                                contentColor = DarkBackground
                            ),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("add_goal_button")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Add Goal",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (essTotal > 0) {
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { summary.todayCompletionRatio.coerceIn(0f, 1f) },
                            color = ratioColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            // Empty Goal State or Goal Cards
            if (todayGoals.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No daily goals set yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Use Plan Day above or tap Add Goal to define your focus targets.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = onOpenAddGoal,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(42.dp)
                            ) {
                                Text("Create First Goal")
                            }
                        }
                    }
                }
            } else {
                items(essentialGoals + optionalGoals, key = { it.id }) { goal ->
                    GoalRowCard(
                        goal = goal,
                        onToggle = {
                            if (summary.profile.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onToggleGoal(goal)
                        },
                        onDelete = { onDeleteGoal(goal.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun GoalRowCard(
    goal: DailyGoal,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isOptional = goal.resolvedTier() == GoalTier.OPTIONAL
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (goal.completed) ForestGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .testTag("goal_card_${goal.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (goal.completed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = if (goal.completed) "Mark incomplete" else "Mark complete",
                    tint = if (goal.completed) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (isOptional) AmberStreak.copy(alpha = 0.12f) else ForestGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isOptional) "BONUS" else "ESSENTIAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOptional) AmberStreak else ForestGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "${goal.targetMinutes}m • ${goal.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (goal.completed) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (goal.completed) TextDecoration.LineThrough else TextDecoration.None
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete goal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
