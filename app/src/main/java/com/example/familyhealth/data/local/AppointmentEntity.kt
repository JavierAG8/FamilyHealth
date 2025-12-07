package com.example.familyhealth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String? = null,
    val calendarEventId: String? = null,
    val doctor: String,
    val location: String,
    val start: Long,
    val end: Long,
    val notes: String = ""
)
