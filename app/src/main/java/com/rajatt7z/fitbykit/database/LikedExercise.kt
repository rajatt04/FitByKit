package com.rajatt7z.fitbykit.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liked_exercises")
data class LikedExercise (
    @PrimaryKey val name: String,
    val videoUrl: String
)