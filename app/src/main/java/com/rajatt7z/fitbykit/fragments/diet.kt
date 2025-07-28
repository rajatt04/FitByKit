package com.rajatt7z.fitbykit.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.Utils.calculateMacros
import com.rajatt7z.fitbykit.adapters.MealAdapter
import com.rajatt7z.fitbykit.viewModels.DietViewModel

class diet : Fragment() {

    private var isSearching = false
    private lateinit var macrosCard: MaterialTextView
    private lateinit var mealAdapter: MealAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: DietViewModel
    private lateinit var loadingIndicator: CircularProgressIndicator
    private lateinit var searchEditText: TextInputEditText
    private lateinit var searchButton: ImageButton

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_diet, container, false)

        macrosCard = view.findViewById(R.id.macrosCard)
        recyclerView = view.findViewById(R.id.mealRecyclerView)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        searchEditText = view.findViewById(R.id.searchEdit)
        searchButton = view.findViewById(R.id.searchButton)

        viewModel = ViewModelProvider(this)[DietViewModel::class.java]

        viewModel.loadTopMealsIfNoQuery()

        mealAdapter = MealAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
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

        searchEditText.setText(viewModel.getLastQuery())

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                if (input.isEmpty()) {
                    viewModel.loadTopMeals()
                }
            }
        })

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotBlank()) {
                isSearching = true
                viewModel.searchMeals(query)
            }
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