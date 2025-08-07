package com.rajatt7z.fitbykit.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.adapters.ExerciseAdapter
import com.rajatt7z.fitbykit.database.AppDatabase
import com.rajatt7z.fitbykit.database.LikedExerciseDao
import com.rajatt7z.fitbykit.databinding.ActivityLikedWorkoutsBinding
import com.rajatt7z.fitbykit.fragments.Workouts
import com.rajatt7z.fitbykit.navigation.FitByKitNav
import com.rajatt7z.workout_api.Exercise
import com.rajatt7z.workout_api.Translation
import kotlinx.coroutines.launch

class LikedWorkoutsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLikedWorkoutsBinding
    private lateinit var adapter: ExerciseAdapter
    private lateinit var dao: LikedExerciseDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLikedWorkoutsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdgeUI()
        binding.emptyPlaceholder.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("No Liked Workouts Found !!")
                .setMessage(
                    """
                        To like workouts, follow these steps:
        
                        1. Go to the Workout section.
                        2. Tap on a Muscle Group.
                        3. Tap the ❤️ icon on an exercise you enjoy.

                        You'll find all your liked workouts here!
                    """.trimIndent()
                )
                .setIcon(R.drawable.cannabis_48dp)
                .setPositiveButton("Take Me There") { _, _ ->
                    val intent = Intent(this, FitByKitNav::class.java).apply {
                        putExtra("navigate_to", "workouts")
                    }
                    startActivity(intent)
                }
                .setNegativeButton("Maybe Later", null)
                .show()
        }

        dao = AppDatabase.getDatabase(this).likedExerciseDao()
        val recyclerView = findViewById<RecyclerView>(R.id.likedRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val likedList = dao.getAll()
            val likedNamesSet = likedList.map { it.name }.toMutableSet()
            if (::adapter.isInitialized) {
                adapter.updateLikedSet(likedNamesSet)
            }
            if (likedList.isEmpty()) {
                binding.likedRecyclerView.visibility = View.GONE
                binding.emptyPlaceholder.visibility = View.VISIBLE
            } else {
                binding.likedRecyclerView.visibility = View.VISIBLE
                binding.emptyPlaceholder.visibility = View.GONE

                val exercises = likedList.map {
                    Exercise(
                        id = 0,
                        name = it.name,
                        category = null,
                        description = "",
                        equipment = emptyList(),
                        translations = listOf(
                            Translation(
                                language = 2,
                                id = 0,
                                name = it.name,
                                description = ""
                            )
                        )
                    )
                }

                adapter = ExerciseAdapter(this@LikedWorkoutsActivity, exercises, likedNamesSet) { videoUrl ->
                    val intent = Intent(this@LikedWorkoutsActivity, VideoPlayerActivity::class.java)
                    intent.putExtra("video_url", videoUrl)
                    startActivity(intent)
                }

                binding.likedRecyclerView.adapter = adapter
            }
        }
    }

    private fun setupEdgeToEdgeUI() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                statusBarInsets.top,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }
    }
}