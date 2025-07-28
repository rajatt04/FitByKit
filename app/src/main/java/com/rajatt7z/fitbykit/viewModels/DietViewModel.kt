package com.rajatt7z.fitbykit.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajatt7z.workout_api.Meal
import com.rajatt7z.workout_api.MealApiClient
import com.rajatt7z.workout_api.MealRepository
import kotlinx.coroutines.launch

class DietViewModel : ViewModel() {
    private val repository = MealRepository(MealApiClient.api)

    private val _meals = MutableLiveData<List<Meal>>()
    val meals: LiveData<List<Meal>> = _meals

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private var lastQuery: String = ""

    fun searchMeals(query: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                lastQuery = query
                val result = repository.searchMeals(query)
                _meals.value = result
            } catch (e: Exception) {
                Log.e("DietViewModel", "Search error: ", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadTopMealsIfNoQuery() {
        if (lastQuery.isBlank()) {
            loadTopMeals()
        }
    }

    fun getLastQuery(): String = lastQuery


    fun loadTopMeals() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val result = repository.searchMeals("")
                _meals.value = result.take(20)
                _loading.value = false
            } catch (e: Exception) {
                Log.e("DietViewModel", "Error loading top meals", e)
            }
        }
    }

    fun setLoading(value: Boolean) {
        _loading.value = value
    }

    fun shouldLoadTopMeals(): Boolean = lastQuery.isBlank()
}
