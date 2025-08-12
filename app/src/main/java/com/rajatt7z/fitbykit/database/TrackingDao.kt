package com.rajatt7z.fitbykit.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackingDao {
    @Query("SELECT * FROM tracking_records ORDER BY startTime DESC")
    fun getAllRecords(): Flow<List<TrackingRecord>>

    @Insert
    suspend fun insertRecord(record: TrackingRecord): Long

    @Delete
    suspend fun deleteRecord(record: TrackingRecord)

    @Query("DELETE FROM tracking_records")
    suspend fun deleteAllRecords()
}