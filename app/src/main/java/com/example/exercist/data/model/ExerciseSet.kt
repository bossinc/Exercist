package com.example.exercist.data.model

data class ExerciseSet(
    val setNumber: Int = 0,
    val reps: Int = 0,
    val weight: Double = 0.0,
    val unit: String = "kg",
    val isCompleted: Boolean = false,
    val rpe: Float? = null
)
