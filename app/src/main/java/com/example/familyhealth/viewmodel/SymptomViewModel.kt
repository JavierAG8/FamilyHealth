package com.example.familyhealth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyhealth.data.SymptomRepository
import com.example.familyhealth.data.local.SymptomEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SymptomViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SymptomRepository(app)
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val symptoms = repo.observe().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    fun addSymptom(
        dateTime: Long,
        intensity: Int,
        description: String,
        tags: String
    ) {
        viewModelScope.launch {
            val base = SymptomEntity(
                dateTime = dateTime,
                intensity = intensity,
                description = description,
                tags = tags
            )

            val localId = repo.add(base)

            runCatching {
                val remoteId = repo.pushToCloud(base.copy(id = localId))
                if (remoteId != null) {
                    repo.update(
                        base.copy(
                            id = localId,
                            remoteId = remoteId
                        )
                    )
                }
            }
        }
    }

    fun updateSymptom(s: SymptomEntity) {
        viewModelScope.launch {
            repo.update(s)

            val user = auth.currentUser
            val remoteId = s.remoteId
            if (user != null && !remoteId.isNullOrBlank()) {
                val data = mapOf(
                    "dateTime" to s.dateTime,
                    "intensity" to s.intensity,
                    "description" to s.description,
                    "tags" to s.tags
                )

                db.collection("users")
                    .document(user.uid)
                    .collection("symptoms")
                    .document(remoteId)
                    .set(data)
            }
        }
    }

    fun deleteSymptom(s: SymptomEntity) {
        viewModelScope.launch {
            repo.delete(s)

            val user = auth.currentUser
            val remoteId = s.remoteId
            if (user != null && !remoteId.isNullOrBlank()) {
                db.collection("users")
                    .document(user.uid)
                    .collection("symptoms")
                    .document(remoteId)
                    .delete()
            }
        }
    }

    fun syncFromCloud() {
        viewModelScope.launch {
            runCatching { repo.syncFromCloudReplaceLocal() }
        }
    }
}
