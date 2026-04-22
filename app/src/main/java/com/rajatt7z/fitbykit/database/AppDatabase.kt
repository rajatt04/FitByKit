package com.rajatt7z.fitbykit.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Main Room database for FitByKit.
 *
 * The singleton instance is managed entirely by Hilt (via AppModule.provideAppDatabase).
 * Do NOT use the old companion object pattern — accessing the DB directly bypasses DI.
 *
 * Current schema version: 2
 * Entities: LikedExercise, WaterIntake
 *
 * When you add/modify entities in the future, increment the version and add a
 * Migration object in AppModule, e.g.:
 *   .addMigrations(MIGRATION_2_3)
 * Never rely on fallbackToDestructiveMigration in production — it silently
 * deletes all user data on schema change.
 */
@Database(
    entities = [LikedExercise::class, WaterIntake::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun likedExerciseDao(): LikedExerciseDao
    abstract fun waterIntakeDao(): WaterIntakeDao
}