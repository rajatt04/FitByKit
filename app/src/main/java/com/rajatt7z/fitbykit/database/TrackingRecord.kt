package com.rajatt7z.fitbykit.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracking_records")
data class TrackingRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startLatitude: Double,
    val startLongitude: Double,
    val endLatitude: Double,
    val endLongitude: Double,
    val distance: Double, // in meters
    val startTime: Long,
    val endTime: Long,
    val duration: Long // in milliseconds
)