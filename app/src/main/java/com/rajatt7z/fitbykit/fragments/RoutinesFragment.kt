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
import com.rajatt7z.fitbykit.databinding.FragmentRoutinesBinding
import com.rajatt7z.fitbykit.viewModels.RoutineViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoutinesFragment : Fragment() {

    private var _binding: FragmentRoutinesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RoutineViewModel by viewModels()
    private lateinit var adapter: DictionaryElementAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutinesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // We reuse the DictionaryElementAdapter for Routines since Routines just have names
        adapter = DictionaryElementAdapter { id, name ->
            // Live tracking has been disabled/removed
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.routines.observe(viewLifecycleOwner) { routines ->
            adapter.submitList(routines.map { Pair(it.id, it.name) })
        }



        viewModel.fetchRoutines()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
