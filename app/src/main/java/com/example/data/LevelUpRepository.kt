package com.example.data

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.DailyGoal
import com.example.model.DayStatus
import com.example.model.GoalTier
import com.example.model.TaskLog
import com.example.model.UserProfile
import com.example.model.WorkSession
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class LevelUpRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUserId: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        if (expectedUserId != null) {
            check(uid == expectedUserId) { "Signed-in user changed; refusing cross-user operation." }
        }
        return uid
    }

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
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
        val uid = requireUserId(userId)
        val colRef = userDoc(uid).collection("goals")
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
        val uid = requireUserId(userId)
        val colRef = userDoc(uid).collection("sessions")
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
        val uid = requireUserId(userId)
        val colRef = userDoc(uid).collection("dayStatuses")
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

    fun observeTaskLogs(userId: String): Flow<List<TaskLog>> = flow {
        val uid = requireUserId(userId)
        val colRef = userDoc(uid).collection("taskLogs")
        emitAll(
            colRef.orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TaskLog::class.java,
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

    suspend fun addTaskLog(
        userId: String,
        workDone: String,
        category: String,
        durationMinutes: Int?
    ): Result<Unit> {
        var docPath = "users/$userId/taskLogs"
        return try {
            val uid = requireUserId(userId)
            val safeWorkDone = workDone.trim().take(500)
            require(safeWorkDone.isNotEmpty()) { "Work description cannot be empty." }
            val safeCategory = category.trim().ifEmpty { "Deep Work" }.take(40)
            val safeDuration = durationMinutes?.coerceIn(1, 720)?.toLong()

            val docRef = userDoc(uid).collection("taskLogs").document()
            docPath = docRef.path

            val payload = mapOf(
                "userId" to uid,
                "workDone" to safeWorkDone,
                "category" to safeCategory,
                "durationMinutes" to safeDuration,
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

    suspend fun deleteTaskLog(userId: String, logId: String): Result<Unit> {
        var docPath = "users/$userId/taskLogs/$logId"
        return try {
            val uid = requireUserId(userId)
            require(logId.isNotBlank()) { "Task log ID must not be blank." }
            val docRef = userDoc(uid).collection("taskLogs").document(logId)
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
}
