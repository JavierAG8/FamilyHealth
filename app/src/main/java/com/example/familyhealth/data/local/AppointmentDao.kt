package com.example.familyhealth.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Query("SELECT * FROM appointments ORDER BY start DESC")
    fun observeAll(): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AppointmentEntity): Long

    @Update
    suspend fun update(entity: AppointmentEntity)

    @Delete
    suspend fun delete(entity: AppointmentEntity)

    @Query("DELETE FROM appointments")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<AppointmentEntity>)

    suspend fun replaceAll(list: List<AppointmentEntity>) {
        clear()
        insertAll(list)
    }
}
