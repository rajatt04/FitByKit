package com.rajatt7z.fitbykit.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LikedExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: LikedExercise)

    @Delete
    suspend fun delete(exercise: LikedExercise)

    @Query("SELECT * FROM liked_exercises")
    suspend fun getAll(): List<LikedExercise>

    @Query("SELECT * FROM liked_exercises WHERE name = :name")
    suspend fun getByName(name: String): LikedExercise?

}