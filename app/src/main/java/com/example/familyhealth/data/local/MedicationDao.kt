package com.example.familyhealth.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Query("SELECT * FROM medications ORDER BY id DESC")
    fun observeAll(): Flow<List<MedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(med: MedicationEntity): Long

    @Update
    suspend fun update(med: MedicationEntity)

    @Delete
    suspend fun delete(med: MedicationEntity)

    @Query("DELETE FROM medications")
    suspend fun clearAll()

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MedicationEntity?
}
