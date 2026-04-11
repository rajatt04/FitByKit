package com.rajatt7z.fitbykit.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajatt7z.workout_api.Routine
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
class RoutineViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _routines = MutableLiveData<List<Routine>>()
    val routines: LiveData<List<Routine>> = _routines

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _currentSessionId = MutableLiveData<Int?>()
    val currentSessionId: LiveData<Int?> = _currentSessionId

    fun fetchRoutines() {
        _loading.value = true
        viewModelScope.launch {
            try {
                _routines.value = repository.fetchRoutines()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun startWorkoutSession(routineId: Int?) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val session = WorkoutSession(0, today, routineId, "Started from FitByKit Epic 3")
                val createdSession = repository.createWorkoutSession(session)
                _currentSessionId.value = createdSession?.id
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun logExerciseSet(reps: Int, weight: String, exerciseId: Int) {
        val sessionId = _currentSessionId.value ?: return
        viewModelScope.launch {
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val log = WorkoutLog(0, reps, weight, exerciseId, today)
                // Note: WGER logs need to be attached to the session or exercise step. 
                // We're issuing a post directly.
                repository.createWorkoutLog(log)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
