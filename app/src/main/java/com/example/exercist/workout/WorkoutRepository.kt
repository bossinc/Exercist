package com.example.exercist.workout

import com.example.exercist.data.model.WorkoutSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId get() = auth.currentUser?.uid ?: ""

    private fun sessionsCollection() = firestore.collection("users").document(userId).collection("workouts")

    suspend fun saveWorkoutSession(session: WorkoutSession): Result<Unit> = runCatching {
        sessionsCollection().add(session.copy(userId = userId)).await()
        Unit
    }
}
