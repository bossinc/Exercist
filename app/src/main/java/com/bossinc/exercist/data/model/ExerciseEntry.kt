package com.bossinc.exercist.data.model

data class ExerciseEntry(
    val exerciseId: String = "",
    val exerciseName: String = "",
    val sets: List<ExerciseSet> = emptyList(),
    val notes: String = ""
)
