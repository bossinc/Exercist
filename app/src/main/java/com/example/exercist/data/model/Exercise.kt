package com.example.exercist.data.model

import com.google.firebase.firestore.DocumentId

data class Exercise(
    @DocumentId val id: String = "",
    val name: String = "",
    val muscleGroup: String = "",
    val equipment: String = "",
    val description: String = "",
    val isCustom: Boolean = false,
    val createdBy: String = ""
)

object MuscleGroups {
    val all = listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core", "Full Body", "Cardio")
}
