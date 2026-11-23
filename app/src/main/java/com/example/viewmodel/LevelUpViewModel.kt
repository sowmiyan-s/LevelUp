package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiFocusCoachRepository
import com.example.data.LevelUpRepository
import com.example.model.AiCoachState
import com.example.model.AiPlanSuggestion
import com.example.model.AiSuggestedGoal
import com.example.model.CalendarDaySummary
import com.example.model.ChatMessage
import com.example.model.DailyGoal
import com.example.model.DateKeyUtils
import com.example.model.DayStatus
import com.example.model.DayTier
import com.example.model.GoalTier
import com.example.model.TaskLog
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.model.WorkCategory
import com.example.model.WorkSession
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

private const val TAG = "LevelUpVM"
private const val STOP_TIMEOUT_MILLIS = 5000L

enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED
}

data class TimerUiState(
    val status: TimerStatus = TimerStatus.IDLE,
    val plannedMinutes: Int = 45,
    val elapsedSeconds: Int = 0,
    val startMinuteOfDay: Int = DateKeyUtils.currentMinuteOfDay(),
    val selectedCategory: WorkCategory = WorkCategory.DEEP_WORK,
    val linkedGoalId: String? = null,
    val shieldOverlayExpanded: Boolean = false,
    val pendingLogMinutes: Int? = null
)

data class DashboardSummary(
    val profile: UserProfile = UserProfile(),
    val goals: List<DailyGoal> = emptyList(),
    val sessions: List<WorkSession> = emptyList(),
    val dayStatuses: List<DayStatus> = emptyList(),
    val taskLogs: List<TaskLog> = emptyList(),
    val todayFocusMinutes: Int = 0,
    val todayCompletionRatio: Float = 0f,
    val isSuperDayToday: Boolean = false,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val superDaysCount: Int = 0,
    val totalFocusMinutes: Int = 0
)

