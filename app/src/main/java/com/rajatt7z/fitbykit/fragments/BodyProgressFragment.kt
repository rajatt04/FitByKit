package com.rajatt7z.fitbykit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rajatt7z.fitbykit.adapters.ProgressEntryAdapter
import com.rajatt7z.fitbykit.databinding.FragmentBodyProgressBinding
import com.rajatt7z.fitbykit.viewModels.ProgressViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BodyProgressFragment : Fragment() {

    private var _binding: FragmentBodyProgressBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProgressViewModel by viewModels()

    private val weightAdapter = ProgressEntryAdapter()
    private val measureAdapter = ProgressEntryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBodyProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.rvWeightEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWeightEntries.adapter = weightAdapter

        binding.rvMeasurements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMeasurements.adapter = measureAdapter

        // Observe loading
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        // Observe weight entries
        viewModel.weightEntries.observe(viewLifecycleOwner) { entries ->
            val items = entries.map { Pair(it.date, "${it.weight} kg") }
            weightAdapter.submitList(items)
        }

        // Observe measurements
        viewModel.measurements.observe(viewLifecycleOwner) { measurements ->
            val items = measurements.map { Pair(it.date, "${it.value} cm") }
            measureAdapter.submitList(items)
        }

        // Observe gallery count
        viewModel.gallery.observe(viewLifecycleOwner) { photos ->
            binding.tvGalleryCount.text = "${photos.size} photos on WGER"
        }

        // Observe success messages
        viewModel.successMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }

        // Log weight button
        binding.btnLogWeight.setOnClickListener {
            val weight = binding.etWeight.text.toString()
            if (weight.isNotBlank()) {
                viewModel.logWeight(weight)
                binding.etWeight.text?.clear()
            }
        }

        // Log measurement button
        binding.btnLogMeasure.setOnClickListener {
            val catId = binding.etMeasureCatId.text.toString().toIntOrNull()
            val value = binding.etMeasureValue.text.toString()
            if (catId != null && value.isNotBlank()) {
                viewModel.logMeasurement(catId, value)
                binding.etMeasureCatId.text?.clear()
                binding.etMeasureValue.text?.clear()
            }
        }

        viewModel.fetchBodyProgress()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
