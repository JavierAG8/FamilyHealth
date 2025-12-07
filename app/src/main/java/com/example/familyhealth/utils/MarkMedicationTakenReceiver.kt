package com.example.familyhealth.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.familyhealth.data.MedicationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MarkMedicationTakenReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getLongExtra("medId", -1L)
        val notificationId = intent.getIntExtra("notificationId", -1)

        if (medId == -1L) return

        val repo = MedicationRepository(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            val med = repo.getById(medId)   // asegúrate de tener este método en el repo
            if (med != null) {
                repo.updateLocal(med.copy(taken = true))
            }
        }

        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }
    }
}
