package com.rajatt7z.fitbykit.repository

import com.rajatt7z.fitbykit.database.LikedExercise
import com.rajatt7z.fitbykit.database.LikedExerciseDao
import com.rajatt7z.fitbykit.database.WaterIntake
import com.rajatt7z.fitbykit.database.WaterIntakeDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataRepository @Inject constructor(
    private val likedExerciseDao: LikedExerciseDao,
    private val waterIntakeDao: WaterIntakeDao
) {
    // Liked Exercises
    suspend fun insertLikedExercise(exercise: LikedExercise) = likedExerciseDao.insert(exercise)
    suspend fun deleteLikedExercise(exercise: LikedExercise) = likedExerciseDao.delete(exercise)
    suspend fun getAllLikedExercises(): List<LikedExercise> = likedExerciseDao.getAll()

    // Water Intake
    suspend fun insertWaterIntake(waterIntake: WaterIntake) = waterIntakeDao.insert(waterIntake)
    fun getTotalWaterForDate(date: String): Flow<Int?> = waterIntakeDao.getTotalWaterForDate(date)
}
