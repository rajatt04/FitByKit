package com.rajatt7z.fitbykit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rajatt7z.fitbykit.adapters.TrophyAdapter
import com.rajatt7z.fitbykit.databinding.FragmentAchievementsBinding
import com.rajatt7z.fitbykit.viewModels.ProgressViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProgressViewModel by viewModels()
    private val trophyAdapter = TrophyAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.rvTrophies.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTrophies.adapter = trophyAdapter

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        // Build unlocked set from user trophies
        viewModel.userTrophies.observe(viewLifecycleOwner) { userTrophies ->
            val unlockedIds = userTrophies.map { it.trophy }.toSet()
            val allTrophies = viewModel.trophies.value ?: emptyList()
            trophyAdapter.submitData(allTrophies, unlockedIds)
            binding.tvTrophyCount.text = "Trophies Earned: ${userTrophies.size}"
        }

        viewModel.trophies.observe(viewLifecycleOwner) { allTrophies ->
            val unlockedIds = (viewModel.userTrophies.value ?: emptyList()).map { it.trophy }.toSet()
            trophyAdapter.submitData(allTrophies, unlockedIds)
        }

        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            if (stats.isNotEmpty()) {
                val totalWeight = stats.firstOrNull()?.total_weight_lifted ?: "—"
                binding.tvTotalWeight.text = "Total Weight Lifted: $totalWeight kg"
            }
        }

        viewModel.fetchAchievements()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
