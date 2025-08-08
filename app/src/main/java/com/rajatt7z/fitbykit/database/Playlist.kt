package com.rajatt7z.fitbykit.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val day: String,
    val notificationsEnabled: Boolean,
    val analyticsEnabled: Boolean,
    val categories: String
)
