package com.example.exercist.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    @DocumentId val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val bodyWeightLog: List<BodyWeightEntry> = emptyList(),
    val goals: UserGoals = UserGoals(),
    @ServerTimestamp val createdAt: Date? = null
)

data class BodyWeightEntry(
    val weight: Double = 0.0,
    val unit: String = "kg",
    val date: Date = Date()
)

data class UserGoals(
    val weeklyWorkouts: Int = 3,
    val targetBodyWeight: Double? = null
)
