package com.bossinc.exercist.data.model

data class ExerciseSet(
    val setNumber: Int = 0,
    val reps: Int = 0,
    val weight: Int = 0,
    val unit: String = "lbs",
    val isCompleted: Boolean = false,
    val rpe: Float? = null
)
