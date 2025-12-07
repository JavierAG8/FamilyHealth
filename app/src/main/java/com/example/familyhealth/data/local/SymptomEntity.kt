package com.example.familyhealth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptoms")
data class SymptomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,


    val remoteId: String? = null,

    val dateTime: Long,
    val intensity: Int,
    val description: String,
    val tags: String
)
