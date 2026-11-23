package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auth.AuthScreen
import com.example.auth.attemptAutoSignIn
import com.example.auth.signOut
import com.example.data.AiFocusCoachRepository
import com.example.data.LevelUpRepository
import com.example.firebase.configureAppCheck
import com.example.model.DateKeyUtils
import com.example.model.UiState
import com.example.ui.components.AddGoalDialog
import com.example.ui.components.AiCoachChatBottomSheet
import com.example.ui.components.FocusShieldOverlayDialog
import com.example.ui.components.LogSessionDialog
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.StatsAndShieldScreen
import com.example.ui.screens.TimelineScreen
import com.example.ui.theme.Emerald500
import com.example.ui.theme.LevelUpTheme
import com.example.ui.theme.Mint400
import com.example.viewmodel.DashboardSummary
import com.example.viewmodel.LevelUpViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private enum class LevelUpDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Home", Icons.Rounded.Dashboard, Icons.Outlined.Dashboard),
    TIMELINE("timeline", "Timeline", Icons.Rounded.Schedule, Icons.Outlined.Schedule),
    CALENDAR("calendar", "Calendar", Icons.Rounded.CalendarMonth, Icons.Outlined.CalendarMonth),
    STATS("stats", "Stats", Icons.Rounded.Analytics, Icons.Outlined.Analytics)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Must run before setContent, ViewModels, or any Firebase operation.
        configureAppCheck(this, intent)

        setContent {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isGuestMode by remember { mutableStateOf(false) }
    var currentUser by remember {
        mutableStateOf(
            try {
                Firebase.auth.currentUser
            } catch (e: Exception) {
                null
            }
        )
    }
    var checkingAutoSignIn by remember { mutableStateOf(currentUser == null && !isGuestMode) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                checkingAutoSignIn = false
            }
        }
        try {
            Firebase.auth.addAuthStateListener(listener)
        } catch (_: Exception) {}
        onDispose {
            try {
                Firebase.auth.removeAuthStateListener(listener)
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null && !isGuestMode) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { checkingAutoSignIn = false },
                onUnauthenticated = { checkingAutoSignIn = false },
                scope = scope
            )
        }
    }

    val user = currentUser
    if (user == null && !isGuestMode) {
        LevelUpTheme(darkTheme = true) {
            if (checkingAutoSignIn) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Mint400)
                }
            } else {
                AuthScreen(
                    onAuthSuccess = {
                        currentUser = try {
                            Firebase.auth.currentUser
                        } catch (_: Exception) {
                            null
                        }
                    },
                    onContinueAsGuest = {
                        isGuestMode = true
                    }
                )
            }
        }
    } else {
        val userId = user?.uid ?: "guest_focus_builder"
        val displayName = user?.displayName ?: "Guest Explorer"
        val userEmail = user?.email
        key(userId) {
            AuthenticatedLevelUpApp(
                currentUserId = userId,
                displayName = displayName,
                userEmail = userEmail,
                onSignOut = {
                    if (isGuestMode) {
                        isGuestMode = false
                    } else {
                        signOut(
                            credentialManager = credentialManager,
                            scope = scope
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedLevelUpApp(
    currentUserId: String,
    displayName: String,
    userEmail: String?,
    onSignOut: () -> Unit
) {
    val viewModel: LevelUpViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = try {
                    app.getString(R.string.firestore_database_id)
                } catch (_: Exception) {
                    null
                }
                val db = try {
                    if (!databaseId.isNullOrBlank()) {
                        FirebaseFirestore.getInstance(databaseId)
                    } else {
                        FirebaseFirestore.getInstance()
                    }
                } catch (_: Exception) {
                    FirebaseFirestore.getInstance()
                }
                LevelUpViewModel(
                    repository = LevelUpRepository(db),
                    aiRepository = AiFocusCoachRepository(),
                    currentUserId = currentUserId,
                    initialDisplayName = displayName
                )
            }
        }
    )

    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val briefingState by viewModel.briefingState.collectAsStateWithLifecycle()
    val planState by viewModel.planState.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val coachReplyState by viewModel.coachReplyState.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    val darkTheme = (dashboardState as? UiState.Success)?.data?.profile?.darkModeEnabled ?: true
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        val msg = feedbackMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var manualLogDateKey by remember { mutableStateOf<String?>(null) }
    var showCoachChatSheet by remember { mutableStateOf(false) }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: LevelUpDestination.HOME.route

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(LevelUpDestination.HOME.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    LevelUpTheme(darkTheme = darkTheme) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (!useNavRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            LevelUpDestination.entries.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Emerald500,
                                        selectedTextColor = Emerald500,
                                        indicatorColor = Emerald500.copy(alpha = 0.16f)
                                    ),
                                    modifier = Modifier.testTag("nav_${dest.route}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxHeight()
                                .windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            Spacer(Modifier.height(12.dp))
                            LevelUpDestination.entries.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = Emerald500,
                                        selectedTextColor = Emerald500,
                                        indicatorColor = Emerald500.copy(alpha = 0.16f)
                                    ),
                                    modifier = Modifier.testTag("rail_${dest.route}")
                                )
                            }
                        }
                    }

                    when (val state = dashboardState) {
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Mint400)
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "Syncing your focus telemetry...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        is UiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "Unable to sync data",
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                        Text(
                                            text = state.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Button(onClick = onSignOut) {
                                            Text("Sign Out & Retry")
                                        }
                                    }
                                }
                            }
                        }

                        is UiState.Success -> {
                            val summary: DashboardSummary = state.data
                            NavHost(
                                navController = navController,
                                startDestination = LevelUpDestination.HOME.route,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable(LevelUpDestination.HOME.route) {
                                    HomeDashboardScreen(
                                        summary = summary,
                                        timerState = timerState,
                                        briefingState = briefingState,
                                        planState = planState,
                                        onSelectMinutes = viewModel::setPlannedMinutes,
                                        onSelectCategory = viewModel::selectFocusCategory,
                                        onStartStudyMode = viewModel::startStudyMode,
                                        onPauseStudyMode = viewModel::pauseStudyMode,
                                        onStopAndLog = viewModel::requestStopAndLog,
                                        onOpenShieldOverlay = { viewModel.setShieldOverlayExpanded(true) },
                                        onOpenAddGoal = { showAddGoalDialog = true },
                                        onToggleGoal = viewModel::toggleGoal,
                                        onDeleteGoal = viewModel::deleteGoal,
                                        onGenerateBriefing = viewModel::generateHeroBriefing,
                                        onGeneratePlan = viewModel::generateAiDayPlan,
                                        onAdoptSuggestedGoal = viewModel::adoptSuggestedGoal,
                                        onClearPlan = viewModel::clearAiPlan,
                                        onOpenCoachChat = { showCoachChatSheet = true },
                                        onNavigateToShieldSettings = { navigateTo(LevelUpDestination.STATS.route) }
                                    )
                                }

                                composable(LevelUpDestination.TIMELINE.route) {
                                    TimelineScreen(
                                        summary = summary,
                                        onOpenManualLog = { dateKey -> manualLogDateKey = dateKey },
                                        onUpdateSessionNotes = viewModel::updateSessionNotes,
                                        onDeleteSession = viewModel::deleteSession
                                    )
                                }

                                composable(LevelUpDestination.CALENDAR.route) {
                                    CalendarScreen(
                                        summary = summary,
                                        buildMonthCells = viewModel::buildCalendarMonth,
                                        onApplyStreakFreeze = viewModel::applyStreakFreeze
                                    )
                                }

                                composable(LevelUpDestination.STATS.route) {
                                    StatsAndShieldScreen(
                                        summary = summary,
                                        userEmail = userEmail,
                                        onUpdateSettings = viewModel::updateProfileSettings,
                                        onSignOut = onSignOut
                                    )
                                }
                            }

                            // Immersive Focus Shield Overlay
                            if (timerState.shieldOverlayExpanded) {
                                FocusShieldOverlayDialog(
                                    elapsedSeconds = timerState.elapsedSeconds,
                                    plannedMinutes = timerState.plannedMinutes,
                                    shieldedApps = summary.profile.shieldedApps,
                                    onMinimize = { viewModel.setShieldOverlayExpanded(false) },
                                    onEmergencyUnlock = viewModel::emergencyUnlockAndReset
                                )
                            }
                        }
                    }
                }
            }

            // Dialogs & Sheets
            if (showAddGoalDialog) {
                AddGoalDialog(
                    onDismiss = { showAddGoalDialog = false },
                    onSave = { title, minutes, category, tier ->
                        viewModel.addGoal(
                            dateKey = DateKeyUtils.todayKey(),
                            title = title,
                            targetMinutes = minutes,
                            category = category,
                            tier = tier
                        )
                        showAddGoalDialog = false
                    }
                )
            }

            val pendingTimerMins = timerState.pendingLogMinutes
            if (pendingTimerMins != null) {
                LogSessionDialog(
                    initialTitle = "${timerState.selectedCategory.label} Focus Sprint",
                    initialDurationMinutes = pendingTimerMins,
                    initialStartMinute = timerState.startMinuteOfDay,
                    initialCategory = timerState.selectedCategory,
                    onDismiss = viewModel::dismissLogDialogAndReset,
                    onSave = { title, category, startMin, durationMins, tags, notes ->
                        viewModel.logWorkSession(
                            dateKey = DateKeyUtils.todayKey(),
                            title = title,
                            category = category,
                            startMinuteOfDay = startMin,
                            durationMinutes = durationMins,
                            tags = tags,
                            subNotes = notes,
                            goalId = timerState.linkedGoalId,
                            shieldActive = true,
                            fromActiveTimer = true
                        )
                    }
                )
            }

            val logDateKey = manualLogDateKey
            if (logDateKey != null) {
                LogSessionDialog(
                    initialTitle = "",
                    initialDurationMinutes = 60,
                    initialStartMinute = DateKeyUtils.currentMinuteOfDay(),
                    initialCategory = timerState.selectedCategory,
                    onDismiss = { manualLogDateKey = null },
                    onSave = { title, category, startMin, durationMins, tags, notes ->
                        viewModel.logWorkSession(
                            dateKey = logDateKey,
                            title = title,
                            category = category,
                            startMinuteOfDay = startMin,
                            durationMinutes = durationMins,
                            tags = tags,
                            subNotes = notes,
                            goalId = null,
                            shieldActive = false,
                            fromActiveTimer = false
                        )
                        manualLogDateKey = null
                    }
                )
            }

            if (showCoachChatSheet) {
                AiCoachChatBottomSheet(
                    messages = chatMessages,
                    replyState = coachReplyState,
                    onSendMessage = viewModel::sendCoachMessage,
                    onDismiss = { showCoachChatSheet = false }
                )
            }
        }
    }
}
