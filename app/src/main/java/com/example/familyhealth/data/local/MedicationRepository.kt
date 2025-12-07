package com.example.familyhealth.data

import android.content.Context
import com.example.familyhealth.data.local.AppDatabase
import com.example.familyhealth.data.local.MedicationEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class MedicationRepository(
    context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val dao = AppDatabase.getInstance(context).medicationDao()


    fun observeLocal(): Flow<List<MedicationEntity>> = dao.observeAll()
    suspend fun insertLocal(m: MedicationEntity): Long = dao.insert(m)
    suspend fun updateLocal(m: MedicationEntity) = dao.update(m)
    suspend fun deleteLocal(m: MedicationEntity) = dao.delete(m)
    suspend fun clearLocal() = dao.clearAll()


    suspend fun getById(id: Long): MedicationEntity? = dao.getById(id)



    private fun userMedicationsCollectionOrNull() =
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .collection("medications")
        }

    suspend fun pushToCloud(m: MedicationEntity): String? {
        val col = userMedicationsCollectionOrNull() ?: return null

        val data = hashMapOf(
            "name" to m.name,
            "dosage" to m.dosage,
            "frequency" to m.frequency,
            "startDate" to m.startDate,
            "endDate" to m.endDate,
            "taken" to m.taken
        )
        val docRef = col.add(data).await()
        return docRef.id
    }

    suspend fun syncFromCloudReplaceLocal() {
        val col = userMedicationsCollectionOrNull() ?: return

        val snapshot = col.get().await()

        dao.clearAll()

        for (doc in snapshot.documents) {
            val m = MedicationEntity(
                remoteId = doc.id,
                name = doc.getString("name") ?: "",
                dosage = doc.getString("dosage") ?: "",
                frequency = doc.getString("frequency") ?: "",
                startDate = doc.getString("startDate") ?: "",
                endDate = doc.getString("endDate") ?: "",
                taken = doc.getBoolean("taken") ?: false,
                userId = auth.currentUser?.uid
            )
            dao.insert(m)
        }
    }
}
