package com.example.familyhealth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.familyhealth.data.MedicationRepository
import com.example.familyhealth.data.local.MedicationEntity
import com.example.familyhealth.utils.MedicationReminderWorker
import com.example.familyhealth.utils.ReminderScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MedicationViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MedicationRepository(app)
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val medications = repo.observeLocal().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    fun addMedication(
        name: String,
        dosage: String,
        frequency: String,
        startDate: String,
        endDate: String
    ) {
        viewModelScope.launch {

            val baseMed = MedicationEntity(
                name = name,
                dosage = dosage,
                frequency = frequency,
                startDate = startDate,
                endDate = endDate,
                taken = false
            )


            val localId = repo.insertLocal(baseMed)

            runCatching {
                val remoteId = repo.pushToCloud(
                    baseMed.copy(id = localId)
                )
                if (remoteId != null) {
                    repo.updateLocal(
                        baseMed.copy(
                            id = localId,
                            remoteId = remoteId
                        )
                    )
                }
            }

            val startMillis = parseToMillis(startDate)
            val interval = frequencyToIntervalMillis(frequency)

            val ctx = getApplication<Application>()

            if (startMillis != null && startMillis > System.currentTimeMillis()) {
                if (interval != null) {

                    scheduleMedicationWork(
                        medId = localId,
                        name = name,
                        dosage = dosage,
                        startMillis = startMillis,
                        intervalMillis = interval
                    )
                } else {

                    ReminderScheduler.schedule(
                        context = ctx,
                        triggerAtMillis = startMillis,
                        title = "Tomar medicación",
                        text = "$name - $dosage"
                    )
                }
            }
        }
    }

    fun updateMedication(med: MedicationEntity) {
        viewModelScope.launch {

            repo.updateLocal(med)

            val user = auth.currentUser
            val remoteId = med.remoteId
            if (user != null && !remoteId.isNullOrBlank()) {
                val data = mapOf(
                    "name" to med.name,
                    "dosage" to med.dosage,
                    "frequency" to med.frequency,
                    "startDate" to med.startDate,
                    "endDate" to med.endDate,
                    "taken" to med.taken
                )

                db.collection("users")
                    .document(user.uid)
                    .collection("medications")
                    .document(remoteId)
                    .set(data)
            }

            val startMillis = parseToMillis(med.startDate)
            val interval = frequencyToIntervalMillis(med.frequency)
            if (startMillis != null && interval != null) {
                scheduleMedicationWork(
                    medId = med.id,
                    name = med.name,
                    dosage = med.dosage,
                    startMillis = startMillis,
                    intervalMillis = interval
                )
            }
        }
    }

    fun toggleTaken(m: MedicationEntity) {
        viewModelScope.launch {
            val updated = m.copy(taken = !m.taken)
            repo.updateLocal(updated)

            val user = auth.currentUser
            val remoteId = updated.remoteId
            if (user != null && !remoteId.isNullOrBlank()) {
                db.collection("users")
                    .document(user.uid)
                    .collection("medications")
                    .document(remoteId)
                    .update("taken", updated.taken)
            }

            if (updated.taken) {
                val interval = frequencyToIntervalMillis(updated.frequency)
                if (interval == null) {
                    scheduleNextDailyReminder(updated)
                }
            }
        }
    }

    fun deleteMedication(m: MedicationEntity) {
        viewModelScope.launch {
            repo.deleteLocal(m)

            val user = auth.currentUser
            val remoteId = m.remoteId
            if (user != null && !remoteId.isNullOrBlank()) {
                db.collection("users")
                    .document(user.uid)
                    .collection("medications")
                    .document(remoteId)
                    .delete()
            }

            val id = m.id
            WorkManager.getInstance(getApplication<Application>())
                .cancelAllWorkByTag("medication_$id")
        }
    }

    fun syncFromCloud() {
        viewModelScope.launch {
            runCatching { repo.syncFromCloudReplaceLocal() }
        }
    }

    private fun parseToMillis(s: String): Long? {
        return try {
            val parts = s.split(" ", limit = 2)
            val d = parts.getOrNull(0)?.split("/") ?: return null
            val t = parts.getOrNull(1)?.split(":") ?: return null

            val day = d[0].toInt()
            val month = d[1].toInt()
            val year = d[2].toInt()
            val hour = t[0].toInt()
            val min = t[1].toInt()

            Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, min)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } catch (_: Exception) {
            null
        }
    }

    private fun frequencyToIntervalMillis(frequency: String): Long? {
        val f = frequency.trim().lowercase()

        return when {
            ("4" in f && "hora" in f) || f == "4h" -> 4L * 60L * 60L * 1000L
            ("6" in f && "hora" in f) || f == "6h" -> 6L * 60L * 60L * 1000L
            ("8" in f && "hora" in f) || f == "8h" -> 8L * 60L * 60L * 1000L
            ("12" in f && "hora" in f) || f == "12h" -> 12L * 60L * 60L * 1000L
            "24" in f && "hora" in f ||
                    f == "24h" ||
                    "diaria" in f ||
                    "una vez al día" in f ||
                    "una vez al dia" in f -> 24L * 60L * 60L * 1000L
            else -> null
        }
    }

    @Suppress("unused")
    fun hasPeriodicReminder(med: MedicationEntity): Boolean {
        return frequencyToIntervalMillis(med.frequency) != null
    }

    fun hasAnyReminderConfigured(med: MedicationEntity): Boolean {
        val startMillis = parseToMillis(med.startDate)
        val interval = frequencyToIntervalMillis(med.frequency)
        return interval != null || (startMillis != null && startMillis > System.currentTimeMillis())
    }

    private fun scheduleNextDailyReminder(med: MedicationEntity) {
        val now = System.currentTimeMillis()
        val next = now + 24L * 60L * 60L * 1000L

        ReminderScheduler.schedule(
            context = getApplication(),
            triggerAtMillis = next,
            title = "Tomar medicación",
            text = "Revisa tu medicación: ${med.name} (${med.dosage})"
        )
    }


    private fun scheduleMedicationWork(
        medId: Long,
        name: String,
        dosage: String,
        startMillis: Long,
        intervalMillis: Long
    ) {
        val ctx = getApplication<Application>()
        val now = System.currentTimeMillis()
        val initialDelay = (startMillis - now).coerceAtLeast(0L)

        val repeatIntervalMs = intervalMillis.coerceAtLeast(15L * 60L * 1000L)

        val data = workDataOf(
            "title" to "Tomar medicación",
            "text" to "$name - $dosage",
            "medId" to medId
        )

        val request = PeriodicWorkRequestBuilder<MedicationReminderWorker>(
            repeatIntervalMs,
            TimeUnit.MILLISECONDS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("medication_$medId")
            .build()

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            "medication_$medId",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