class LevelUpViewModel(
    private val repository: LevelUpRepository,
    private val aiRepository: AiFocusCoachRepository,
    private val currentUserId: String,
    private val initialDisplayName: String
) : ViewModel() {

    private val profileFlow: StateFlow<UiState<UserProfile>> =
        repository.observeUserProfile(currentUserId)
            .map<UserProfile?, UiState<UserProfile>> { profile ->
                UiState.Success(profile ?: UserProfile(userId = currentUserId, displayName = initialDisplayName))
            }
            .catch { error ->
                val firestoreError = unwrapFirestoreException(error)
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing profile", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load profile"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    private val goalsFlow: StateFlow<UiState<List<DailyGoal>>> =
        repository.observeGoals(currentUserId)
            .map<List<DailyGoal>, UiState<List<DailyGoal>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = unwrapFirestoreException(error)
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing goals", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load goals"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    private val sessionsFlow: StateFlow<UiState<List<WorkSession>>> =
        repository.observeSessions(currentUserId)
            .map<List<WorkSession>, UiState<List<WorkSession>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = unwrapFirestoreException(error)
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing sessions", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load sessions"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    private val dayStatusesFlow: StateFlow<UiState<List<DayStatus>>> =
        repository.observeDayStatuses(currentUserId)
            .map<List<DayStatus>, UiState<List<DayStatus>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = unwrapFirestoreException(error)
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing day statuses", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load calendar statuses"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    private val taskLogsFlow: StateFlow<UiState<List<TaskLog>>> =
        repository.observeTaskLogs(currentUserId)
            .map<List<TaskLog>, UiState<List<TaskLog>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = unwrapFirestoreException(error)
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing task logs", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load task logs"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val dashboardState: StateFlow<UiState<DashboardSummary>> = combine(
        profileFlow,
        goalsFlow,
        sessionsFlow,
        dayStatusesFlow,
        taskLogsFlow
    ) { profileState, goalsState, sessionsState, statusesState, taskLogsState ->
        val firstError = listOf(profileState, goalsState, sessionsState, statusesState, taskLogsState)
            .filterIsInstance<UiState.Error>()
            .firstOrNull()
        if (firstError != null) {
            UiState.Error(firstError.message)
        } else if (
            profileState is UiState.Success &&
            goalsState is UiState.Success &&
            sessionsState is UiState.Success &&
            statusesState is UiState.Success &&
            taskLogsState is UiState.Success
        ) {
            UiState.Success(
                buildDashboardSummary(
                    profile = profileState.data,
                    goals = goalsState.data,
                    sessions = sessionsState.data,
                    dayStatuses = statusesState.data,
                    taskLogs = taskLogsState.data
                )
            )
        } else {
            UiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading
    )

    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    private val _briefingState = MutableStateFlow<AiCoachState>(AiCoachState.Idle)
    val briefingState: StateFlow<AiCoachState> = _briefingState.asStateFlow()

    private val _planState = MutableStateFlow<UiState<AiPlanSuggestion?>?>(null)
    val planState: StateFlow<UiState<AiPlanSuggestion?>?> = _planState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _coachReplyState = MutableStateFlow<AiCoachState>(AiCoachState.Idle)
    val coachReplyState: StateFlow<AiCoachState> = _coachReplyState.asStateFlow()

    private val _isSavingTaskLog = MutableStateFlow(false)
    val isSavingTaskLog: StateFlow<Boolean> = _isSavingTaskLog.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val result = repository.ensureUserProfile(currentUserId, initialDisplayName)
            if (result.isFailure) {
                _feedbackMessage.value =
                    result.exceptionOrNull()?.localizedMessage ?: "Failed to initialize profile."
            }
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    // --- Timer & Focus Shield Controls ---

    fun setPlannedMinutes(minutes: Int) {
        if (_timerState.value.status == TimerStatus.IDLE) {
            _timerState.value = _timerState.value.copy(
                plannedMinutes = minutes.coerceIn(5, 180)
            )
        }
    }

    fun selectFocusCategory(category: WorkCategory) {
        _timerState.value = _timerState.value.copy(selectedCategory = category)
    }

    fun selectLinkedGoal(goalId: String?) {
        _timerState.value = _timerState.value.copy(linkedGoalId = goalId)
    }

    fun startStudyMode() {
        val current = _timerState.value
        if (current.status == TimerStatus.RUNNING) return
        val startMinute = if (current.status == TimerStatus.IDLE) {
            DateKeyUtils.currentMinuteOfDay()
        } else {
            current.startMinuteOfDay
        }
        _timerState.value = current.copy(
            status = TimerStatus.RUNNING,
            startMinuteOfDay = startMinute
        )
        startTicker()
    }

    fun pauseStudyMode() {
        timerJob?.cancel()
        timerJob = null
        if (_timerState.value.status == TimerStatus.RUNNING) {
            _timerState.value = _timerState.value.copy(status = TimerStatus.PAUSED)
        }
    }

    fun requestStopAndLog() {
        timerJob?.cancel()
        timerJob = null
        val elapsedMins = (_timerState.value.elapsedSeconds / 60).coerceAtLeast(1)
        _timerState.value = _timerState.value.copy(
            status = TimerStatus.PAUSED,
            shieldOverlayExpanded = false,
            pendingLogMinutes = elapsedMins
        )
    }

    fun dismissLogDialogAndReset() {
        timerJob?.cancel()
        timerJob = null
        _timerState.value = TimerUiState(
            plannedMinutes = _timerState.value.plannedMinutes,
            selectedCategory = _timerState.value.selectedCategory
        )
    }

    fun cancelStopAndResume() {
        _timerState.value = _timerState.value.copy(pendingLogMinutes = null)
        startStudyMode()
    }

    fun setShieldOverlayExpanded(expanded: Boolean) {
        _timerState.value = _timerState.value.copy(shieldOverlayExpanded = expanded)
    }

    fun emergencyUnlockAndReset() {
        timerJob?.cancel()
        timerJob = null
        _timerState.value = TimerUiState(
            plannedMinutes = _timerState.value.plannedMinutes,
            selectedCategory = _timerState.value.selectedCategory
        )
        _feedbackMessage.value = "Focus Shield unlocked early. Session was not logged."
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _timerState.value
                if (current.status != TimerStatus.RUNNING) break
                _timerState.value = current.copy(elapsedSeconds = current.elapsedSeconds + 1)
            }
        }
    }

    // --- Firestore CRUD ---

    fun addGoal(
        dateKey: String,
        title: String,
        targetMinutes: Int,
        category: String,
        tier: GoalTier
    ) {
        viewModelScope.launch {
            val result = repository.addGoal(
                userId = currentUserId,
                dateKey = dateKey,
                title = title,
                targetMinutes = targetMinutes,
                category = category,
                tier = tier
            )
            if (result.isFailure) {
                _feedbackMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to add goal."
            }
        }
    }

    fun toggleGoal(goal: DailyGoal) {
        viewModelScope.launch {
            val result = repository.toggleGoalCompleted(
                userId = currentUserId,
                goalId = goal.id,
                completed = !goal.completed
            )
            if (result.isFailure) {
                _feedbackMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to update goal."
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            val result = repository.deleteGoal(currentUserId, goalId)
            if (result.isFailure) {
                _feedbackMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete goal."
            }
        }
    }

    fun logWorkSession(
        dateKey: String,
        title: String,
        category: String,
        startMinuteOfDay: Int,
        durationMinutes: Int,
        tags: List<String>,
        subNotes: String,
        goalId: String?,
        shieldActive: Boolean,
        fromActiveTimer: Boolean
    ) {
        viewModelScope.launch {
            val result = repository.addSession(
                userId = currentUserId,
                dateKey = dateKey,
                title = title,
                category = category,
                startMinuteOfDay = startMinuteOfDay,
                durationMinutes = durationMinutes,
                tags = tags,
                subNotes = subNotes,
                goalId = goalId,
                shieldActive = shieldActive
            )
            if (result.isSuccess) {
                if (fromActiveTimer) {
                    dismissLogDialogAndReset()
                }
                _feedbackMessage.value = "Session logged (${DateKeyUtils.formatDurationMinutes(durationMinutes)})."
            } else {
                _feedbackMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to save work session."
            }
        }
    }

    fun updateSessionNotes(sessionId: String, subNotes: String) {
        viewModelScope.launch {
            val result = repository.updateSessionNotes(currentUserId, sessionId, subNotes)
            if (result.isFailure) {
                _feedbackMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to update notes."
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            val result = repository.deleteSession(currentUserId, sessionId)
            if (result.isFailure) {
                _feedbackMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete session."
            }
        }
    }

    fun updateProfileSettings(
        dailyTargetMinutes: Long,
        shieldedApps: List<String>,
        darkModeEnabled: Boolean,
        hapticsEnabled: Boolean
    ) {
        viewModelScope.launch {
            val result = repository.updateProfileSettings(
                userId = currentUserId,
                dailyTargetMinutes = dailyTargetMinutes,
                shieldedApps = shieldedApps,
                darkModeEnabled = darkModeEnabled,
                hapticsEnabled = hapticsEnabled
            )
            if (result.isFailure) {
                _feedbackMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to update settings."
            }
        }
    }

    fun applyStreakFreeze(dateKey: String) {
        viewModelScope.launch {
            val result = repository.applyStreakFreeze(currentUserId, dateKey)
            _feedbackMessage.value = if (result.isSuccess) {
                "Streak Freeze applied to ${DateKeyUtils.displayDate(dateKey)}."
            } else {
                result.exceptionOrNull()?.localizedMessage ?: "Unable to apply Streak Freeze."
            }
        }
    }

    fun addTaskLog(
        workDone: String,
        category: String,
        durationMinutes: Int?,
        onSuccess: () -> Unit
    ) {
        if (_isSavingTaskLog.value) return
        viewModelScope.launch {
            _isSavingTaskLog.value = true
            try {
                val result = repository.addTaskLog(
                    userId = currentUserId,
                    workDone = workDone,
                    category = category,
                    durationMinutes = durationMinutes
                )
                if (result.isSuccess) {
                    _feedbackMessage.value = "Work log saved."
                    onSuccess()
                } else {
                    _feedbackMessage.value =
                        result.exceptionOrNull()?.localizedMessage ?: "Failed to save work log."
                }
            } finally {
                _isSavingTaskLog.value = false
            }
        }
    }

    fun deleteTaskLog(logId: String) {
        viewModelScope.launch {
            val result = repository.deleteTaskLog(currentUserId, logId)
            if (result.isFailure) {
                _feedbackMessage.value =
                    result.exceptionOrNull()?.localizedMessage ?: "Failed to delete work log."
            }
        }
    }

    // --- Hero AI Focus Coach (Firebase AI Logic) ---

    fun generateHeroBriefing() {
        val summary = (dashboardState.value as? UiState.Success)?.data ?: DashboardSummary()
        val telemetry = buildTelemetryContext(summary)

        viewModelScope.launch {
            _briefingState.value = AiCoachState.Loading
            try {
                var latestText = ""
                aiRepository.streamFocusBriefing(telemetry).collect { chunk ->
                    latestText = chunk
                    _briefingState.value = AiCoachState.Streaming(chunk)
                }
                _briefingState.value = if (latestText.isNotBlank()) {
                    AiCoachState.Success(latestText)
                } else {
                    AiCoachState.Error("AI Focus Coach returned an empty response.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Briefing timed out", e)
                _briefingState.value = AiCoachState.Error("Focus briefing timed out after 60s. Tap retry.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Briefing failed", e)
                _briefingState.value = AiCoachState.Error(e.localizedMessage ?: "Unable to reach Gemini AI Coach.")
            }
        }
    }

    fun generateAiDayPlan(objective: String) {
        val cleanObjective = objective.trim().ifEmpty {
            "Maximize deep work progress on my core coding, study, and health priorities today."
        }
        val summary = (dashboardState.value as? UiState.Success)?.data ?: DashboardSummary()
        val telemetry = buildTelemetryContext(summary)

        viewModelScope.launch {
            _planState.value = UiState.Loading
            val result = aiRepository.generateStructuredPlan(cleanObjective, telemetry)
            _planState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.localizedMessage ?: "Failed to generate focus plan.") }
            )
        }
    }

    fun clearAiPlan() {
        _planState.value = null
    }

    fun adoptSuggestedGoal(suggested: AiSuggestedGoal) {
        addGoal(
            dateKey = DateKeyUtils.todayKey(),
            title = suggested.title,
            targetMinutes = suggested.targetMinutes,
            category = suggested.category,
            tier = suggested.tier
        )
        _feedbackMessage.value = "Added \"${suggested.title}\" to Today's Goals."
    }

    fun sendCoachMessage(question: String) {
        val trimmed = question.trim()
        if (trimmed.isEmpty()) return

        val summary = (dashboardState.value as? UiState.Success)?.data ?: DashboardSummary()
        val telemetry = buildTelemetryContext(summary)
        val userMsg = ChatMessage(id = UUID.randomUUID().toString(), isUser = true, text = trimmed)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _coachReplyState.value = AiCoachState.Loading
            try {
                var latest = ""
                aiRepository.streamCoachResponse(trimmed, telemetry).collect { text ->
                    latest = text
                    _coachReplyState.value = AiCoachState.Streaming(text)
                }
                if (latest.isNotBlank()) {
                    val botMsg = ChatMessage(id = UUID.randomUUID().toString(), isUser = false, text = latest)
                    _chatMessages.value = _chatMessages.value + botMsg
                    _coachReplyState.value = AiCoachState.Idle
                } else {
                    _coachReplyState.value = AiCoachState.Error("Model returned an empty reply.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Coach chat timed out", e)
                _coachReplyState.value = AiCoachState.Error("Coach reply timed out after 60s.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Coach chat failed", e)
                _coachReplyState.value = AiCoachState.Error(e.localizedMessage ?: "Coach reply failed.")
            }
        }
    }

    // --- Analytics & Calendar Helpers ---

    fun buildCalendarMonth(
        summary: DashboardSummary,
        year: Int,
        monthZeroBased: Int
    ): List<CalendarDaySummary> {
        val today = DateKeyUtils.todayKey()
        val firstDay = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Monday = 1 .. Sunday = 7
        val dow = firstDay.get(Calendar.DAY_OF_WEEK)
        val mondayOffset = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
        val cursor = (firstDay.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, -mondayOffset)
        }

        val goalsByDay = summary.goals.groupBy { it.dateKey }
        val sessionsByDay = summary.sessions.groupBy { it.dateKey }
        val statusByDay = summary.dayStatuses.associateBy { it.dateKey }

        return buildList {
            repeat(42) {
                val key = DateKeyUtils.formatKey(cursor)
                val inMonth = cursor.get(Calendar.MONTH) == monthZeroBased
                val dayGoals = goalsByDay[key].orEmpty()
                val daySessions = sessionsByDay[key].orEmpty()
                val freezeUsed = statusByDay[key]?.freezeUsed == true

                val essential = dayGoals.filter { it.resolvedTier() == GoalTier.ESSENTIAL }
                val optional = dayGoals.filter { it.resolvedTier() == GoalTier.OPTIONAL }
                val essDone = essential.count { it.completed }
                val optDone = optional.count { it.completed }
                val focusMins = daySessions.sumOf { it.durationMinutes.toInt() }
                val ratio = if (essential.isNotEmpty()) {
                    essDone.toFloat() / essential.size.toFloat()
                } else if (focusMins > 0) {
                    (focusMins.toFloat() / summary.profile.dailyTargetMinutes.coerceAtLeast(60L).toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }

                val tier = computeDayTier(
                    dateKey = key,
                    todayKey = today,
                    essentialCount = essential.size,
                    essentialDone = essDone,
                    optionalCount = optional.size,
                    optionalDone = optDone,
                    focusMinutes = focusMins,
                    ratio = ratio,
                    freezeUsed = freezeUsed
                )

                add(
                    CalendarDaySummary(
                        dateKey = key,
                        dayOfMonth = cursor.get(Calendar.DAY_OF_MONTH),
                        inCurrentMonth = inMonth,
                        tier = tier,
                        completionRatio = ratio,
                        essentialCompleted = essDone,
                        essentialTotal = essential.size,
                        optionalCompleted = optDone,
                        optionalTotal = optional.size,
                        focusMinutes = focusMins,
                        freezeUsed = freezeUsed
                    )
                )
                cursor.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    private fun buildDashboardSummary(
        profile: UserProfile,
        goals: List<DailyGoal>,
        sessions: List<WorkSession>,
        dayStatuses: List<DayStatus>,
        taskLogs: List<TaskLog>
    ): DashboardSummary {
        val today = DateKeyUtils.todayKey()
        val todayGoals = goals.filter { it.dateKey == today }
        val todaySessions = sessions.filter { it.dateKey == today }
        val todayMinutes = todaySessions.sumOf { it.durationMinutes.toInt() }

        val essentialToday = todayGoals.filter { it.resolvedTier() == GoalTier.ESSENTIAL }
        val optionalToday = todayGoals.filter { it.resolvedTier() == GoalTier.OPTIONAL }
        val essDone = essentialToday.count { it.completed }
        val optDone = optionalToday.count { it.completed }

        val ratio = if (essentialToday.isNotEmpty()) {
            essDone.toFloat() / essentialToday.size.toFloat()
        } else if (todayMinutes > 0) {
            (todayMinutes.toFloat() / profile.dailyTargetMinutes.coerceAtLeast(60L).toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        val superToday = essentialToday.isNotEmpty() &&
            essDone == essentialToday.size &&
            optionalToday.isNotEmpty() &&
            optDone == optionalToday.size

        // Compute streaks across recorded days up to today
        val allKeys = (goals.map { it.dateKey } + sessions.map { it.dateKey } + dayStatuses.map { it.dateKey } + today)
            .distinct()
            .sorted()

        val goalsByDay = goals.groupBy { it.dateKey }
        val sessionsByDay = sessions.groupBy { it.dateKey }
        val freezeByDay = dayStatuses.associateBy { it.dateKey }

        var superDays = 0
        val qualifyingSet = mutableSetOf<String>()

        allKeys.forEach { key ->
            val dg = goalsByDay[key].orEmpty()
            val ds = sessionsByDay[key].orEmpty()
            val freeze = freezeByDay[key]?.freezeUsed == true
            val ess = dg.filter { it.resolvedTier() == GoalTier.ESSENTIAL }
            val opt = dg.filter { it.resolvedTier() == GoalTier.OPTIONAL }
            val eDone = ess.count { it.completed }
            val oDone = opt.count { it.completed }
            val mins = ds.sumOf { it.durationMinutes.toInt() }
            val r = if (ess.isNotEmpty()) eDone.toFloat() / ess.size.toFloat() else 0f
            val tier = computeDayTier(key, today, ess.size, eDone, opt.size, oDone, mins, r, freeze)
            if (tier == DayTier.SUPER_DAY) superDays++
            if (tier == DayTier.SUPER_DAY || tier == DayTier.TARGET_ACHIEVED || tier == DayTier.PARTIAL_PROGRESS || tier == DayTier.STREAK_FREEZE) {
                qualifyingSet.add(key)
            }
        }

        var streak = 0
        var cursorKey = if (qualifyingSet.contains(today)) today else DateKeyUtils.shiftDays(today, -1)
        while (qualifyingSet.contains(cursorKey)) {
            streak++
            cursorKey = DateKeyUtils.shiftDays(cursorKey, -1)
        }

        // Best streak
        var best = streak
        var running = 0
        var prev: String? = null
        qualifyingSet.sorted().forEach { key ->
            if (prev != null && DateKeyUtils.shiftDays(prev!!, 1) == key) {
                running++
            } else {
                running = 1
            }
            if (running > best) best = running
            prev = key
        }

        return DashboardSummary(
            profile = profile,
            goals = goals,
            sessions = sessions,
            dayStatuses = dayStatuses,
            taskLogs = taskLogs,
            todayFocusMinutes = todayMinutes,
            todayCompletionRatio = ratio,
            isSuperDayToday = superToday,
            currentStreak = streak,
            bestStreak = best,
            superDaysCount = superDays,
            totalFocusMinutes = sessions.sumOf { it.durationMinutes.toInt() }
        )
    }

    private fun computeDayTier(
        dateKey: String,
        todayKey: String,
        essentialCount: Int,
        essentialDone: Int,
        optionalCount: Int,
        optionalDone: Int,
        focusMinutes: Int,
        ratio: Float,
        freezeUsed: Boolean
    ): DayTier {
        if (dateKey > todayKey) return DayTier.UPCOMING
        if (freezeUsed) return DayTier.STREAK_FREEZE
        if (essentialCount == 0 && optionalCount == 0 && focusMinutes == 0) return DayTier.NO_GOALS
        if (essentialCount == 0 && focusMinutes > 0) {
            return if (ratio >= 1f) DayTier.TARGET_ACHIEVED else DayTier.PARTIAL_PROGRESS
        }
        val allEssDone = essentialCount > 0 && essentialDone == essentialCount
        val allOptDone = optionalCount > 0 && optionalDone == optionalCount
        return when {
            allEssDone && allOptDone -> DayTier.SUPER_DAY
            allEssDone -> DayTier.TARGET_ACHIEVED
            ratio >= 0.50f -> DayTier.PARTIAL_PROGRESS
            else -> DayTier.NEEDS_ATTENTION
        }
    }

    private fun buildTelemetryContext(summary: DashboardSummary): String {
        val today = DateKeyUtils.todayKey()
        val todayGoals = summary.goals.filter { it.dateKey == today }
        val openGoals = todayGoals.filter { !it.completed }.joinToString("; ") {
            "${it.title} (${it.targetMinutes}m, ${it.category}, ${it.tier})"
        }.ifEmpty { "None yet" }
        val completedGoals = todayGoals.filter { it.completed }.joinToString("; ") { it.title }
            .ifEmpty { "None yet" }
        val todaySessions = summary.sessions.filter { it.dateKey == today }
            .joinToString("; ") { "${it.title} ${it.durationMinutes}m (${it.category})" }
            .ifEmpty { "No sessions logged today yet" }

        return """
            - Date: $today
            - User: ${summary.profile.displayName}
            - Today's Focus Logged: ${summary.todayFocusMinutes} min / ${summary.profile.dailyTargetMinutes} min target
            - Current Streak: ${summary.currentStreak} days (Best: ${summary.bestStreak} days)
            - Completed Goals Today: $completedGoals
            - Remaining Goals Today: $openGoals
            - Logged Sessions Today: $todaySessions
            - Shielded Distraction Apps: ${summary.profile.shieldedApps.joinToString(", ")}
        """.trimIndent()
    }

    private fun unwrapFirestoreException(error: Throwable): FirebaseFirestoreException? =
        generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
