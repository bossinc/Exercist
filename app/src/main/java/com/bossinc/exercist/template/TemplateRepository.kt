package com.bossinc.exercist.template

import com.bossinc.exercist.data.model.WorkoutTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId get() = auth.currentUser?.uid ?: ""
    private fun collection() = firestore.collection("users").document(userId).collection("templates")

    fun getTemplates(): Flow<List<WorkoutTemplate>> = callbackFlow {
        val listener = collection().addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val templates = snapshot?.documents?.mapNotNull { it.toObject(WorkoutTemplate::class.java) } ?: emptyList()
            trySend(templates)
        }
        awaitClose { listener.remove() }
    }

    suspend fun createTemplate(template: WorkoutTemplate): Result<Unit> = runCatching {
        collection().add(template.copy(userId = userId)).await()
        Unit
    }

    suspend fun getTemplate(id: String): WorkoutTemplate? =
        collection().document(id).get().await().toObject(WorkoutTemplate::class.java)
}
