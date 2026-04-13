package com.rajatt7z.fitbykit.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajatt7z.workout_api.FilterMeal
import com.rajatt7z.workout_api.Meal
import com.rajatt7z.workout_api.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DietViewModel @Inject constructor(
    private val repository: MealRepository
) : ViewModel() {

    private val _meals = MutableLiveData<List<Meal>>()
    val meals: LiveData<List<Meal>> = _meals

    private val _filterMeals = MutableLiveData<List<FilterMeal>>()
    val filterMeals: LiveData<List<FilterMeal>> = _filterMeals

    private val _selectedMeal = MutableLiveData<Meal?>()
    val selectedMeal: LiveData<Meal?> = _selectedMeal

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _isFilterMode = MutableLiveData<Boolean>(false)
    val isFilterMode: LiveData<Boolean> = _isFilterMode

    private var lastQuery: String = ""

    // ─── Initial Load: Fetch ALL ~300 meals A→Z in parallel ───────────────────
    fun fetchAllMeals() {
        viewModelScope.launch {
            _loading.value = true
            _isFilterMode.value = false
            try {
                val results = withContext(Dispatchers.IO) {
                    ('a'..'z').map { letter ->
                        async { repository.searchByLetter(letter) }
                    }.awaitAll()
                }
                val merged = results.flatten()
                    .distinctBy { it.idMeal }
                    .sortedBy { it.strMeal }
                _meals.value = merged
            } catch (e: Exception) {
                Log.e("DietViewModel", "fetchAllMeals error", e)
            } finally {
                _loading.value = false
            }
        }
    }

    // ─── Name Search ───────────────────────────────────────────────────────────
    fun searchMeals(query: String) {
        viewModelScope.launch {
            _loading.value = true
            _isFilterMode.value = false
            lastQuery = query
            try {
                _meals.value = repository.searchMeals(query)
            } catch (e: Exception) {
                Log.e("DietViewModel", "Search error: ", e)
                _meals.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    // ─── Browse by First Letter ────────────────────────────────────────────────
    fun filterByLetter(letter: Char) {
        viewModelScope.launch {
            _loading.value = true
            _isFilterMode.value = false
            lastQuery = ""
            try {
                _meals.value = repository.searchByLetter(letter)
            } catch (e: Exception) {
                Log.e("DietViewModel", "Letter filter error", e)
                _meals.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    // ─── Browse by Country (lightweight filter list) ───────────────────────────
    fun filterByCountry(area: String) {
        viewModelScope.launch {
            _loading.value = true
            _isFilterMode.value = true
            lastQuery = ""
            try {
                _filterMeals.value = repository.filterByArea(area)
            } catch (e: Exception) {
                Log.e("DietViewModel", "Country filter error", e)
                _filterMeals.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    // ─── Lookup full meal by ID (after clicking a FilterMeal card) ─────────────
    fun lookupMeal(id: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _selectedMeal.value = repository.lookupById(id)
            } catch (e: Exception) {
                Log.e("DietViewModel", "Lookup error", e)
                _selectedMeal.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearSelectedMeal() {
        _selectedMeal.value = null
    }

    fun loadTopMealsIfNoQuery() {
        if (lastQuery.isBlank()) fetchAllMeals()
    }

    fun getLastQuery(): String = lastQuery

    fun setLoading(value: Boolean) {
        _loading.value = value
    }
}
