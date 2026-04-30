package com.bossinc.exercist.history

import com.bossinc.exercist.data.model.WorkoutSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface HistoryRepository {
    fun getSessions(): Flow<List<WorkoutSession>>
    suspend fun loadSession(id: String): WorkoutSession?
    suspend fun updateSession(session: WorkoutSession): Result<Unit>
    suspend fun deleteSession(sessionId: String): Result<Unit>
}

@Singleton
class FirebaseHistoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : HistoryRepository {

    private val userId get() = auth.currentUser?.uid ?: ""

    private fun workoutsCollection() =
        firestore.collection("users").document(userId).collection("workouts")

    override fun getSessions(): Flow<List<WorkoutSession>> = callbackFlow {
        if (userId.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val listener = workoutsCollection().addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val sessions = snapshot?.documents
                ?.mapNotNull { it.toObject(WorkoutSession::class.java) }
                ?.sortedByDescending { it.startedAt }
                ?: emptyList()
            trySend(sessions)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun loadSession(id: String): WorkoutSession? =
        workoutsCollection().document(id).get().await().toObject(WorkoutSession::class.java)

    override suspend fun updateSession(session: WorkoutSession): Result<Unit> = runCatching {
        workoutsCollection().document(session.id).set(session).await()
    }

    override suspend fun deleteSession(sessionId: String): Result<Unit> = runCatching {
        workoutsCollection().document(sessionId).delete().await()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class HistoryModule {
    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: FirebaseHistoryRepository): HistoryRepository
}
