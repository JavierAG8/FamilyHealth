package com.example.familyhealth.data

import android.app.Application
import com.example.familyhealth.data.local.AppDatabase
import com.example.familyhealth.data.local.SymptomEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class SymptomRepository(app: Application) {

    private val localDb = AppDatabase.getInstance(app)
    private val symptomDao = localDb.symptomDao()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun observe(): Flow<List<SymptomEntity>> =
        symptomDao.observeAll()

    suspend fun add(symptom: SymptomEntity): Long =
        symptomDao.insert(symptom)

    suspend fun update(symptom: SymptomEntity) =
        symptomDao.update(symptom)

    suspend fun delete(symptom: SymptomEntity) =
        symptomDao.delete(symptom)

    suspend fun pushToCloud(symptom: SymptomEntity): String? {
        val user = auth.currentUser ?: return null

        val data = hashMapOf(
            "dateTime" to symptom.dateTime,
            "intensity" to symptom.intensity,
            "description" to symptom.description,
            "tags" to symptom.tags
        )

        return try {
            val docRef = db.collection("users")
                .document(user.uid)
                .collection("symptoms")
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
            .collection("symptoms")
            .get()
            .await()

        val list = snap.documents.map { doc ->
            SymptomEntity(
                id = 0L,                  // Room genera id al insertar
                remoteId = doc.id,
                dateTime = doc.getLong("dateTime") ?: 0L,
                intensity = (doc.getLong("intensity") ?: 0L).toInt(),
                description = doc.getString("description") ?: "",
                tags = doc.getString("tags") ?: ""
            )
        }

        symptomDao.replaceAll(list)
    }
}
