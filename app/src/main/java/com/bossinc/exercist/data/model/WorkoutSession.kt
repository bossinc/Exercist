package com.bossinc.exercist.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class WorkoutSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val exercises: List<ExerciseEntry> = emptyList(),
    val durationMinutes: Int = 0,
    val notes: String = "",
    val templateId: String? = null,
    @ServerTimestamp val date: Date? = null,
    val startedAt: Date? = null,
    val finishedAt: Date? = null
)
