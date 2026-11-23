package com.example.ui.screens

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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.CalendarDaySummary
import com.example.model.DateKeyUtils
import com.example.model.DayTier
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldSuperDay
import com.example.ui.theme.RoseError
import com.example.ui.theme.SkyFreeze
import com.example.viewmodel.DashboardSummary
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    summary: DashboardSummary,
    buildMonthCells: (DashboardSummary, Int, Int) -> List<CalendarDaySummary>,
    onApplyStreakFreeze: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val now = remember { Calendar.getInstance() }
    var currentYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH)) }
    var selectedDateKey by remember { mutableStateOf(DateKeyUtils.todayKey()) }

    val monthCells = remember(summary, currentYear, currentMonth) {
        buildMonthCells(summary, currentYear, currentMonth)
    }
    val selectedDay = monthCells.firstOrNull { it.dateKey == selectedDateKey }
        ?: monthCells.firstOrNull { it.inCurrentMonth }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Study Calendar",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Monthly study consistency and streaks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Month Switcher
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentMonth == 0) {
                                    currentMonth = 11
                                    currentYear -= 1
                                } else {
                                    currentMonth -= 1
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
                        }

                        Text(
                            text = DateKeyUtils.monthYearLabel(currentYear, currentMonth),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                if (currentMonth == 11) {
                                    currentMonth = 0
                                    currentYear += 1
                                } else {
                                    currentMonth += 1
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
                        }
                    }
                }
            }

            // Calendar Grid Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Weekday Headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // 6 Rows of 7 Days
                        monthCells.chunked(7).forEach { week ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                week.forEach { cell ->
                                    CalendarDayCell(
                                        cell = cell,
                                        isSelected = cell.dateKey == selectedDateKey,
                                        onClick = { selectedDateKey = cell.dateKey },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Legend
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LegendDot("Target Met", ForestGreen)
                    LegendDot("Partial Study", AmberStreak)
                    LegendDot("Rest / Freeze", SkyFreeze)
                }
            }

            // Selected Day Summary Card
            if (selectedDay != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = DateKeyUtils.displayDate(selectedDay.dateKey),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = selectedDay.dateKey,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = tierColor(selectedDay.tier).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = tierLabel(selectedDay.tier),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tierColor(selectedDay.tier),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DayStatTile(
                                    label = "Study Time",
                                    value = DateKeyUtils.formatDurationMinutes(selectedDay.focusMinutes),
                                    modifier = Modifier.weight(1f)
                                )
                                DayStatTile(
                                    label = "Priority Goals",
                                    value = "${selectedDay.essentialCompleted}/${selectedDay.essentialTotal}",
                                    modifier = Modifier.weight(1f)
                                )
                                DayStatTile(
                                    label = "Optional Goals",
                                    value = "${selectedDay.optionalCompleted}/${selectedDay.optionalTotal}",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            val canApplyFreeze = !selectedDay.freezeUsed &&
                                selectedDay.dateKey <= DateKeyUtils.todayKey() &&
                                (selectedDay.tier == DayTier.NEEDS_ATTENTION || selectedDay.tier == DayTier.NO_GOALS) &&
                                summary.profile.streakFreezes > 0L

                            if (canApplyFreeze) {
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { onApplyStreakFreeze(selectedDay.dateKey) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SkyFreeze,
                                        contentColor = DarkBackground
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("apply_streak_freeze_button")
                                ) {
                                    Icon(Icons.Rounded.AcUnit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Apply Streak Saver (${summary.profile.streakFreezes} left)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(14.dp)) }
        }
    }
}

@Composable
private fun CalendarDayCell(
    cell: CalendarDaySummary,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeColor = tierColor(cell.tier)
    val hasActivity = cell.tier != DayTier.NO_GOALS && cell.tier != DayTier.UPCOMING

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) {
            ForestGreen.copy(alpha = 0.15f)
        } else if (hasActivity) {
            badgeColor.copy(alpha = 0.10f)
        } else {
            Color.Transparent
        },
        border = if (isSelected) {
            BorderStroke(1.5.dp, ForestGreen)
        } else {
            null
        },
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${cell.dayOfMonth}",
                style = MaterialTheme.typography.bodySmall,
                color = if (!cell.inCurrentMonth) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                } else if (isSelected) {
                    ForestGreen
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (hasActivity && cell.inCurrentMonth) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DayStatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun tierColor(tier: DayTier): Color = when (tier) {
    DayTier.SUPER_DAY -> GoldSuperDay
    DayTier.TARGET_ACHIEVED -> ForestGreen
    DayTier.PARTIAL_PROGRESS -> AmberStreak
    DayTier.NEEDS_ATTENTION -> RoseError
    DayTier.STREAK_FREEZE -> SkyFreeze
    DayTier.NO_GOALS -> Color.Transparent
    DayTier.UPCOMING -> Color.Transparent
}

private fun tierLabel(tier: DayTier): String = when (tier) {
    DayTier.SUPER_DAY -> "Goal Met"
    DayTier.TARGET_ACHIEVED -> "Target Met"
    DayTier.PARTIAL_PROGRESS -> "Partial"
    DayTier.NEEDS_ATTENTION -> "Missed"
    DayTier.STREAK_FREEZE -> "Rest Day"
    DayTier.NO_GOALS -> "No Activity"
    DayTier.UPCOMING -> "Upcoming"
}
