package com.rajatt7z.fitbykit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R
import com.rajatt7z.workout_api.Exercise

class ExerciseAdapter(
    private var list: List<Exercise>,
    private val onExerciseClick: (String) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: MaterialTextView = view.findViewById(R.id.exercise_name)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val item = list[position]
        val name = item.translations.firstOrNull { it.language == 2 }?.name
        holder.nameText.text = name ?: "Unnamed"
        holder.itemView.setOnClickListener {
            val videoUrl = exerciseVideoMap[name] // lookup from the map
            if (videoUrl != null) {
                onExerciseClick(videoUrl)
            }
        }
    }


    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<Exercise>) {
        list = newList
        notifyDataSetChanged()
    }

    companion object {
        private val exerciseVideoMap = mapOf(

            // Shoulders
            "Barbell Wrist Curl" to "https://www.youtube.com/shorts/d5YiFNoiCa0",
            "Bear Walk" to "https://www.youtube.com/shorts/l8eRtgP7ZoY",
            "Punches" to "https://www.youtube.com/shorts/0G6PMagbQko",
            "Claps over the head" to "https://www.youtube.com/watch?v=C19KjmUad_I",
            "Shrugs on Multipress" to "https://www.youtube.com/shorts/kG4qXCYvITg",
            "Front lever tuck" to "https://www.youtube.com/shorts/FWFVx-_axc4",
            "Pseudo Planche Push-up" to "https://www.youtube.com/shorts/HkGcKQ36Mfo",
            "Bag training" to "https://www.youtube.com/shorts/BkTWWuN5fVY",
            "Side to Side Push Ups" to "https://www.youtube.com/shorts/mzr0RYNDzzI",
            "Shrugs, Dumbells" to "https://www.youtube.com/shorts/aEDmpZSLAYk",

            // Bicep
            "Biceps Curls With Barbell" to "https://www.youtube.com/shorts/ez3YoWf62Eg",
            "Biceps Curls With Dumbbell" to "https://www.youtube.com/shorts/MKWBV29S6c0",
            "Biceps Curls With SZ-bar" to "https://www.youtube.com/shorts/KFinlAT6aEo",
            "Biceps Curl With Cable" to "https://www.youtube.com/shorts/CrbTqNOlFgE",
            "Dumbbell Incline Curl" to "https://www.youtube.com/shorts/0-qmVm4tHDw",
            "Dumbbells on Scott Machine" to "https://www.youtube.com/watch?v=P2iFr1HvN3g",
            "Hammercurls" to "https://www.youtube.com/shorts/lmIo_gVE8T4",
            "Hammercurls on Cable" to "https://www.youtube.com/shorts/HTtd5uMFVz8",
            "Hercules Pillars" to "https://www.youtube.com/shorts/XiYxVwJJhog",
            "Muscle up" to "https://www.youtube.com/shorts/IvylUo8C-go",

            //Hamstrings
            "Barbell Wrist Curl" to "https://www.youtube.com/shorts/d5YiFNoiCa0",
            "Good Mornings" to "https://www.youtube.com/shorts/7cpldMZjLOs",
            "High Knee Jumps" to "https://www.youtube.com/shorts/LJMrXG_vPQ8",
            "Leg Curl" to "https://www.youtube.com/shorts/lGNeJsdqJwg",
            "Leg Curls (laying)" to "https://www.youtube.com/shorts/lGNeJsdqJwg",
            "Leg Curls (sitting)" to "https://www.youtube.com/shorts/6_cLOU9BpgE",
            "Leg Curls (standing)" to "https://www.youtube.com/watch?v=LaDKpYN9FDw",
            "Leg Press" to "https://www.youtube.com/shorts/EotSw18oR9w",
            "Pistol Squat" to "https://www.youtube.com/shorts/IfESGr170DY",

            //Calves
            "Bear Walk" to "https://www.youtube.com/shorts/l8eRtgP7ZoY",
            "Calf Press Using Leg Press Machine" to "https://www.youtube.com/shorts/4rfJPBk2yuM",
            "Calf Raises on Hackenschmitt Machine" to "https://www.youtube.com/shorts/IrrmU7_swBI",
            "High Knee Jumps" to "https://www.youtube.com/shorts/LJMrXG_vPQ8",
            "Leg Press" to "https://www.youtube.com/shorts/EotSw18oR9w",
            "Skipping - Standard" to "https://www.youtube.com/shorts/WGx8nfjlU-g",
            "Standing Calf Raises" to "https://www.youtube.com/shorts/a-x_NR-ibos",
            "Calf raises, one legged" to "https://www.youtube.com/shorts/E1mG5L9rpFc",
            "Rowing Machine" to "https://www.youtube.com/shorts/bCxq4zMHpzs",
            "Walking" to "https://www.youtube.com/shorts/BYe4uyGF-h4",

            //Glutes
            "Seated Hip Adduction" to "https://www.youtube.com/shorts/tu4o4quPv2k",
            "Front Squats" to "https://www.youtube.com/shorts/rKjh8K-ZxLc",
            "Full Sit Outs" to "https://www.youtube.com/shorts/Kh5A2WlZl4s",
            "Glute Bridge" to "https://www.youtube.com/shorts/X_IGw8U_e38",
            "Hip Thrust" to "https://www.youtube.com/shorts/96uDbymTaHM",
            "Kettlebell Swings" to "https://www.youtube.com/shorts/n1df4ASFeZU",
            "Leg Curl" to "https://www.youtube.com/shorts/_lgE0gPvbik",
            "Leg Press" to "https://www.youtube.com/shorts/EotSw18oR9w",
            "Low Box Squat - Wide Stance" to "https://www.youtube.com/shorts/2aIgi73HIw8",
            "Pistol Squat" to "https://www.youtube.com/shorts/36s9GQSM13s",

            //Lats
            "TRX Rows" to "https://www.youtube.com/shorts/Cpn2P7vn2vs",
            "Jalón abierto supino" to "https://www.youtube.com/shorts/W2x6zP9k7SM",
            "Bent Over Rowing Reverse" to "https://www.youtube.com/shorts/t9DDSK40PKY",
            "Jalón cerrado supino" to "https://www.youtube.com/shorts/amSuLWswuI0",
            "Archer Pull Up" to "https://www.youtube.com/shorts/eDP_OOhMTZ4",
            "Arca femoral una gamba" to "https://www.youtube.com/shorts/1R8xVRE15Bs",
            "Bent Over Rowing Reverse" to "https://www.youtube.com/shorts/t9DDSK40PKY",
            "Hyper Y W Combo" to "https://www.youtube.com/shorts/1mQ7cHckWEE",
            "High Pull" to "https://www.youtube.com/shorts/IeOqdw9WI90",
            "Pullover Machine" to "https://www.youtube.com/shorts/1tWmRBJwzsY",

            //Chest
            "Hindu Pushups" to "https://www.youtube.com/shorts/QraO1UIy1Uw",
            "Incline Push up" to "https://www.youtube.com/shorts/SOu-3_YyX2c",
            "Butterfly Narrow Grip" to "https://www.youtube.com/shorts/4yKLxOsrGfg",
            "Leverage Machine Chest Press" to "https://www.youtube.com/shorts/Qu7-ceCvq7w",
            "Isometric Wipers" to "https://www.youtube.com/watch?v=Qbw79-LJrNA",
            "Bear Walk" to "https://www.youtube.com/shorts/l8eRtgP7ZoY",
            "Plank" to "https://www.youtube.com/shorts/xe2MXatLTUw",
            "Butterfly" to "https://www.youtube.com/shorts/p7biTTJvs8g",
            "Pike Push ups" to "https://www.youtube.com/shorts/V6BtY3Lt0Ys",
            "Bag training" to "https://www.youtube.com/shorts/BkTWWuN5fVY",

            //Quads
            "Barbell Lunges Standing" to "https://www.youtube.com/shorts/EWBiNhxDnmQ",
            "Single Leg Extension" to "https://www.youtube.com/shorts/00oU4iadGsY",
            "Braced Squat" to "https://www.youtube.com/shorts/syRJ0VRSzlA",
            "Dumbbell Goblet Squat" to "https://www.youtube.com/shorts/YuRik26Rd-M",
            "Dumbbell Lunges Standing" to "https://www.youtube.com/shorts/mJilHWIBWO8",
            "Dumbbell Lunges Walking" to "https://www.youtube.com/shorts/6Rhchj0nCp8",
            "High Knee Jumps" to "https://www.youtube.com/shorts/bvBHOqE_KAE",
            "Hindu Squats" to "https://www.youtube.com/shorts/HRQSFwVoEYM",
            "Jumping Jacks" to "https://www.youtube.com/shorts/DLq5hAO2t-Q",
            "Squats on Multipress" to "https://www.youtube.com/shorts/iKCJCydYYrE",

            //Abs
            "Abdominal Stabilization" to "https://www.youtube.com/shorts/acj52MXBaeo",
            "Crunches" to "https://www.youtube.com/shorts/eeJ_CYqSoT4",
            "Incline Crunches" to "https://www.youtube.com/shorts/D_6tXVmq_nM",
            "Crunches on Machine" to "https://www.youtube.com/shorts/K2m0jj6RfYg",
            "Crunches With Cable" to "https://www.youtube.com/shorts/ByZJuk85YuE",
            "Crunches With Legs Up" to "https://www.youtube.com/shorts/yp7UKH_mBvY",
            "Flutter Kicks" to "https://www.youtube.com/shorts/tPmybsDX8ZY",
            "Full Sit Outs" to "https://www.youtube.com/shorts/yyrUZkDivZY",
            "Hanging Leg Raises" to "https://www.youtube.com/shorts/XQc0WHO90Lk",

            //Triceps
            "Barbell Triceps Extension" to "https://www.youtube.com/shorts/K3mFeNz4e3w",
            "Bench Press Narrow Grip" to "https://www.youtube.com/shorts/sRcc5oaHqhk",
            "Dips" to "https://www.youtube.com/shorts/6IknX1l-XhM",
            "Dips Between Two Benches" to "https://www.youtube.com/shorts/4ua3MzaU0QU",
            "Dumbbell Triceps Extension" to "https://www.youtube.com/shorts/b_r_LW4HEcM",
            "Skullcrusher Dumbbells" to "https://www.youtube.com/shorts/Q2cA5xv2rSs",
            "Skullcrusher SZ-bar" to "https://www.youtube.com/shorts/zR9gty7LUxE",
            "Headstand Pushup" to "https://www.youtube.com/shorts/gSjHRuRQ4hk",
            )
    }
}
