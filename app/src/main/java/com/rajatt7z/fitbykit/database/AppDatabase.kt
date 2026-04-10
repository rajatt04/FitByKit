package com.rajatt7z.fitbykit.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LikedExercise::class, WaterIntake::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase(){
    abstract fun likedExerciseDao(): LikedExerciseDao
    abstract fun waterIntakeDao(): WaterIntakeDao

    companion object{
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitbykit_db"
                ).build().also { instance = it }
            }
        }
    }
}