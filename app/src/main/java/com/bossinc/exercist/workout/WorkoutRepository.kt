package com.bossinc.exercist.workout

import com.bossinc.exercist.data.model.WorkoutSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface WorkoutRepository {
    suspend fun saveWorkoutSession(session: WorkoutSession): Result<Unit>
    suspend fun getRecentSessions(limit: Int = 20): List<WorkoutSession>
    suspend fun deleteWorkoutSession(sessionId: String): Result<Unit>
}

@Singleton
class FirebaseWorkoutRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : WorkoutRepository {
    private val userId get() = auth.currentUser?.uid ?: ""

    private fun sessionsCollection() = firestore.collection("users").document(userId).collection("workouts")

    override suspend fun saveWorkoutSession(session: WorkoutSession): Result<Unit> = runCatching {
        sessionsCollection().add(session.copy(userId = userId)).await()
        Unit
    }

    override suspend fun getRecentSessions(limit: Int): List<WorkoutSession> =
        sessionsCollection()
            .orderBy("startedAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get().await()
            .documents.mapNotNull { it.toObject(WorkoutSession::class.java) }

    override suspend fun deleteWorkoutSession(sessionId: String): Result<Unit> = runCatching {
        sessionsCollection().document(sessionId).delete().await()
        Unit
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkoutModule {
    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: FirebaseWorkoutRepository): WorkoutRepository
}
