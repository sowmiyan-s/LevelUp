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
import com.example.ui.theme.Amber500
import com.example.ui.theme.Crimson500
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Gold400
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "4-TIER CONSISTENCY MATRIX",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Global Calendar",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Month Switcher
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
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
                            onClick = {
                                if (currentMonth == 0) {
                                    currentMonth = 11
                                    currentYear -= 1
                                } else {
                                    currentMonth -= 1
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
                        }

                        Text(
                            text = DateKeyUtils.monthYearLabel(currentYear, currentMonth),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
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
                            modifier = Modifier.size(48.dp)
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
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Weekday Headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { day ->
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

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

            // 4-Tier + Freeze Legend
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LegendDot("Super Day (100%+Bonus)", Gold400)
                    LegendDot("Target Achieved (100%)", Emerald500)
                    LegendDot("Partial (50-99%)", Amber500)
                    LegendDot("Needs Attention (<50%)", Crimson500)
                    LegendDot("Streak Freeze", Cyan400)
                }
            }

            // Selected Day Summary Card
            if (selectedDay != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, tierColor(selectedDay.tier).copy(alpha = 0.6f)),
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
                                        text = DateKeyUtils.displayDate(selectedDay.dateKey),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = selectedDay.dateKey,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = tierColor(selectedDay.tier).copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = tierLabel(selectedDay.tier),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tierColor(selectedDay.tier),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                DayStatTile(
                                    label = "Focus Logged",
                                    value = DateKeyUtils.formatDurationMinutes(selectedDay.focusMinutes),
                                    modifier = Modifier.weight(1f)
                                )
                                DayStatTile(
                                    label = "Essential Goals",
                                    value = "${selectedDay.essentialCompleted}/${selectedDay.essentialTotal}",
                                    modifier = Modifier.weight(1f)
                                )
                                DayStatTile(
                                    label = "Bonus Goals",
                                    value = "${selectedDay.optionalCompleted}/${selectedDay.optionalTotal}",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            val canApplyFreeze = !selectedDay.freezeUsed &&
                                selectedDay.dateKey <= DateKeyUtils.todayKey() &&
                                (selectedDay.tier == DayTier.NEEDS_ATTENTION || selectedDay.tier == DayTier.NO_GOALS) &&
                                summary.profile.streakFreezes > 0L

                            if (canApplyFreeze) {
                                Spacer(Modifier.height(14.dp))
                                Button(
                                    onClick = { onApplyStreakFreeze(selectedDay.dateKey) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Cyan400,
                                        contentColor = Emerald950
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("apply_streak_freeze_button")
                                ) {
                                    Icon(Icons.Rounded.AcUnit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Protect Day with Streak Freeze (${summary.profile.streakFreezes} left)",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
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
    val bgAlpha = when {
        !cell.inCurrentMonth -> 0.08f
        cell.tier == DayTier.NO_GOALS || cell.tier == DayTier.UPCOMING -> 0.14f
        else -> 0.25f
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = badgeColor.copy(alpha = bgAlpha),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface)
        } else if (cell.tier != DayTier.NO_GOALS && cell.tier != DayTier.UPCOMING) {
            BorderStroke(1.dp, badgeColor.copy(alpha = 0.55f))
        } else {
            null
        },
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = cell.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = if (cell.inCurrentMonth) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                }
            )
            if (cell.tier != DayTier.NO_GOALS && cell.tier != DayTier.UPCOMING) {
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .size(7.dp)
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
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DayStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun tierColor(tier: DayTier): Color = when (tier) {
    DayTier.SUPER_DAY -> Gold400
    DayTier.TARGET_ACHIEVED -> Emerald500
    DayTier.PARTIAL_PROGRESS -> Amber500
    DayTier.NEEDS_ATTENTION -> Crimson500
    DayTier.STREAK_FREEZE -> Cyan400
    DayTier.NO_GOALS, DayTier.UPCOMING -> Color(0xFF4B6B5E)
}

private fun tierLabel(tier: DayTier): String = when (tier) {
    DayTier.SUPER_DAY -> "SUPER DAY"
    DayTier.TARGET_ACHIEVED -> "TARGET ACHIEVED"
    DayTier.PARTIAL_PROGRESS -> "PARTIAL PROGRESS"
    DayTier.NEEDS_ATTENTION -> "NEEDS ATTENTION"
    DayTier.STREAK_FREEZE -> "STREAK FREEZE"
    DayTier.NO_GOALS -> "NO DATA"
    DayTier.UPCOMING -> "UPCOMING"
}
