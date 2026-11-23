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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DateKeyUtils
import com.example.model.WorkSession
import com.example.ui.components.IllustratedEmptyStateCard
import com.example.ui.components.categoryIconRes
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Mint400
import com.example.viewmodel.DashboardSummary

@Composable
fun TimelineScreen(
    summary: DashboardSummary,
    onOpenManualLog: (dateKey: String) -> Unit,
    onUpdateSessionNotes: (sessionId: String, notes: String) -> Unit,
    onDeleteSession: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDateKey by remember { mutableStateOf(DateKeyUtils.todayKey()) }
    val daySessions = remember(summary.sessions, selectedDateKey) {
        summary.sessions
            .filter { it.dateKey == selectedDateKey }
            .sortedBy { it.startMinuteOfDay }
    }
    val dayTotalMinutes = daySessions.sumOf { it.durationMinutes.toInt() }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "24-HOUR TIME BLOCK",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Work Timeline",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Button(
                        onClick = { onOpenManualLog(selectedDateKey) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = Emerald950
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("add_manual_log_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Log", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Date Navigation Strip
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
                            onClick = { selectedDateKey = DateKeyUtils.shiftDays(selectedDateKey, -1) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = "Previous day"
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = DateKeyUtils.displayDate(selectedDateKey),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${DateKeyUtils.formatDurationMinutes(dayTotalMinutes)} logged • ${daySessions.size} blocks",
                                style = MaterialTheme.typography.labelSmall,
                                color = Mint400
                            )
                        }

                        IconButton(
                            onClick = { selectedDateKey = DateKeyUtils.shiftDays(selectedDateKey, 1) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = "Next day"
                            )
                        }
                    }
                }
            }

            if (daySessions.isEmpty()) {
                item {
                    IllustratedEmptyStateCard(
                        illustrationRes = R.drawable.img_empty_timeline,
                        illustrationDescription = "Minimalist emerald timeline artwork",
                        title = "No work blocks logged for ${DateKeyUtils.displayDate(selectedDateKey)}",
                        subtitle = "Run Study Mode from Home or tap Add Log to place a time block with sub-notes.",
                        actionContent = {
                            OutlinedButton(
                                onClick = { onOpenManualLog(selectedDateKey) },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Log Work Block")
                            }
                        }
                    )
                }
            } else {
                items(daySessions, key = { it.id }) { session ->
                    TimelineBlockCard(
                        session = session,
                        onSaveNotes = { updated -> onUpdateSessionNotes(session.id, updated) },
                        onDelete = { onDeleteSession(session.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimelineBlockCard(
    session: WorkSession,
    onSaveNotes: (String) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    var editingNotes by remember { mutableStateOf(false) }
    var draftNotes by remember(session.subNotes) { mutableStateOf(session.subNotes) }

    val startFormatted = DateKeyUtils.formatMinuteOfDay(session.startMinuteOfDay.toInt())
    val endFormatted = DateKeyUtils.formatMinuteOfDay(
        (session.startMinuteOfDay + session.durationMinutes).toInt().coerceAtMost(1439)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Left Time Rail
        Column(
            modifier = Modifier.width(74.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = startFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = Mint400
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Emerald500)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(62.dp)
                    .background(Emerald500.copy(alpha = 0.35f))
            )
            Text(
                text = endFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(8.dp))

        // Session Content & Sub-Note Drawer
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
            modifier = Modifier
                .weight(1f)
                .clickable { expanded = !expanded }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Emerald500.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = DateKeyUtils.formatDurationMinutes(session.durationMinutes.toInt()),
                                style = MaterialTheme.typography.labelSmall,
                                color = Mint400,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = categoryIconRes(session.category)),
                                    contentDescription = null,
                                    tint = Mint400,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = session.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (session.shieldActive) {
                            Icon(
                                imageVector = Icons.Rounded.Shield,
                                contentDescription = "Focus Shield active",
                                tint = Mint400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (expanded) "Collapse notes" else "Expand notes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (session.tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        session.tags.forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Expandable Sub-Note Drawer
                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SUB-NOTES & DELIVERABLES",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row {
                                        IconButton(
                                            onClick = { editingNotes = !editingNotes },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.EditNote,
                                                contentDescription = "Edit sub-notes",
                                                tint = Mint400
                                            )
                                        }
                                        IconButton(
                                            onClick = onDelete,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                contentDescription = "Delete session",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                if (editingNotes) {
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = draftNotes,
                                        onValueChange = { if (it.length <= 2000) draftNotes = it },
                                        minLines = 3,
                                        maxLines = 6,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = {
                                            draftNotes = session.subNotes
                                            editingNotes = false
                                        }) {
                                            Text("Cancel")
                                        }
                                        Button(
                                            onClick = {
                                                onSaveNotes(draftNotes)
                                                editingNotes = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Emerald500,
                                                contentColor = Emerald950
                                            )
                                        ) {
                                            Text("Save", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = session.subNotes.ifBlank { "No deliverables recorded yet. Tap edit to add notes." },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
