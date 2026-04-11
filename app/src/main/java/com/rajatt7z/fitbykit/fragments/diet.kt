package com.rajatt7z.fitbykit.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.Utils.calculateMacros
import com.rajatt7z.fitbykit.activity.MealPlayer
import com.rajatt7z.fitbykit.adapters.MealAdapter
import com.rajatt7z.fitbykit.viewModels.DietViewModel
import com.rajatt7z.workout_api.getIngredientsPair
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class diet : Fragment() {

    private var isSearching = false
    private lateinit var macrosCard: MaterialTextView
    private lateinit var mealAdapter: MealAdapter
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

        macrosCard = view.findViewById(R.id.macrosCard)
        recyclerView = view.findViewById(R.id.mealRecyclerView)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        fabSearch = view.findViewById(R.id.fabSearch)

        viewModel.loadTopMealsIfNoQuery()

        mealAdapter = MealAdapter(emptyList()) { meal ->
            val ingredientPairs = meal.getIngredientsPair()
            val ingredientStrings = ArrayList<String>()
            val measureStrings = ArrayList<String>()

            for (pair in ingredientPairs) {
                ingredientStrings.add(pair.ingredient)
                measureStrings.add(pair.measure)
            }

            val intent = Intent(requireContext(), MealPlayer::class.java).apply {
                putExtra("mealName", meal.strMeal)
                putExtra("mealInstructions", meal.strInstructions)
                putExtra("mealThumb", meal.strMealThumb)
                putExtra("mealCategory", meal.strCategory)
                putExtra("mealArea", meal.strArea)
                putExtra("mealTags", meal.strTags)
                putExtra("mealVideo", meal.strYoutube)
                putStringArrayListExtra("mealIngredients", ingredientStrings)
                putStringArrayListExtra("mealMeasures", measureStrings)
            }
            startActivity(intent)

        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = mealAdapter

        mealAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                viewModel.setLoading(false)
            }
        })

        viewModel.meals.observe(viewLifecycleOwner) { meals ->
            mealAdapter.updateMeals(meals)
            if (isSearching && meals.isEmpty()) {
                Snackbar.make(requireView(), "Meal not found!", Snackbar.LENGTH_SHORT).show()
            }
            isSearching = false
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        fabSearch.setOnClickListener {
            val bottomSheet = SearchBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, SearchBottomSheetFragment.TAG)
        }

        val sharedPref =
            requireContext().getSharedPreferences("userPref", AppCompatActivity.MODE_PRIVATE)
        val weight = sharedPref.getString("userWeight", null)?.toIntOrNull()
        val height = sharedPref.getString("userHeight", null)?.toIntOrNull()
        if (weight != null && height != null) {
            val macros = calculateMacros(weight, height)
            macrosCard.text = """
                You Need To Take These Macros
                🍗 Protein: ${macros.protein} g
                🥦 Carbs: ${macros.carbs} g
                🥑 Fats: ${macros.fats} g
            """.trimIndent()
        } else {
            macrosCard.text = "User data missing. Please complete your profile."
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setFragmentResultListener("searchRequestKey") { _, bundle ->
            val query = bundle.getString("query")
            if (!query.isNullOrBlank()) {
                isSearching = true
                viewModel.searchMeals(query)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                statusBarInsets.top,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }
    }
}