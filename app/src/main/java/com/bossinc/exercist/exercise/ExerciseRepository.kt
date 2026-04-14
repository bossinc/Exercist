package com.bossinc.exercist.exercise

import com.bossinc.exercist.data.model.Exercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface ExerciseRepository {
    fun getExercises(): Flow<List<Exercise>>
    suspend fun createExercise(exercise: Exercise): Result<Unit>
    suspend fun updateExercise(exercise: Exercise): Result<Unit>
    suspend fun deleteExercise(exerciseId: String): Result<Unit>
}

@Singleton
class FirebaseExerciseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ExerciseRepository {
    private val collection = firestore.collection("exercises")

    override fun getExercises(): Flow<List<Exercise>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val exercises = snapshot?.documents?.mapNotNull { it.toObject(Exercise::class.java) } ?: emptyList()
            trySend(exercises)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun createExercise(exercise: Exercise): Result<Unit> = runCatching {
        collection.add(exercise.copy(createdBy = auth.currentUser?.uid ?: "")).await()
        Unit
    }

    override suspend fun updateExercise(exercise: Exercise): Result<Unit> = runCatching {
        collection.document(exercise.id).set(exercise).await()
        Unit
    }

    override suspend fun deleteExercise(exerciseId: String): Result<Unit> = runCatching {
        collection.document(exerciseId).delete().await()
        Unit
    }
}
