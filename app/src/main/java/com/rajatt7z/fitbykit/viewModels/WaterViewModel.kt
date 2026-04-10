package com.rajatt7z.fitbykit.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajatt7z.fitbykit.database.WaterIntake
import com.rajatt7z.fitbykit.repository.LocalDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WaterViewModel @Inject constructor(
    private val repository: LocalDataRepository
) : ViewModel() {

    private val _todayWater = MutableStateFlow(0)
    val todayWater: StateFlow<Int> = _todayWater

    init {
        fetchTodayWater()
    }

    private fun fetchTodayWater() {
        val today = getTodayDate()
        viewModelScope.launch {
            repository.getTotalWaterForDate(today).collectLatest { total ->
                _todayWater.value = total ?: 0
            }
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val intake = WaterIntake(
                date = getTodayDate(),
                amountMl = amountMl
            )
            repository.insertWaterIntake(intake)
            // Flow will automatically update _todayWater due to collectLatest
        }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
