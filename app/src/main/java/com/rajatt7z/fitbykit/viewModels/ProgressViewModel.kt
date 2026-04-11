package com.rajatt7z.fitbykit.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajatt7z.workout_api.Gallery
import com.rajatt7z.workout_api.Measurement
import com.rajatt7z.workout_api.MeasurementCategory
import com.rajatt7z.workout_api.Trophy
import com.rajatt7z.workout_api.UserStatistics
import com.rajatt7z.workout_api.UserTrophy
import com.rajatt7z.workout_api.WeightEntry
import com.rajatt7z.workout_api.WorkoutLog
import com.rajatt7z.workout_api.WorkoutRepository
import com.rajatt7z.workout_api.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    // Weight entries
    private val _weightEntries = MutableLiveData<List<WeightEntry>>()
    val weightEntries: LiveData<List<WeightEntry>> = _weightEntries

    // Measurements
    private val _measurementCategories = MutableLiveData<List<MeasurementCategory>>()
    val measurementCategories: LiveData<List<MeasurementCategory>> = _measurementCategories

    private val _measurements = MutableLiveData<List<Measurement>>()
    val measurements: LiveData<List<Measurement>> = _measurements

    // Trophies
    private val _trophies = MutableLiveData<List<Trophy>>()
    val trophies: LiveData<List<Trophy>> = _trophies

    private val _userTrophies = MutableLiveData<List<UserTrophy>>()
    val userTrophies: LiveData<List<UserTrophy>> = _userTrophies

    // Stats
    private val _userStats = MutableLiveData<List<UserStatistics>>()
    val userStats: LiveData<List<UserStatistics>> = _userStats

    // Gallery
    private val _gallery = MutableLiveData<List<Gallery>>()
    val gallery: LiveData<List<Gallery>> = _gallery

    // Workout history
    private val _sessions = MutableLiveData<List<WorkoutSession>>()
    val sessions: LiveData<List<WorkoutSession>> = _sessions

    private val _workoutLogs = MutableLiveData<List<WorkoutLog>>()
    val workoutLogs: LiveData<List<WorkoutLog>> = _workoutLogs

    // Loading
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    // Success message for log operations
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    fun fetchBodyProgress() {
        _loading.value = true
        viewModelScope.launch {
            try {
                _weightEntries.value = repository.fetchWeightEntries()
                _measurementCategories.value = repository.fetchMeasurementCategories()
                _measurements.value = repository.fetchMeasurements()
                _gallery.value = repository.fetchGallery()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchAchievements() {
        _loading.value = true
        viewModelScope.launch {
            try {
                _trophies.value = repository.fetchTrophies()
                _userTrophies.value = repository.fetchUserTrophies()
                _userStats.value = repository.fetchUserStatistics()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchWorkoutHistory() {
        _loading.value = true
        viewModelScope.launch {
            try {
                _sessions.value = repository.fetchWorkoutSessions()
                _workoutLogs.value = repository.fetchWorkoutLogs()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun logWeight(weightKg: String) {
        viewModelScope.launch {
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val entry = WeightEntry(0, weightKg, today)
                val result = repository.createWeightEntry(entry)
                if (result != null) {
                    _successMessage.value = "Weight logged: ${result.weight} kg"
                    fetchBodyProgress() // Refresh
                } else {
                    _successMessage.value = "Failed to log weight"
                }
            } catch (e: Exception) {
                _successMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun logMeasurement(categoryId: Int, value: String) {
        viewModelScope.launch {
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val m = Measurement(0, categoryId, value, today)
                val result = repository.createMeasurement(m)
                if (result != null) {
                    _successMessage.value = "Measurement saved!"
                    fetchBodyProgress()
                } else {
                    _successMessage.value = "Failed to save measurement"
                }
            } catch (e: Exception) {
                _successMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _successMessage.value = null
    }
}
