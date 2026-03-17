package com.bossinc.exercist.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PersonalRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val exerciseId: String = "",
    val exerciseName: String = "",
    val weight: Double = 0.0,
    val reps: Int = 0,
    val unit: String = "kg",
    val workoutSessionId: String = "",
    @ServerTimestamp val achievedAt: Date? = null
)
