package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.AiCoachState
import com.example.model.ChatMessage
import com.example.model.DateKeyUtils
import com.example.model.GoalTier
import com.example.model.WorkCategory
import com.example.ui.theme.Amber500
import com.example.ui.theme.Crimson500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Gold400
import com.example.ui.theme.Mint200
import com.example.ui.theme.Mint400
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, targetMinutes: Int, category: String, tier: GoalTier) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetMinutes by remember { mutableFloatStateOf(60f) }
    var selectedCategory by remember { mutableStateOf(WorkCategory.DEEP_WORK) }
    var selectedTier by remember { mutableStateOf(GoalTier.ESSENTIAL) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "New Daily Goal",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 120) title = it },
                    label = { Text("Goal title") },
                    placeholder = { Text("e.g. Complete Chapter 4 Algorithms") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_goal_title_input")
                )

                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Target Duration: ${targetMinutes.roundToInt()} min",
                    style = MaterialTheme.typography.titleMedium
                )
                Slider(
                    value = targetMinutes,
                    onValueChange = { targetMinutes = ((it / 15f).roundToInt() * 15f).coerceIn(15f, 300f) },
                    valueRange = 15f..300f,
                    colors = SliderDefaults.colors(
                        thumbColor = Emerald500,
                        activeTrackColor = Mint400
                    )
                )

                Spacer(Modifier.height(8.dp))
                Text("Priority Tier", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = selectedTier == GoalTier.ESSENTIAL,
                        onClick = { selectedTier = GoalTier.ESSENTIAL },
                        label = { Text("Essential") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald500.copy(alpha = 0.22f),
                            selectedLabelColor = Mint400
                        )
                    )
                    FilterChip(
                        selected = selectedTier == GoalTier.OPTIONAL,
                        onClick = { selectedTier = GoalTier.OPTIONAL },
                        label = { Text("Optional Bonus") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Gold400.copy(alpha = 0.22f),
                            selectedLabelColor = Gold400
                        )
                    )
                }

                Spacer(Modifier.height(10.dp))
                Text("Category", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WorkCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = categoryIconRes(cat.label)),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(cat.label) }
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    title.trim(),
                                    targetMinutes.roundToInt(),
                                    selectedCategory.label,
                                    selectedTier
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = Emerald950
                        ),
                        modifier = Modifier.testTag("save_goal_button")
                    ) {
                        Text("Save Goal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogSessionDialog(
    initialTitle: String,
    initialDurationMinutes: Int,
    initialStartMinute: Int,
    initialCategory: WorkCategory,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: String,
        startMinuteOfDay: Int,
        durationMinutes: Int,
        tags: List<String>,
        subNotes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var durationMinutes by remember { mutableIntStateOf(initialDurationMinutes.coerceIn(1, 360)) }
    var startHour by remember { mutableIntStateOf((initialStartMinute / 60).coerceIn(0, 23)) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var tagsText by remember { mutableStateOf(initialCategory.defaultTag) }
    var subNotes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Log Focus Session",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 120) title = it },
                    label = { Text("Session Title") },
                    placeholder = { Text("e.g. Deep Work Architecture") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_session_title_input")
                )

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Duration: ${DateKeyUtils.formatDurationMinutes(durationMinutes)} • Start: ${DateKeyUtils.formatMinuteOfDay(startHour * 60)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = durationMinutes.toFloat(),
                    onValueChange = { durationMinutes = it.roundToInt().coerceIn(5, 360) },
                    valueRange = 5f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = Emerald500,
                        activeTrackColor = Mint400
                    )
                )

                Text(
                    text = "Start Hour: ${DateKeyUtils.formatMinuteOfDay(startHour * 60)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = startHour.toFloat(),
                    onValueChange = { startHour = it.roundToInt().coerceIn(0, 23) },
                    valueRange = 0f..23f,
                    colors = SliderDefaults.colors(
                        thumbColor = Mint400,
                        activeTrackColor = Emerald500
                    )
                )

                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WorkCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = categoryIconRes(cat.label)),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(cat.label) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("Tags (comma or space separated)") },
                    placeholder = { Text("#kotlin #architecture") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = subNotes,
                    onValueChange = { if (it.length <= 2000) subNotes = it },
                    label = { Text("Sub-Notes & Deliverables") },
                    placeholder = { Text("• Key milestones achieved\n• Next step for tomorrow") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_session_notes_input")
                )

                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Discard")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedTags = tagsText
                                .split(" ", ",", ";")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .map { if (it.startsWith("#")) it else "#$it" }
                                .take(8)
                            if (title.isNotBlank()) {
                                onSave(
                                    title.trim(),
                                    selectedCategory.label,
                                    startHour * 60,
                                    durationMinutes,
                                    parsedTags,
                                    subNotes.trim()
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = Emerald950
                        ),
                        modifier = Modifier.testTag("confirm_log_session_button")
                    ) {
                        Text("Save to Timeline", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusShieldOverlayDialog(
    elapsedSeconds: Int,
    plannedMinutes: Int,
    shieldedApps: List<String>,
    onMinimize: () -> Unit,
    onEmergencyUnlock: () -> Unit
) {
    var showUnlockConfirm by remember { mutableStateOf(false) }
    val remainingSeconds = ((plannedMinutes * 60) - elapsedSeconds).coerceAtLeast(0)

    Dialog(
        onDismissRequest = onMinimize,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Emerald950, Emerald900, Color(0xFF041D15))
                    )
                )
                .bioTechGridPattern(
                    dotColor = Mint400.copy(alpha = 0.16f),
                    lineColor = Emerald500.copy(alpha = 0.08f)
                )
                .safeDrawingPadding()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Emerald500.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Mint400.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = Mint400,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "FOCUS SHIELD ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Mint200
                            )
                        }
                    }

                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Minimize Focus Shield",
                            tint = Mint200
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        border = BorderStroke(2.dp, Mint400.copy(alpha = 0.7f)),
                        tonalElevation = 8.dp,
                        modifier = Modifier.size(112.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_focus_shield_emblem),
                            contentDescription = "Focus Shield Active Emblem",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Remaining Focus Time",
                        style = MaterialTheme.typography.titleMedium,
                        color = Mint200.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = DateKeyUtils.formatTimerSeconds(remainingSeconds),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Shielded Distraction Apps:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mint200.copy(alpha = 0.75f)
                    )
                    Spacer(Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        shieldedApps.forEach { app ->
                            Surface(
                                color = Emerald900,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Emerald700)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = Crimson500,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(app, color = Color.White, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onMinimize,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = Emerald950
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Return to Dashboard", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showUnlockConfirm = true },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Crimson500.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = Crimson500,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Emergency Unlock",
                            color = Color(0xFFFCA5A5),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showUnlockConfirm) {
        AlertDialog(
            onDismissRequest = { showUnlockConfirm = false },
            title = { Text("Break Focus Shield?") },
            text = {
                Text("Unlocking early cancels this active Study Mode session without logging your elapsed focus minutes.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlockConfirm = false
                        onEmergencyUnlock()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Crimson500)
                ) {
                    Text("Unlock & Discard", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlockConfirm = false }) {
                    Text("Stay Focused")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiCoachChatBottomSheet(
    messages: List<ChatMessage>,
    replyState: AiCoachState,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var input by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Mint400
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "AI Focus Coach",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(10.dp))

            if (messages.isEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "How should I sequence my next 2 hours?",
                        "Give me a 25m sprint plan to beat procrastination",
                        "Summarize my progress today"
                    ).forEach { prompt ->
                        FilterChip(
                            selected = false,
                            onClick = { onSendMessage(prompt) },
                            label = { Text(prompt) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    Surface(
                        color = if (msg.isUser) {
                            Emerald500.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (msg.isUser) "YOU" else "GEMINI FOCUS COACH",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (msg.isUser) Mint400 else Amber500
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                when (replyState) {
                    is AiCoachState.Loading -> item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Thinking...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    is AiCoachState.Streaming -> item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "GEMINI FOCUS COACH",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Mint400
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = replyState.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    is AiCoachState.Error -> item {
                        Text(
                            text = replyState.message,
                            color = Crimson500,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    else -> Unit
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask your Focus Coach...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (input.isNotBlank()) {
                            onSendMessage(input)
                            input = ""
                        }
                    },
                    enabled = input.isNotBlank() && replyState !is AiCoachState.Loading && replyState !is AiCoachState.Streaming,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send to AI Coach",
                        tint = Mint400
                    )
                }
            }
        }
    }
}
