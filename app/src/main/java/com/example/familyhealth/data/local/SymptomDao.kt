package com.example.familyhealth.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {

    @Query("SELECT * FROM symptoms ORDER BY dateTime DESC")
    fun observeAll(): Flow<List<SymptomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SymptomEntity): Long

    @Update
    suspend fun update(entity: SymptomEntity)

    @Delete
    suspend fun delete(entity: SymptomEntity)

    @Query("DELETE FROM symptoms")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<SymptomEntity>)

    suspend fun replaceAll(list: List<SymptomEntity>) {
        clear()
        insertAll(list)
    }
}
