package com.rajatt7z.fitbykit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rajatt7z.fitbykit.adapters.SessionAdapter
import com.rajatt7z.fitbykit.databinding.FragmentWorkoutHistoryBinding
import com.rajatt7z.fitbykit.viewModels.ProgressViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkoutHistoryFragment : Fragment() {

    private var _binding: FragmentWorkoutHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProgressViewModel by viewModels()
    private val sessionAdapter = SessionAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.rvSessions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSessions.adapter = sessionAdapter

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.sessions.observe(viewLifecycleOwner) { sessions ->
            if (sessions.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvSessions.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvSessions.visibility = View.VISIBLE
                sessionAdapter.submitList(sessions)
            }
        }

        viewModel.fetchWorkoutHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
