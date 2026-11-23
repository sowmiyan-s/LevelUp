package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DateKeyUtils
import com.example.model.TaskLog
import com.example.model.WorkCategory
import com.example.ui.components.IllustratedEmptyStateCard
import com.example.ui.components.categoryIconRes
import com.example.ui.components.orbitalContourPattern
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Mint400
import com.example.viewmodel.DashboardSummary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskLogScreen(
    summary: DashboardSummary,
    isSavingLog: Boolean,
    onSaveTaskLog: (
        workDone: String,
        category: String,
        durationMinutes: Int?,
        onSuccess: () -> Unit
    ) -> Unit,
    onDeleteTaskLog: (logId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var workDoneInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(WorkCategory.DEEP_WORK) }
    var selectedDuration by remember { mutableStateOf<Int?>(null) }
    var activeFilterCategory by remember { mutableStateOf<String?>(null) }

    val haptics = LocalHapticFeedback.current
    val trimmedLength = workDoneInput.trim().length

    val sortedLogs = remember(summary.taskLogs, activeFilterCategory) {
        val base = if (activeFilterCategory == null) {
            summary.taskLogs
        } else {
            summary.taskLogs.filter { it.category.equals(activeFilterCategory, ignoreCase = true) }
        }
        base.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
    }

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
            // Header
            item {
                Column {
                    Text(
                        text = "QUICK CAPTURE FEED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Task Log",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Work Done Composer Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    border = BorderStroke(1.dp, Mint400.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .orbitalContourPattern()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.EditNote,
                                    contentDescription = null,
                                    tint = Mint400,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Record Work Done",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${workDoneInput.length}/500",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = workDoneInput,
                            onValueChange = { if (it.length <= 500) workDoneInput = it },
                            placeholder = {
                                Text("What did you just finish? (e.g. Fixed auth token refresh & wrote unit tests)")
                            },
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Mint400,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                                cursorColor = Mint400
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("task_log_text_input")
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = "CATEGORY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WorkCategory.entries.forEach { cat ->
                                val selected = selectedCategory == cat
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedCategory = cat },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = categoryIconRes(cat.label)),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text(cat.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Emerald500.copy(alpha = 0.22f),
                                        selectedLabelColor = Mint400,
                                        selectedLeadingIconColor = Mint400
                                    ),
                                    modifier = Modifier.testTag("task_log_category_${cat.name}")
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "DURATION (OPTIONAL)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(15, 30, 45, 60).forEach { mins ->
                                val selected = selectedDuration == mins
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedDuration = if (selected) null else mins
                                    },
                                    label = { Text("${mins}m") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("task_log_duration_$mins")
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (summary.profile.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onSaveTaskLog(
                                    workDoneInput,
                                    selectedCategory.label,
                                    selectedDuration
                                ) {
                                    workDoneInput = ""
                                    selectedDuration = null
                                }
                            },
                            enabled = trimmedLength > 0 && !isSavingLog,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald500,
                                contentColor = Emerald950
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("save_task_log_button")
                        ) {
                            if (isSavingLog) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Emerald950,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Saving...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Save Work Log",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Recent Work Logs Header & Category Filter Row
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Work Logs",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${sortedLogs.size} entries",
                            style = MaterialTheme.typography.labelSmall,
                            color = Mint400
                        )
                    }

                    AnimatedVisibility(visible = summary.taskLogs.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            FilterChip(
                                selected = activeFilterCategory == null,
                                onClick = { activeFilterCategory = null },
                                label = { Text("All") }
                            )
                            WorkCategory.entries.forEach { cat ->
                                FilterChip(
                                    selected = activeFilterCategory == cat.label,
                                    onClick = {
                                        activeFilterCategory =
                                            if (activeFilterCategory == cat.label) null else cat.label
                                    },
                                    label = { Text(cat.label) }
                                )
                            }
                        }
                    }
                }
            }

            // Feed Items or Empty State
            if (sortedLogs.isEmpty()) {
                item {
                    IllustratedEmptyStateCard(
                        illustrationRes = R.drawable.img_empty_task_log,
                        illustrationDescription = "Minimalist emerald work log illustration",
                        title = if (activeFilterCategory == null) {
                            "No work logs recorded yet"
                        } else {
                            "No $activeFilterCategory logs yet"
                        },
                        subtitle = "Type what you completed above and tap Save Work Log to record it with a live server timestamp."
                    )
                }
            } else {
                items(sortedLogs, key = { it.id }) { entry ->
                    TaskLogEntryCard(
                        entry = entry,
                        onDelete = { onDeleteTaskLog(entry.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun TaskLogEntryCard(
    entry: TaskLog,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_log_card_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Emerald500.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = categoryIconRes(entry.category)),
                                contentDescription = null,
                                tint = Mint400,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = entry.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Mint400
                            )
                        }
                    }
                    if (entry.durationMinutes != null && entry.durationMinutes > 0L) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = DateKeyUtils.formatDurationMinutes(entry.durationMinutes.toInt()),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = entry.workDone,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = DateKeyUtils.formatTimestamp(entry.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("delete_task_log_${entry.id}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete work log",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }
    }
}
