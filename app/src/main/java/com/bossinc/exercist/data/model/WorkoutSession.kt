package com.bossinc.exercist.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class WorkoutSession(
    @DocumentId val id: String = "",
    val exercises: List<ExerciseEntry> = emptyList(),
    val startedAt: Date? = null,
    val finishedAt: Date? = null
)
