package com.rajatt7z.fitbykit.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.loadingindicator.LoadingIndicator
import com.google.android.material.snackbar.Snackbar
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.activity.MealPlayer
import com.rajatt7z.fitbykit.adapters.FilterMealAdapter
import com.rajatt7z.fitbykit.adapters.MealAdapter
import com.rajatt7z.fitbykit.viewModels.DietViewModel
import com.rajatt7z.workout_api.getIngredientsPair
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class diet : Fragment() {

    private var isSearching = false
    private lateinit var mealAdapter: MealAdapter
    private lateinit var filterMealAdapter: FilterMealAdapter
    private lateinit var recyclerView: RecyclerView
    private val viewModel: DietViewModel by viewModels()
    private lateinit var loadingIndicator: LoadingIndicator
    private lateinit var fabSearch: FloatingActionButton

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_diet, container, false)

        recyclerView     = view.findViewById(R.id.mealRecyclerView)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        fabSearch        = view.findViewById(R.id.fabSearch)

        // ── Adapters ────────────────────────────────────────────────────────
        mealAdapter = MealAdapter(emptyList()) { meal ->
            openMealPlayer(
                name         = meal.strMeal,
                instructions = meal.strInstructions,
                thumb        = meal.strMealThumb,
                category     = meal.strCategory,
                area         = meal.strArea,
                tags         = meal.strTags,
                youtube      = meal.strYoutube,
                ingredients  = ArrayList(meal.getIngredientsPair().map { it.ingredient }),
                measures     = ArrayList(meal.getIngredientsPair().map { it.measure })
            )
        }

        filterMealAdapter = FilterMealAdapter(emptyList()) { filterMeal ->
            // Lookup full meal details before opening MealPlayer
            viewModel.lookupMeal(filterMeal.idMeal)
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = mealAdapter

        // ── LiveData observers ──────────────────────────────────────────────
        viewModel.isFilterMode.observe(viewLifecycleOwner) { isFilter ->
            recyclerView.adapter = if (isFilter) filterMealAdapter else mealAdapter
        }

        viewModel.meals.observe(viewLifecycleOwner) { meals ->
            mealAdapter.updateMeals(meals)
            if (isSearching && meals.isEmpty()) {
                Snackbar.make(requireView(), "No meals found!", Snackbar.LENGTH_SHORT).show()
            }
            isSearching = false
        }

        viewModel.filterMeals.observe(viewLifecycleOwner) { filterMeals ->
            filterMealAdapter.updateMeals(filterMeals)
            if (filterMeals.isEmpty()) {
                Snackbar.make(requireView(), "No meals found for this country!", Snackbar.LENGTH_SHORT).show()
            }
        }

        // ── When lookup completes after tapping a country meal card ─────────
        viewModel.selectedMeal.observe(viewLifecycleOwner) { meal ->
            meal ?: return@observe
            openMealPlayer(
                name         = meal.strMeal,
                instructions = meal.strInstructions,
                thumb        = meal.strMealThumb,
                category     = meal.strCategory,
                area         = meal.strArea,
                tags         = meal.strTags,
                youtube      = meal.strYoutube,
                ingredients  = ArrayList(meal.getIngredientsPair().map { it.ingredient }),
                measures     = ArrayList(meal.getIngredientsPair().map { it.measure })
            )
            viewModel.clearSelectedMeal()
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        fabSearch.setOnClickListener {
            val bottomSheet = SearchBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, SearchBottomSheetFragment.TAG)
        }

        // ── Initial load: fetch all ~300 meals ──────────────────────────────
        viewModel.loadTopMealsIfNoQuery()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener(SearchBottomSheetFragment.RESULT_KEY) { _, bundle ->
            when (bundle.getString("type")) {
                "search"  -> {
                    val query = bundle.getString("query") ?: return@setFragmentResultListener
                    isSearching = true
                    viewModel.searchMeals(query)
                }
                "country" -> {
                    val area = bundle.getString("area") ?: return@setFragmentResultListener
                    viewModel.filterByCountry(area)
                }
                "letter"  -> {
                    val letterStr = bundle.getString("letter") ?: return@setFragmentResultListener
                    viewModel.filterByLetter(letterStr.first())
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, statusBarInsets.top, v.paddingRight, v.paddingBottom)
            insets
        }
    }

    private fun openMealPlayer(
        name: String, instructions: String, thumb: String,
        category: String?, area: String, tags: String?, youtube: String,
        ingredients: ArrayList<String>, measures: ArrayList<String>
    ) {
        startActivity(Intent(requireContext(), MealPlayer::class.java).apply {
            putExtra("mealName", name)
            putExtra("mealInstructions", instructions)
            putExtra("mealThumb", thumb)
            putExtra("mealCategory", category)
            putExtra("mealArea", area)
            putExtra("mealTags", tags)
            putExtra("mealVideo", youtube)
            putStringArrayListExtra("mealIngredients", ingredients)
            putStringArrayListExtra("mealMeasures", measures)
        })
    }
}