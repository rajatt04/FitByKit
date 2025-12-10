package com.rajatt7z.fitbykit.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_intake_table")
data class WaterIntake(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String, // Format: yyyy-MM-dd
    val amountMl: Int // Amount in milliliters
)
