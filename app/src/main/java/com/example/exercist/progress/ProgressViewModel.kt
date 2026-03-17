package com.example.exercist.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercist.data.model.PersonalRecord
import com.example.exercist.data.model.WorkoutSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val userId get() = auth.currentUser?.uid ?: ""

    val sessions: StateFlow<List<WorkoutSession>> = callbackFlow {
        if (userId.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val listener = firestore.collection("users").document(userId).collection("workouts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { it.toObject(WorkoutSession::class.java) }
                    ?.sortedBy { it.date } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prs: StateFlow<List<PersonalRecord>> = callbackFlow {
        if (userId.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val listener = firestore.collection("users").document(userId).collection("prs")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { it.toObject(PersonalRecord::class.java) }
                    ?.sortedByDescending { it.achievedAt } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
