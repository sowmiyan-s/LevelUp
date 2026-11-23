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
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.R
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
import com.example.ui.components.LevelUpBrandBadge
import com.example.ui.components.bioTechGridPattern
import com.example.ui.components.categoryIconRes
import com.example.ui.theme.Amber500
import com.example.ui.theme.Crimson500
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Gold400
import com.example.ui.theme.Mint400
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LevelUpBrandBadge(size = 42.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "LEVEL UP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Focus Dashboard",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Streak Flame Pill
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Gold400.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, Gold400.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = "Current streak",
                                    tint = Gold400,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${summary.currentStreak}d",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Gold400
                                )
                            }
                        }

                        // Freeze Shield Pill
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Cyan400.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, Cyan400.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AcUnit,
                                    contentDescription = "Available streak freezes",
                                    tint = Cyan400,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${summary.profile.streakFreezes}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Cyan400
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
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, Gold400.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Gold400.copy(alpha = 0.25f), Amber500.copy(alpha = 0.14f))
                                    )
                                )
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = Gold400,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SUPER DAY ACHIEVED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gold400
                                )
                                Text(
                                    text = "100% Essential + Optional Bonus goals completed today!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // HERO AI FOCUS COACH SECTION
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

            // Interactive Focus Ring Dial
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
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (isShieldLive) Mint400.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .bioTechGridPattern()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isShieldLive) Mint400 else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_focus_shield_emblem),
                                        contentDescription = "Focus Shield emblem",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isShieldLive) {
                                            "Focus Shield Active: $shieldedCount Apps Locked"
                                        } else {
                                            "Focus Shield Armed ($shieldedCount Apps)"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isShieldLive) {
                                            "Distraction barrier engaged during Study Mode"
                                        } else {
                                            "Engages automatically when Study Mode starts"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = onNavigateToShieldSettings,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = "Configure Shield Apps",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (summary.profile.shieldedApps.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                summary.profile.shieldedApps.forEach { app ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = app,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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
                    summary.isSuperDayToday -> Gold400
                    essTotal > 0 && essDone == essTotal -> Emerald500
                    summary.todayCompletionRatio >= 0.5f -> Amber500
                    essTotal > 0 -> Crimson500
                    else -> MaterialTheme.colorScheme.primary
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Goals",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (essTotal > 0) {
                                    "Essential: $essDone / $essTotal (${(summary.todayCompletionRatio * 100).toInt()}%)"
                                } else {
                                    "Add essential & optional bonus goals"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onOpenAddGoal,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald500,
                                contentColor = Emerald950
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("add_goal_button")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Goal", fontWeight = FontWeight.Bold)
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
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }
            }

            // Empty Goal State or Goal Cards
            if (todayGoals.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No daily goals locked in yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Use the Hero AI Day Plan above or tap Add Goal to define your Essential & Bonus targets.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = onOpenAddGoal,
                                modifier = Modifier.height(48.dp)
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
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (goal.completed) Mint400.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .testTag("goal_card_${goal.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (goal.completed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = if (goal.completed) "Mark incomplete" else "Mark complete",
                    tint = if (goal.completed) Mint400 else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (isOptional) Gold400.copy(alpha = 0.18f) else Emerald500.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (isOptional) "BONUS" else "ESSENTIAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOptional) Gold400 else Mint400,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Icon(
                        painter = painterResource(id = categoryIconRes(goal.category)),
                        contentDescription = null,
                        tint = Mint400,
                        modifier = Modifier.size(14.dp)
                    )
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
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete goal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
