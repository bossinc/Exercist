package com.example.exercist.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class WorkoutTemplate(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val exercises: List<TemplateExercise> = emptyList(),
    @ServerTimestamp val createdAt: Date? = null
)

data class TemplateExercise(
    val exerciseId: String = "",
    val exerciseName: String = "",
    val targetSets: Int = 3,
    val targetReps: Int = 10,
    val order: Int = 0
)
