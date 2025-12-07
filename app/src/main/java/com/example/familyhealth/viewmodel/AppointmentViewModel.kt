package com.example.familyhealth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyhealth.data.AppointmentRepository
import com.example.familyhealth.data.local.AppointmentEntity
import com.example.familyhealth.utils.ReminderScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppointmentViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppointmentRepository(app)
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val appointments = repo.observe().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    fun add(
        doctor: String,
        location: String,
        start: Long,
        end: Long,
        notes: String
    ) {
        viewModelScope.launch {
            val base = AppointmentEntity(
                doctor = doctor,
                location = location,
                start = start,
                end = end,
                notes = notes
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

            val ctx = getApplication<Application>()
            val tenMinutesBefore = start - 10 * 60 * 1000

            if (tenMinutesBefore > System.currentTimeMillis()) {
                ReminderScheduler.schedule(
                    context = ctx,
                    triggerAtMillis = tenMinutesBefore,
                    title = "Cita médica",
                    text = "Con $doctor en $location"
                )
            }
        }
    }

    fun update(a: AppointmentEntity) {
        viewModelScope.launch {

            repo.update(a)

            val user = auth.currentUser
            val remoteId = a.remoteId
            if (user != null && !remoteId.isNullOrBlank()) {
                val data = mapOf(
                    "doctor" to a.doctor,
                    "location" to a.location,
                    "start" to a.start,
                    "end" to a.end,
                    "notes" to a.notes
                )

                db.collection("users")
                    .document(user.uid)
                    .collection("appointments")
                    .document(remoteId)
                    .set(data)
            } else {

                runCatching {
                    val newRemoteId = repo.pushToCloud(a)
                    if (newRemoteId != null) {
                        repo.update(a.copy(remoteId = newRemoteId))
                    }
                }
            }
        }
    }


    fun delete(a: AppointmentEntity) {
        viewModelScope.launch {

            repo.delete(a)

            val user = auth.currentUser
            val remoteId = a.remoteId
            if (user != null && !remoteId.isNullOrBlank()) {
                db.collection("users")
                    .document(user.uid)
                    .collection("appointments")
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
