package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AiCoachState
import com.example.model.AiPlanSuggestion
import com.example.model.AiSuggestedGoal
import com.example.model.GoalTier
import com.example.model.UiState
import com.example.ui.theme.Crimson500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Gold400
import com.example.ui.theme.Mint200
import com.example.ui.theme.Mint400

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroAiSection(
    briefingState: AiCoachState,
    planState: UiState<AiPlanSuggestion?>?,
    onGenerateBriefing: () -> Unit,
    onGeneratePlan: (String) -> Unit,
    onAdoptSuggestedGoal: (AiSuggestedGoal) -> Unit,
    onClearPlan: () -> Unit,
    onOpenCoachChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPlannerInput by remember { mutableStateOf(false) }
    var objectiveInput by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Emerald900,
        border = BorderStroke(1.dp, Mint400.copy(alpha = 0.4f)),
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_ai_section")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Custom Generated Hero Atmospheric Art
            Image(
                painter = painterResource(id = R.drawable.img_hero_ai_coach),
                contentDescription = "AI Focus Coach Hero Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(28.dp))
            )

            // Crisp Glassmorphic Deep Emerald Overlay for High Contrast Readability
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Emerald950.copy(alpha = 0.68f),
                                Emerald900.copy(alpha = 0.88f),
                                Emerald950.copy(alpha = 0.94f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Emerald500.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Mint400.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Mint400,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "HERO AI FOCUS COACH",
                                style = MaterialTheme.typography.labelSmall,
                                color = Mint200
                            )
                        }
                    }

                    IconButton(
                        onClick = onGenerateBriefing,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("hero_ai_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Generate AI Focus Briefing",
                            tint = Mint200
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Clarity Engine",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                // AI Briefing Display
                when (briefingState) {
                    is AiCoachState.Idle -> {
                        Text(
                            text = "Tap Generate Briefing for a live Gemini strategy tailored to today's goals, streak, and focus velocity.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Mint200.copy(alpha = 0.9f)
                        )
                    }

                    is AiCoachState.Loading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Mint400,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Synthesizing your focus telemetry...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Mint200
                            )
                        }
                    }

                    is AiCoachState.Streaming -> {
                        Text(
                            text = briefingState.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }

                    is AiCoachState.Success -> {
                        Text(
                            text = briefingState.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }

                    is AiCoachState.Error -> {
                        Surface(
                            color = Crimson500.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Crimson500.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = briefingState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFECACA),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Action Chips Row
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onGenerateBriefing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = Emerald950
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("hero_ai_briefing_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Daily Briefing", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showPlannerInput = !showPlannerInput },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Mint400.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("hero_ai_planner_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = Mint400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("AI Day Plan", color = Color.White)
                    }

                    AssistChip(
                        onClick = onOpenCoachChat,
                        label = { Text("Ask Coach", color = Mint200) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.ChatBubbleOutline,
                                contentDescription = null,
                                tint = Mint400,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Emerald950.copy(alpha = 0.65f)
                        ),
                        border = BorderStroke(1.dp, Emerald700),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("hero_ai_chat_button")
                    )
                }

                // Expandable Structured AI Day Planner
                AnimatedVisibility(visible = showPlannerInput) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                    ) {
                        OutlinedTextField(
                            value = objectiveInput,
                            onValueChange = { objectiveInput = it },
                            placeholder = {
                                Text(
                                    "e.g. Ship auth flow, study algorithms, 30m workout",
                                    color = Mint200.copy(alpha = 0.6f)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Mint400,
                                unfocusedBorderColor = Emerald700,
                                cursorColor = Mint400
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_objective_input")
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onGeneratePlan(objectiveInput) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Mint400,
                                    contentColor = Emerald950
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("generate_ai_plan_button")
                            ) {
                                Text("Build 3 Focus Blocks", fontWeight = FontWeight.Bold)
                            }
                            if (planState != null) {
                                OutlinedButton(
                                    onClick = onClearPlan,
                                    border = BorderStroke(1.dp, Emerald700),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("Clear", color = Mint200)
                                }
                            }
                        }
                    }
                }

                // Structured Plan Result Cards
                when (planState) {
                    is UiState.Loading -> {
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Mint400,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Generating structured work blocks...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Mint200
                            )
                        }
                    }

                    is UiState.Error -> {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = planState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFCA5A5)
                        )
                    }

                    is UiState.Success -> {
                        val suggestion = planState.data
                        if (suggestion != null) {
                            Spacer(Modifier.height(16.dp))
                            Surface(
                                color = Emerald950.copy(alpha = 0.72f),
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, Mint400.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = suggestion.headline,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Mint400,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = suggestion.strategyTip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Mint200.copy(alpha = 0.85f)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    suggestion.suggestedGoals.forEach { goal ->
                                        SuggestedGoalCard(
                                            goal = goal,
                                            onAdd = { onAdoptSuggestedGoal(goal) }
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    null -> Unit
                }
            }
        }
    }
}

@Composable
private fun SuggestedGoalCard(
    goal: AiSuggestedGoal,
    onAdd: () -> Unit
) {
    Surface(
        color = Emerald900.copy(alpha = 0.9f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Emerald700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (goal.tier == GoalTier.ESSENTIAL) "ESSENTIAL" else "BONUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (goal.tier == GoalTier.ESSENTIAL) Mint400 else Gold400
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${goal.targetMinutes}m • ${goal.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Mint200.copy(alpha = 0.75f)
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                if (goal.rationale.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = goal.rationale,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mint200.copy(alpha = 0.75f)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onAdd,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add suggested goal",
                    tint = Mint400
                )
            }
        }
    }
}
