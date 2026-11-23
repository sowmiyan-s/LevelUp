package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DateKeyUtils
import com.example.model.UserProfile
import com.example.model.WorkCategory
import com.example.ui.components.bioTechGridPattern
import com.example.ui.components.categoryIconRes
import com.example.ui.components.orbitalContourPattern
import com.example.ui.theme.Crimson500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Gold400
import com.example.ui.theme.Mint400
import com.example.viewmodel.DashboardSummary
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatsAndShieldScreen(
    summary: DashboardSummary,
    userEmail: String?,
    onUpdateSettings: (
        dailyTargetMinutes: Long,
        shieldedApps: List<String>,
        darkModeEnabled: Boolean,
        hapticsEnabled: Boolean
    ) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = summary.profile
    val today = DateKeyUtils.todayKey()

    // Last 7 days focus bar chart data
    val weeklyData = remember(summary.sessions, today) {
        val dayFmt = SimpleDateFormat("EEE", Locale.US)
        (-6..0).map { offset ->
            val key = DateKeyUtils.shiftDays(today, offset)
            val label = synchronized(dayFmt) { dayFmt.format(DateKeyUtils.parseKey(key).time) }
            val mins = summary.sessions.filter { it.dateKey == key }.sumOf { it.durationMinutes.toInt() }
            label to mins
        }
    }

    // Category distribution
    val categoryTotals = remember(summary.sessions) {
        val total = summary.sessions.sumOf { it.durationMinutes.toInt() }.coerceAtLeast(1)
        WorkCategory.entries.map { cat ->
            val mins = summary.sessions.filter { it.category.equals(cat.label, ignoreCase = true) }
                .sumOf { it.durationMinutes.toInt() }
            Triple(cat.label, mins, mins.toFloat() / total.toFloat())
        }.filter { it.second > 0 }
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
            item {
                Column {
                    Text(
                        text = "TELEMETRY & SHIELD CONFIG",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Analytics & Shield",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // KPI Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        icon = Icons.Rounded.LocalFireDepartment,
                        label = "Current Streak",
                        value = "${summary.currentStreak} days",
                        accent = Gold400,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        icon = Icons.Rounded.EmojiEvents,
                        label = "Super Days",
                        value = "${summary.superDaysCount}",
                        accent = Mint400,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        icon = Icons.Rounded.Bolt,
                        label = "Total Logged",
                        value = DateKeyUtils.formatDurationMinutes(summary.totalFocusMinutes),
                        accent = Emerald500,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 7-Day Focus Velocity Canvas Chart
            item {
                val maxMins = (weeklyData.maxOfOrNull { it.second } ?: 60).coerceAtLeast(60)
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .orbitalContourPattern()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "7-Day Focus Velocity",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Daily deep work minutes across the past week",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        ) {
                            val barCount = weeklyData.size
                            val slotWidth = size.width / barCount
                            val barWidth = slotWidth * 0.52f
                            weeklyData.forEachIndexed { idx, (_, mins) ->
                                val ratio = (mins.toFloat() / maxMins.toFloat()).coerceIn(0.04f, 1f)
                                val barHeight = size.height * ratio
                                val x = idx * slotWidth + (slotWidth - barWidth) / 2f
                                val y = size.height - barHeight
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        listOf(Mint400, Emerald500)
                                    ),
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            weeklyData.forEach { (label, mins) ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${mins}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Mint400
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category Breakdown
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Category Breakdown",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(12.dp))
                        if (categoryTotals.isEmpty()) {
                            Text(
                                text = "Log focus sessions to see your time distribution by category.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            categoryTotals.forEach { (label, mins, pct) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = categoryIconRes(label)),
                                            contentDescription = null,
                                            tint = Mint400,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(label, style = MaterialTheme.typography.titleMedium)
                                    }
                                    Text(
                                        DateKeyUtils.formatDurationMinutes(mins),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Mint400
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    color = Emerald500,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }

            // Distraction Shield Apps
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .bioTechGridPattern()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                border = BorderStroke(1.dp, Mint400.copy(alpha = 0.55f)),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_focus_shield_emblem),
                                    contentDescription = "Focus Shield emblem",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Shielded Distraction Apps",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Select apps to lock inside Focus Shield when Study Mode is active.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UserProfile.AVAILABLE_DISTRACTION_APPS.forEach { app ->
                                val selected = profile.shieldedApps.contains(app)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        val updated = if (selected) {
                                            profile.shieldedApps - app
                                        } else {
                                            profile.shieldedApps + app
                                        }
                                        onUpdateSettings(
                                            profile.dailyTargetMinutes,
                                            updated,
                                            profile.darkModeEnabled,
                                            profile.hapticsEnabled
                                        )
                                    },
                                    label = { Text(app) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Emerald500.copy(alpha = 0.22f),
                                        selectedLabelColor = Mint400
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Preferences & Daily Target
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Preferences & Targets",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Daily Focus Target: ${DateKeyUtils.formatDurationMinutes(profile.dailyTargetMinutes.toInt())}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Slider(
                            value = profile.dailyTargetMinutes.toFloat(),
                            onValueChange = { value ->
                                val snapped = (((value / 30f).roundToInt()) * 30L).coerceIn(60L, 720L)
                                onUpdateSettings(
                                    snapped,
                                    profile.shieldedApps,
                                    profile.darkModeEnabled,
                                    profile.hapticsEnabled
                                )
                            },
                            valueRange = 60f..720f,
                            colors = SliderDefaults.colors(
                                thumbColor = Emerald500,
                                activeTrackColor = Mint400
                            )
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = Mint400)
                                Spacer(Modifier.width(10.dp))
                                Text("Bio-Tech Dark Emerald Theme", style = MaterialTheme.typography.bodyLarge)
                            }
                            Switch(
                                checked = profile.darkModeEnabled,
                                onCheckedChange = { checked ->
                                    onUpdateSettings(
                                        profile.dailyTargetMinutes,
                                        profile.shieldedApps,
                                        checked,
                                        profile.hapticsEnabled
                                    )
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = Emerald500),
                                modifier = Modifier.testTag("dark_mode_switch")
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Vibration, contentDescription = null, tint = Mint400)
                                Spacer(Modifier.width(10.dp))
                                Text("Tactile Haptic Feedback", style = MaterialTheme.typography.bodyLarge)
                            }
                            Switch(
                                checked = profile.hapticsEnabled,
                                onCheckedChange = { checked ->
                                    onUpdateSettings(
                                        profile.dailyTargetMinutes,
                                        profile.shieldedApps,
                                        profile.darkModeEnabled,
                                        checked
                                    )
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = Emerald500)
                            )
                        }
                    }
                }
            }

            // Account Card & Sign Out
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = profile.displayName.ifBlank { "Level Up Member" },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!userEmail.isNullOrBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Crimson500.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = null,
                                tint = Crimson500,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Sign Out", color = Crimson500, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun KpiCard(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
