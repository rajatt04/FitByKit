package com.rajatt7z.fitbykit.di

import android.content.Context
import androidx.room.Room
import com.rajatt7z.fitbykit.database.AppDatabase
import com.rajatt7z.fitbykit.database.LikedExerciseDao
import com.rajatt7z.fitbykit.database.WaterIntakeDao
import com.rajatt7z.workout_api.MealApiClient
import com.rajatt7z.workout_api.MealRepository
import com.rajatt7z.workout_api.WorkoutApiClient
import com.rajatt7z.workout_api.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fitbykit_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideLikedExerciseDao(appDatabase: AppDatabase): LikedExerciseDao {
        return appDatabase.likedExerciseDao()
    }

    @Provides
    fun provideWaterIntakeDao(appDatabase: AppDatabase): WaterIntakeDao {
        return appDatabase.waterIntakeDao()
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(): WorkoutRepository {
        return WorkoutRepository(WorkoutApiClient.api)
    }

    @Provides
    @Singleton
    fun provideMealRepository(): MealRepository {
        return MealRepository(MealApiClient.api)
    }
}
