package com.example.familyhealth.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.familyhealth.R

class MedicationReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: "Recordatorio de medicación"
        val text = inputData.getString("text") ?: ""
        val medId = inputData.getLong("medId", -1L)

        val channelId = "medication_reminders"

        // Crear canal para Android 8+
        val channel = NotificationChannel(
            channelId,
            "Recordatorios de medicación",
            NotificationManager.IMPORTANCE_HIGH
        )
        (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                return Result.success()
            }
        }

        val notificationId: Int = if (medId != -1L) {
            medId.toInt()   // asumiendo que tus IDs caben en Int
        } else {
            (10000..99999).random()
        }

        // Acción "Marcar como tomada"
        val markTakenIntent = Intent(
            applicationContext,
            MarkMedicationTakenReceiver::class.java
        ).apply {
            action = "ACTION_MARK_MED_TAKEN"
            putExtra("medId", medId)
            putExtra("notificationId", notificationId)
        }

        val markTakenPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationId,
            markTakenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Marcar como tomada",
                markTakenPendingIntent
            )

        val notif = notifBuilder.build()


        NotificationManagerCompat.from(applicationContext)
            .notify(notificationId, notif)

        return Result.success()
    }
}
