package com.example.familyhealth.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import java.util.TimeZone

fun openCalendarInsert(
    context: Context,
    title: String,
    location: String,
    startMillis: Long,
    endMillis: Long,
    notes: String
) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        // URI estándar de calendario
        data = CalendarContract.Events.CONTENT_URI
        // En algunos dispositivos ayuda indicar también el tipo
        type = "vnd.android.cursor.item/event"

        putExtra(CalendarContract.Events.TITLE, title)
        putExtra(CalendarContract.Events.EVENT_LOCATION, location)
        putExtra(CalendarContract.Events.DESCRIPTION, notes)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
        putExtra(
            CalendarContract.Events.EVENT_TIMEZONE,
            TimeZone.getDefault().id
        )

        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No se ha encontrado ninguna app de calendario para añadir la cita.",
            Toast.LENGTH_LONG
        ).show()
    }
}
