package com.example.exercist.exercise

import com.example.exercist.data.model.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

class ExerciseRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("exercises")

    fun getExercises(): Flow<List<Exercise>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val exercises = snapshot?.documents?.mapNotNull { it.toObject(Exercise::class.java) } ?: emptyList()
            trySend(exercises)
        }
        awaitClose { listener.remove() }
    }

    suspend fun createExercise(exercise: Exercise): Result<Unit> = runCatching {
        collection.add(exercise).await()
        Unit
    }

    suspend fun seedSampleExercises() {
        val samples = listOf(
            Exercise(name = "Bench Press", muscleGroup = "Chest", equipment = "Barbell"),
            Exercise(name = "Squat", muscleGroup = "Legs", equipment = "Barbell"),
            Exercise(name = "Deadlift", muscleGroup = "Back", equipment = "Barbell"),
            Exercise(name = "Pull-up", muscleGroup = "Back", equipment = "Bodyweight"),
            Exercise(name = "Overhead Press", muscleGroup = "Shoulders", equipment = "Barbell"),
            Exercise(name = "Barbell Row", muscleGroup = "Back", equipment = "Barbell"),
            Exercise(name = "Dumbbell Curl", muscleGroup = "Arms", equipment = "Dumbbell"),
            Exercise(name = "Tricep Dip", muscleGroup = "Arms", equipment = "Bodyweight"),
            Exercise(name = "Plank", muscleGroup = "Core", equipment = "Bodyweight"),
            Exercise(name = "Running", muscleGroup = "Cardio", equipment = "None")
        )
        val existing = collection.get().await()
        if (existing.isEmpty) {
            samples.forEach { collection.add(it).await() }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ExerciseModule {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideExerciseRepository(firestore: FirebaseFirestore): ExerciseRepository =
        ExerciseRepository(firestore)
}
