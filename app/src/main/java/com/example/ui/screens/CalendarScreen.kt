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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Schedule
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
import com.example.model.TaskStatus
import com.example.ui.components.bioTechGridPattern
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

    val selectedDayGoals = remember(summary.goals, selectedDateKey) {
        summary.goals.filter { it.dateKey == selectedDateKey }
    }
    val selectedSuccessCount = selectedDayGoals.count { it.completed || it.resolvedStatus() == TaskStatus.COMPLETED }
    val selectedFailedCount = selectedDayGoals.count { it.resolvedStatus() == TaskStatus.FAILED }

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
            // Header
            item {
                Column {
                    Text(
                        text = "Study Calendar",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Success & failed tasks tracked across daily routines",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Month Switcher
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
                            onClick = {
                                if (currentMonth == 0) {
                                    currentMonth = 11
                                    currentYear -= 1
                                } else {
                                    currentMonth -= 1
                                }
                            },
                            modifier = Modifier.size(36.dp)
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
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
                        }
                    }
                }
            }

            // Calendar Grid Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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

            // Legend
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendDot("Success Tasks", ForestGreen)
                        LegendDot("Failed Tasks", RoseError)
                        LegendDot("Study Hours", AmberStreak)
                    }
                }
            }

            // Selected Day Summary Card
            if (selectedDay != null) {
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
                                        text = DateKeyUtils.formatDateHeader(selectedDay.dateKey),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Day Activity Record",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // 3 Metric Cards: Study Time, Success Jobs, Failed Jobs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DayStatTile(
                                    label = "Study Time",
                                    value = DateKeyUtils.formatDurationMinutes(selectedDay.focusMinutes),
                                    accentColor = ForestGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                DayStatTile(
                                    label = "Success Jobs",
                                    value = "$selectedSuccessCount",
                                    accentColor = ForestGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                DayStatTile(
                                    label = "Failed Jobs",
                                    value = "$selectedFailedCount",
                                    accentColor = if (selectedFailedCount > 0) RoseError else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            // Tasks for this date
                            if (selectedDayGoals.isNotEmpty()) {
                                Text(
                                    text = "Scheduled Tasks (${selectedDayGoals.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    selectedDayGoals.forEach { task ->
                                        val isCompleted = task.completed || task.resolvedStatus() == TaskStatus.COMPLETED
                                        val isFailed = task.resolvedStatus() == TaskStatus.FAILED

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            border = BorderStroke(
                                                1.dp,
                                                when {
                                                    isCompleted -> ForestGreen.copy(alpha = 0.4f)
                                                    isFailed -> RoseError.copy(alpha = 0.4f)
                                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                }
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = when {
                                                            isCompleted -> Icons.Rounded.CheckCircle
                                                            isFailed -> Icons.Rounded.Close
                                                            else -> Icons.Rounded.Schedule
                                                        },
                                                        contentDescription = null,
                                                        tint = when {
                                                            isCompleted -> ForestGreen
                                                            isFailed -> RoseError
                                                            else -> AmberStreak
                                                        },
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Column {
                                                        Text(task.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                                        Text(
                                                            "${DateKeyUtils.formatMinuteOfDay(task.startMinuteOfDay.toInt())} • ${task.targetMinutes}m • ${task.category}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = when {
                                                        isCompleted -> "Success"
                                                        isFailed -> "Failed"
                                                        else -> "Pending"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        isCompleted -> ForestGreen
                                                        isFailed -> RoseError
                                                        else -> AmberStreak
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No tasks were scheduled for this date.",
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
    val hasSuccess = cell.successCount > 0 || cell.essentialCompleted > 0
    val hasFailed = cell.failedCount > 0
    val hasFocus = cell.focusMinutes > 0

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            ForestGreen.copy(alpha = 0.2f)
        } else if (hasSuccess) {
            ForestGreen.copy(alpha = 0.08f)
        } else if (hasFocus) {
            AmberStreak.copy(alpha = 0.07f)
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

            if (cell.inCurrentMonth && (hasSuccess || hasFailed || hasFocus)) {
                Spacer(Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasSuccess) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(ForestGreen)
                        )
                    }
                    if (hasFailed) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(RoseError)
                        )
                    }
                    if (hasFocus && !hasSuccess && !hasFailed) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(AmberStreak)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
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
private fun DayStatTile(label: String, value: String, accentColor: Color = ForestGreen, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
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
