package com.rajatt7z.fitbykit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.adapters.DictionaryElementAdapter
import com.rajatt7z.fitbykit.databinding.FragmentCategoryListBinding
import com.rajatt7z.fitbykit.viewmodel.DictionaryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryListFragment : Fragment() {

    private var _binding: FragmentCategoryListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DictionaryViewModel by viewModels()
    private lateinit var adapter: DictionaryElementAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter = DictionaryElementAdapter { id, name ->
            val action = CategoryListFragmentDirections.actionCategoryListFragmentToExercisesFragment(
                filterId = id,
                filterType = "category",
                filterName = name
            )
            findNavController().navigate(action)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.submitList(categories.map { Pair(it.id, it.name) })
        }

        viewModel.fetchCategories()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
