package com.example.familyhealth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String? = null,
    val name: String,
    val dosage: String,
    val frequency: String,
    val startDate: String,
    val endDate: String,
    val taken: Boolean = false,
    val userId: String? = null
)
