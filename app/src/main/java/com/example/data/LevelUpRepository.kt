package com.example.data

import android.content.Context
import com.example.R
import com.example.config.AppConfig
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.DailyGoal
import com.example.model.DateKeyUtils
import com.example.model.DayStatus
import com.example.model.GoalTier
import com.example.model.UserProfile
import com.example.model.WorkSession
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class LevelUpRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    companion object {
        const val GUEST_USER_ID = "guest_focus_builder"
    }

    private val guestProfile = MutableStateFlow(
        UserProfile(
            id = GUEST_USER_ID,
            userId = GUEST_USER_ID,
            displayName = "Guest Explorer",
            dailyTargetMinutes = 180,
            streakFreezes = 2
        )
    )

    private val guestGoals = MutableStateFlow(
        listOf(
            DailyGoal(
                id = "goal_demo_1",
                userId = GUEST_USER_ID,
                dateKey = DateKeyUtils.todayKey(),
                title = "Deep Work: Architecture & Core Systems",
                targetMinutes = 60,
                category = "Deep Work",
                tier = GoalTier.ESSENTIAL.name,
                completed = false
            ),
            DailyGoal(
                id = "goal_demo_2",
                userId = GUEST_USER_ID,
                dateKey = DateKeyUtils.todayKey(),
                title = "Bio-Tech UI Polish & Focus Dial Test",
                targetMinutes = 45,
                category = "Code",
                tier = GoalTier.OPTIONAL.name,
                completed = false
            )
        )
    )

    private val guestSessions = MutableStateFlow<List<WorkSession>>(emptyList())
    private val guestDayStatuses = MutableStateFlow<List<DayStatus>>(emptyList())

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUserId: String? = null): String {
        val uid = auth.currentUser?.uid ?: expectedUserId
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
        if (auth.currentUser != null && expectedUserId != null) {
            check(auth.currentUser?.uid == expectedUserId) { "Signed-in user changed; refusing cross-user operation." }
        }
        return uid
    }

    private fun userDoc(uid: String) = db.collection(AppConfig.Firestore.COLLECTION_USERS).document(uid)

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        if (userId == GUEST_USER_ID) {
            emitAll(guestProfile.asStateFlow())
            return@flow
        }
        val uid = requireUserId(userId)
        val docRef = userDoc(uid)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    snapshot.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeGoals(userId: String): Flow<List<DailyGoal>> = flow {
        if (userId == GUEST_USER_ID) {
            emitAll(guestGoals.asStateFlow())
            return@flow
        }
        val uid = requireUserId(userId)
        val colRef = userDoc(uid).collection(AppConfig.Firestore.COLLECTION_GOALS)
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        DailyGoal::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeSessions(userId: String): Flow<List<WorkSession>> = flow {
        if (userId == GUEST_USER_ID) {
            emitAll(guestSessions.asStateFlow())
            return@flow
        }
        val uid = requireUserId(userId)
        val colRef = userDoc(uid).collection(AppConfig.Firestore.COLLECTION_SESSIONS)
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        WorkSession::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeDayStatuses(userId: String): Flow<List<DayStatus>> = flow {
        if (userId == GUEST_USER_ID) {
            emitAll(guestDayStatuses.asStateFlow())
            return@flow
        }
        val uid = requireUserId(userId)
        val colRef = userDoc(uid).collection(AppConfig.Firestore.COLLECTION_DAY_STATUSES)
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        DayStatus::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(userId: String, displayName: String): Result<Unit> {
        if (userId == GUEST_USER_ID) {
            return Result.success(Unit)
        }
        var docPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val docRef = userDoc(uid)
            docPath = docRef.path
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val safeName = displayName.trim().ifEmpty { "Focus Builder" }.take(80)
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "dailyTargetMinutes" to 360L,
                    "streakFreezes" to 2L,
                    "shieldedApps" to UserProfile.DEFAULT_SHIELDED_APPS,
                    "darkModeEnabled" to true,
                    "hapticsEnabled" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateProfileSettings(
        userId: String,
        dailyTargetMinutes: Long,
        shieldedApps: List<String>,
        darkModeEnabled: Boolean,
        hapticsEnabled: Boolean
    ): Result<Unit> {
        if (userId == GUEST_USER_ID) {
            val safeTarget = dailyTargetMinutes.coerceIn(30L, 960L)
            val safeApps = shieldedApps.map { it.trim().take(40) }.filter { it.isNotEmpty() }.take(20)
            guestProfile.value = guestProfile.value.copy(
                dailyTargetMinutes = safeTarget,
                shieldedApps = safeApps,
                darkModeEnabled = darkModeEnabled,
                hapticsEnabled = hapticsEnabled
            )
            return Result.success(Unit)
        }
        var docPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val safeTarget = dailyTargetMinutes.coerceIn(30L, 960L)
            val safeApps = shieldedApps.map { it.trim().take(40) }.filter { it.isNotEmpty() }.take(20)
            val docRef = userDoc(uid)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "dailyTargetMinutes" to safeTarget,
                    "shieldedApps" to safeApps,
                    "darkModeEnabled" to darkModeEnabled,
                    "hapticsEnabled" to hapticsEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addGoal(
        userId: String,
        dateKey: String,
        title: String,
        targetMinutes: Int,
        category: String,
        tier: GoalTier
    ): Result<Unit> {
        if (userId == GUEST_USER_ID) {
            val trimmedTitle = title.trim().take(120)
            require(trimmedTitle.isNotEmpty()) { "Goal title cannot be empty." }
            val newGoal = DailyGoal(
                id = "guest_goal_${UUID.randomUUID().toString().take(8)}",
                userId = GUEST_USER_ID,
                dateKey = dateKey,
                title = trimmedTitle,
                targetMinutes = targetMinutes.coerceIn(5, 600).toLong(),
                category = category.trim().ifEmpty { "Deep Work" }.take(40),
                tier = tier.name,
                completed = false
            )
            guestGoals.value = guestGoals.value + newGoal
            return Result.success(Unit)
        }
        var docPath = "users/$userId/goals"
        return try {
            val uid = requireUserId(userId)
            val trimmedTitle = title.trim().take(120)
            require(trimmedTitle.isNotEmpty()) { "Goal title cannot be empty." }
            require(dateKey.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))) { "Invalid date format." }
            val safeMinutes = targetMinutes.coerceIn(5, 600).toLong()
            val safeCategory = category.trim().ifEmpty { "Deep Work" }.take(40)

            val docRef = userDoc(uid).collection("goals").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "dateKey" to dateKey,
                "title" to trimmedTitle,
                "targetMinutes" to safeMinutes,
                "category" to safeCategory,
                "tier" to tier.name,
                "completed" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleGoalCompleted(
        userId: String,
        goalId: String,
        completed: Boolean
    ): Result<Unit> {
        if (userId == GUEST_USER_ID) {
            guestGoals.value = guestGoals.value.map {
                if (it.id == goalId) it.copy(completed = completed) else it
            }
            return Result.success(Unit)
        }
        var docPath = "users/$userId/goals/$goalId"
        return try {
            val uid = requireUserId(userId)
            require(goalId.isNotBlank()) { "Goal ID must not be blank." }
            val docRef = userDoc(uid).collection("goals").document(goalId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "completed" to completed,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteGoal(userId: String, goalId: String): Result<Unit> {
        if (userId == GUEST_USER_ID) {
            guestGoals.value = guestGoals.value.filter { it.id != goalId }
            return Result.success(Unit)
        }
        var docPath = "users/$userId/goals/$goalId"
        return try {
            val uid = requireUserId(userId)
            require(goalId.isNotBlank()) { "Goal ID must not be blank." }
            val docRef = userDoc(uid).collection("goals").document(goalId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addSession(
        userId: String,
        dateKey: String,
        title: String,
        category: String,
        startMinuteOfDay: Int,
        durationMinutes: Int,
        tags: List<String>,
        subNotes: String,
        goalId: String?,
        shieldActive: Boolean
    ): Result<Unit> {
        if (userId == GUEST_USER_ID) {
            val safeTitle = title.trim().take(120)
            require(safeTitle.isNotEmpty()) { "Session title cannot be empty." }
            val newSession = WorkSession(
                id = "guest_session_${UUID.randomUUID().toString().take(8)}",
                userId = GUEST_USER_ID,
                dateKey = dateKey,
                title = safeTitle,
                category = category.trim().ifEmpty { "Deep Work" }.take(40),
                startMinuteOfDay = startMinuteOfDay.coerceIn(0, 1439).toLong(),
                durationMinutes = durationMinutes.coerceIn(1, 720).toLong(),
                tags = tags.map { it.trim().take(30) }.filter { it.isNotEmpty() }.take(8),
                subNotes = subNotes.trim().take(2000),
                goalId = goalId,
                shieldActive = shieldActive
            )
            guestSessions.value = guestSessions.value + newSession
            return Result.success(Unit)
        }
        var docPath = "users/$userId/sessions"
        return try {
            val uid = requireUserId(userId)
            val safeTitle = title.trim().take(120)
            require(safeTitle.isNotEmpty()) { "Session title cannot be empty." }
            require(dateKey.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))) { "Invalid date format." }
            val safeCategory = category.trim().ifEmpty { "Deep Work" }.take(40)
            val safeStart = startMinuteOfDay.coerceIn(0, 1439).toLong()
            val safeDuration = durationMinutes.coerceIn(1, 720).toLong()
            val safeTags = tags.map { it.trim().take(30) }.filter { it.isNotEmpty() }.take(8)
            val safeNotes = subNotes.trim().take(2000)
            val safeGoalId = goalId?.trim()?.takeIf {
                it.isNotEmpty() && it.matches(Regex("^[a-zA-Z0-9_\\-]+$")) && it.length <= 128
            }

            val docRef = userDoc(uid).collection("sessions").document()
            docPath = docRef.path

            val payload = mapOf(
                "userId" to uid,
                "dateKey" to dateKey,
                "title" to safeTitle,
                "category" to safeCategory,
                "startMinuteOfDay" to safeStart,
                "durationMinutes" to safeDuration,
                "tags" to safeTags,
                "subNotes" to safeNotes,
                "goalId" to safeGoalId,
                "shieldActive" to shieldActive,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateSessionNotes(
        userId: String,
        sessionId: String,
        subNotes: String
    ): Result<Unit> {
        if (userId == GUEST_USER_ID) {
            guestSessions.value = guestSessions.value.map {
                if (it.id == sessionId) it.copy(subNotes = subNotes.trim().take(2000)) else it
            }
            return Result.success(Unit)
        }
        var docPath = "users/$userId/sessions/$sessionId"
        return try {
            val uid = requireUserId(userId)
            require(sessionId.isNotBlank()) { "Session ID must not be blank." }
            val safeNotes = subNotes.trim().take(2000)
            val docRef = userDoc(uid).collection("sessions").document(sessionId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "subNotes" to safeNotes,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteSession(userId: String, sessionId: String): Result<Unit> {
        if (userId == GUEST_USER_ID) {
            guestSessions.value = guestSessions.value.filter { it.id != sessionId }
            return Result.success(Unit)
        }
        var docPath = "users/$userId/sessions/$sessionId"
        return try {
            val uid = requireUserId(userId)
            require(sessionId.isNotBlank()) { "Session ID must not be blank." }
            val docRef = userDoc(uid).collection("sessions").document(sessionId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun applyStreakFreeze(userId: String, dateKey: String): Result<Unit> {
        if (userId == GUEST_USER_ID) {
            if (guestProfile.value.streakFreezes <= 0L) {
                return Result.failure(IllegalStateException("No streak freezes remaining."))
            }
            guestProfile.value = guestProfile.value.copy(streakFreezes = guestProfile.value.streakFreezes - 1L)
            guestDayStatuses.value = guestDayStatuses.value + DayStatus(
                id = dateKey,
                userId = GUEST_USER_ID,
                dateKey = dateKey,
                freezeUsed = true
            )
            return Result.success(Unit)
        }
        var docPath = "users/$userId/dayStatuses/$dateKey"
        return try {
            val uid = requireUserId(userId)
            require(dateKey.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))) { "Invalid date format." }
            val profileRef = userDoc(uid)
            val statusRef = profileRef.collection("dayStatuses").document(dateKey)
            docPath = statusRef.path

            val profileSnap = profileRef.get().await()
            val freezes = profileSnap.getLong("streakFreezes") ?: 0L
            require(freezes > 0L) { "No streak freezes remaining." }

            val batch = db.batch()
            batch.update(
                profileRef,
                mapOf(
                    "streakFreezes" to (freezes - 1L).coerceAtLeast(0L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                statusRef,
                mapOf(
                    "userId" to uid,
                    "dateKey" to dateKey,
                    "freezeUsed" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }
}
