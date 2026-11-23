package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.viewmodel.DashboardSummary

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
    InsightsScreen(
        summary = summary,
        userEmail = userEmail,
        onUpdateSettings = onUpdateSettings,
        onSignOut = onSignOut,
        modifier = modifier
    )
}
