package com.rajatt7z.fitbykit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rajatt7z.fitbykit.adapters.DictionaryElementAdapter
import com.rajatt7z.fitbykit.databinding.FragmentEquipmentListBinding
import com.rajatt7z.fitbykit.viewModels.DictionaryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EquipmentListFragment : Fragment() {

    private var _binding: FragmentEquipmentListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DictionaryViewModel by viewModels()
    private lateinit var adapter: DictionaryElementAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEquipmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter = DictionaryElementAdapter { id, name ->
            val action = EquipmentListFragmentDirections.actionEquipmentListFragmentToExercisesFragment(
                filterId = id,
                filterType = "equipment",
                filterName = name
            )
            findNavController().navigate(action)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.equipment.observe(viewLifecycleOwner) { equipment ->
            adapter.submitList(equipment.map { Pair(it.id, it.name) })
        }

        viewModel.fetchEquipment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
