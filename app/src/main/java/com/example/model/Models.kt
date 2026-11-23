package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AiCoachState {
    data object Idle : AiCoachState
    data object Loading : AiCoachState
    data class Streaming(val text: String) : AiCoachState
    data class Success(val text: String) : AiCoachState
    data class Error(val message: String) : AiCoachState
}

enum class GoalTier {
    ESSENTIAL,
    OPTIONAL;

    companion object {
        fun fromRaw(raw: String): GoalTier =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: ESSENTIAL
    }
}

enum class DayTier {
    SUPER_DAY,
    TARGET_ACHIEVED,
    PARTIAL_PROGRESS,
    NEEDS_ATTENTION,
    STREAK_FREEZE,
    NO_GOALS,
    UPCOMING
}

enum class WorkCategory(val label: String, val defaultTag: String) {
    DEEP_WORK("Deep Work", "#deepwork"),
    CODE("Code", "#code"),
    STUDY("Study", "#study"),
    DESIGN("Design", "#design"),
    READING("Reading", "#reading"),
    FITNESS("Fitness", "#fitness");

    companion object {
        fun fromLabel(raw: String): WorkCategory =
            entries.firstOrNull { it.label.equals(raw, ignoreCase = true) } ?: DEEP_WORK
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val dailyTargetMinutes: Long = 360L,
    val streakFreezes: Long = 2L,
    val shieldedApps: List<String> = DEFAULT_SHIELDED_APPS,
    val darkModeEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        val DEFAULT_SHIELDED_APPS = listOf(
            "Instagram",
            "X / Twitter",
            "YouTube",
            "TikTok"
        )
        val AVAILABLE_DISTRACTION_APPS = listOf(
            "Instagram",
            "X / Twitter",
            "YouTube",
            "TikTok",
            "Reddit",
            "Discord",
            "Twitch",
            "Netflix"
        )
    }
}

@Keep
@IgnoreExtraProperties
data class DailyGoal(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateKey: String = "",
    val title: String = "",
    val targetMinutes: Long = 60L,
    val category: String = WorkCategory.DEEP_WORK.label,
    val tier: String = GoalTier.ESSENTIAL.name,
    val completed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun resolvedTier(): GoalTier = GoalTier.fromRaw(tier)
}

@Keep
@IgnoreExtraProperties
data class WorkSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateKey: String = "",
    val title: String = "",
    val category: String = WorkCategory.DEEP_WORK.label,
    val startMinuteOfDay: Long = 540L,
    val durationMinutes: Long = 60L,
    val tags: List<String> = emptyList(),
    val subNotes: String = "",
    val goalId: String? = null,
    val shieldActive: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DayStatus(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateKey: String = "",
    val freezeUsed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class AiSuggestedGoal(
    val title: String,
    val targetMinutes: Int,
    val category: String,
    val tier: GoalTier,
    val rationale: String
)

data class AiPlanSuggestion(
    val headline: String,
    val strategyTip: String,
    val suggestedGoals: List<AiSuggestedGoal>
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String
)

data class CalendarDaySummary(
    val dateKey: String,
    val dayOfMonth: Int,
    val inCurrentMonth: Boolean,
    val tier: DayTier,
    val completionRatio: Float,
    val essentialCompleted: Int,
    val essentialTotal: Int,
    val optionalCompleted: Int,
    val optionalTotal: Int,
    val focusMinutes: Int,
    val freezeUsed: Boolean
)

object DateKeyUtils {
    private val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("EEE, MMM d", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun todayKey(): String = synchronized(keyFormat) {
        keyFormat.format(Calendar.getInstance().time)
    }

    fun formatKey(calendar: Calendar): String = synchronized(keyFormat) {
        keyFormat.format(calendar.time)
    }

    fun parseKey(dateKey: String): Calendar {
        val cal = Calendar.getInstance()
        synchronized(keyFormat) {
            runCatching { keyFormat.parse(dateKey) }.getOrNull()?.let { date ->
                cal.time = date
            }
        }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    fun shiftDays(dateKey: String, deltaDays: Int): String {
        val cal = parseKey(dateKey)
        cal.add(Calendar.DAY_OF_MONTH, deltaDays)
        return formatKey(cal)
    }

    fun displayDate(dateKey: String): String {
        val today = todayKey()
        if (dateKey == today) return "Today"
        if (dateKey == shiftDays(today, -1)) return "Yesterday"
        val cal = parseKey(dateKey)
        return synchronized(displayFormat) { displayFormat.format(cal.time) }
    }

    fun monthYearLabel(year: Int, monthZeroBased: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return synchronized(monthYearFormat) { monthYearFormat.format(cal.time) }
    }

    fun currentMinuteOfDay(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

    fun formatMinuteOfDay(minuteOfDay: Int): String {
        val clamped = minuteOfDay.coerceIn(0, 1439)
        val hour24 = clamped / 60
        val minute = clamped % 60
        val amPm = if (hour24 < 12) "AM" else "PM"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return String.format(Locale.US, "%02d:%02d %s", hour12, minute, amPm)
    }

    fun formatDurationMinutes(minutes: Int): String {
        val safe = minutes.coerceAtLeast(0)
        val hrs = safe / 60
        val mins = safe % 60
        return when {
            hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
            hrs > 0 -> "${hrs}h"
            else -> "${mins}m"
        }
    }

    fun formatTimerSeconds(totalSeconds: Int): String {
        val safe = totalSeconds.coerceAtLeast(0)
        val hrs = safe / 3600
        val mins = (safe % 3600) / 60
        val secs = safe % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    }
}
