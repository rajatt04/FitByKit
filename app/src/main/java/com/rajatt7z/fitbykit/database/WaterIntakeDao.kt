package com.rajatt7z.fitbykit.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterIntakeDao {
    @Insert
    suspend fun insert(waterIntake: WaterIntake)

    @Query("SELECT SUM(amountMl) FROM water_intake_table WHERE date = :date")
    fun getTotalWaterForDate(date: String): Flow<Int?>

    @Query("SELECT * FROM water_intake_table ORDER BY date DESC")
    fun getAllHistory(): Flow<List<WaterIntake>>
}
