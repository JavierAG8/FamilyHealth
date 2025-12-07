package com.example.familyhealth.data

import android.app.Application
import com.example.familyhealth.data.local.AppDatabase
import com.example.familyhealth.data.local.AppointmentEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class AppointmentRepository(app: Application) {

    private val localDb = AppDatabase.getInstance(app)
    private val appointmentDao = localDb.appointmentDao()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun observe(): Flow<List<AppointmentEntity>> =
        appointmentDao.observeAll()

    suspend fun add(appointment: AppointmentEntity): Long =
        appointmentDao.insert(appointment)

    suspend fun update(appointment: AppointmentEntity) =
        appointmentDao.update(appointment)

    suspend fun delete(appointment: AppointmentEntity) =
        appointmentDao.delete(appointment)

    suspend fun pushToCloud(appointment: AppointmentEntity): String? {
        val user = auth.currentUser ?: return null

        val data = hashMapOf(
            "doctor" to appointment.doctor,
            "location" to appointment.location,
            "start" to appointment.start,
            "end" to appointment.end,
            "notes" to appointment.notes
        )

        return try {
            val docRef = db.collection("users")
                .document(user.uid)
                .collection("appointments")
                .add(data)
                .await()

            docRef.id
        } catch (_: Exception) {
            null
        }
    }

    suspend fun syncFromCloudReplaceLocal() {
        val user = auth.currentUser ?: return

        val snap = db.collection("users")
            .document(user.uid)
            .collection("appointments")
            .get()
            .await()

        val list = snap.documents.map { doc ->
            AppointmentEntity(
                id = 0L,                      // Room generará id al insertar
                remoteId = doc.id,
                doctor = doc.getString("doctor") ?: "",
                location = doc.getString("location") ?: "",
                start = doc.getLong("start") ?: 0L,
                end = doc.getLong("end") ?: 0L,
                notes = doc.getString("notes") ?: ""
            )
        }

        appointmentDao.replaceAll(list)
    }
}
