package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DateKeyUtils
import com.example.model.WorkCategory
import com.example.ui.components.AddBlockedAppDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.bioTechGridPattern
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.RoseError
import com.example.util.InstalledAppsHelper
import com.example.viewmodel.DashboardSummary
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InsightsScreen(
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
    val context = LocalContext.current
    val profile = summary.profile
    val today = DateKeyUtils.todayKey()
    var showAddAppDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Last 7 days study minutes bar chart data
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

    val totalStudyMinutes = remember(summary.sessions) {
        summary.sessions.sumOf { it.durationMinutes.toInt() }
    }
    val totalSuccessTasks = remember(summary.goals) {
        summary.goals.count { it.completed }
    }

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
            // Header with top-right Settings gear icon
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Insights",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Performance analytics & system shields",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Top-right Settings Button
                    Surface(
                        onClick = { showSettingsDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Open Settings",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // MIUI App Vault KPI Cards (2x2 grid)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VaultKpiCard(
                            icon = Icons.Rounded.LocalFireDepartment,
                            label = "Consecutive Streak",
                            value = "${summary.currentStreak} Days",
                            accent = AmberStreak,
                            modifier = Modifier.weight(1f)
                        )
                        VaultKpiCard(
                            icon = Icons.Rounded.Schedule,
                            label = "Total Study Time",
                            value = DateKeyUtils.formatDurationMinutes(totalStudyMinutes),
                            accent = ForestGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VaultKpiCard(
                            icon = Icons.Rounded.Bolt,
                            label = "Today's Focus",
                            value = DateKeyUtils.formatDurationMinutes(summary.todayFocusMinutes),
                            accent = ForestGreen,
                            modifier = Modifier.weight(1f)
                        )
                        VaultKpiCard(
                            icon = Icons.Rounded.CheckCircle,
                            label = "Tasks Completed",
                            value = "$totalSuccessTasks Success",
                            accent = ForestGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Weekly Study Activity Card
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
                            Text(
                                text = "Weekly Study Activity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Past 7 Days",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        val maxMinutes = weeklyData.maxOfOrNull { it.second }?.coerceAtLeast(60) ?: 60
                        val barColor = ForestGreen
                        val emptyBarColor = MaterialTheme.colorScheme.surfaceVariant

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            val slotWidth = size.width / weeklyData.size
                            val barWidth = slotWidth * 0.45f
                            val maxBarHeight = size.height - 24.dp.toPx()

                            weeklyData.forEachIndexed { index, pair ->
                                val xCenter = slotWidth * index + slotWidth / 2f
                                val barHeight = if (maxMinutes > 0) {
                                    (pair.second.toFloat() / maxMinutes.toFloat()) * maxBarHeight
                                } else 0f

                                val left = xCenter - barWidth / 2f
                                val top = maxBarHeight - barHeight

                                drawRoundRect(
                                    color = emptyBarColor,
                                    topLeft = Offset(left, 0f),
                                    size = Size(barWidth, maxBarHeight),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )

                                if (barHeight > 4f) {
                                    drawRoundRect(
                                        color = barColor,
                                        topLeft = Offset(left, top),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            weeklyData.forEach { (label, mins) ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (mins > 0) "${mins}m" else "-",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (mins > 0) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category Breakdown Card
            if (categoryTotals.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Study Category Distribution",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                categoryTotals.forEach { (catLabel, mins, frac) ->
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = catLabel,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${DateKeyUtils.formatDurationMinutes(mins)} (${(frac * 100).toInt()}%)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ForestGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { frac },
                                            color = ForestGreen,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Study Mode Lockdown Vault Card (with real installed app icons)
            item {
                val blockedApps = summary.profile.shieldedApps
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ForestGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Shield,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Study Mode App Lockdown",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Lock internet & silence alerts on blocked apps",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { showAddAppDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForestGreen,
                                    contentColor = DarkBackground
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add App", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        if (blockedApps.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                blockedApps.forEach { appName ->
                                    val appInfo = remember(appName) { InstalledAppsHelper.getAppInfo(context, appName) }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (appInfo.iconBitmap != null) {
                                                Image(
                                                    bitmap = appInfo.iconBitmap,
                                                    contentDescription = appName,
                                                    modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp))
                                                )
                                                Spacer(Modifier.width(6.dp))
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Rounded.Shield,
                                                    contentDescription = null,
                                                    tint = ForestGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                            }

                                            Text(
                                                text = appInfo.appName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            IconButton(
                                                onClick = {
                                                    val updated = blockedApps.filterNot { it.equals(appName, ignoreCase = true) }
                                                    onUpdateSettings(profile.dailyTargetMinutes, updated, profile.darkModeEnabled, profile.hapticsEnabled)
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Close,
                                                    contentDescription = "Remove $appName",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No apps in lockdown. Add distracting social apps to block them automatically whenever you activate Study Mode.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(14.dp)) }
        }
    }

    if (showAddAppDialog) {
        AddBlockedAppDialog(
            currentBlockedApps = summary.profile.shieldedApps,
            onDismiss = { showAddAppDialog = false },
            onAddApp = { newApp ->
                if (summary.profile.shieldedApps.none { it.equals(newApp, ignoreCase = true) }) {
                    onUpdateSettings(
                        profile.dailyTargetMinutes,
                        profile.shieldedApps + newApp,
                        profile.darkModeEnabled,
                        profile.hapticsEnabled
                    )
                }
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            initialDailyTargetMinutes = profile.dailyTargetMinutes,
            initialDarkMode = profile.darkModeEnabled,
            initialHaptics = profile.hapticsEnabled,
            userEmail = userEmail,
            onDismiss = { showSettingsDialog = false },
            onSaveSettings = { targetMins, darkMode, haptics ->
                onUpdateSettings(targetMins, profile.shieldedApps, darkMode, haptics)
            },
            onSignOut = onSignOut
        )
    }
}

@Composable
private fun VaultKpiCard(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
