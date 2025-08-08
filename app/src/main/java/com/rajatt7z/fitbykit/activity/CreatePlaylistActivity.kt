package com.rajatt7z.fitbykit.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.rajatt7z.fitbykit.database.Playlist
import com.rajatt7z.fitbykit.database.PlaylistDatabase
import com.rajatt7z.fitbykit.databinding.ActivityCreatePlaylistBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreatePlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePlaylistBinding
    private lateinit var db: PlaylistDatabase
    private var playlistId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playlistId = intent.getIntExtra("playlist_id", -1).takeIf { it != -1 }

        if (playlistId != null) {
            lifecycleScope.launch {
                val playlist = db.playlistDao().getPlaylistById(playlistId!!)
                playlist?.let { loadPlaylistData(it) }
            }
        }

        db = PlaylistDatabase.getDatabase(this)

        val days = listOf(
            "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday", "Sunday"
        )
        val dayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, days)
        binding.daySelectAutocomplete.setAdapter(dayAdapter)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.saveFab.setOnClickListener {
            savePlaylist()
        }

        binding.cancelFab.setOnClickListener { finish() }
    }

    private fun loadPlaylistData(playlist: Playlist) {
        binding.nameInput.setText(playlist.name)
        binding.daySelectAutocomplete.setText(playlist.day, false)
        binding.notificationsSwitch.isChecked = playlist.notificationsEnabled
        binding.analyticsSwitch.isChecked = playlist.analyticsEnabled

        val cats = playlist.categories.split(", ")
        binding.chipWork.isChecked = cats.contains(binding.chipWork.text.toString())
        binding.chipPersonal.isChecked = cats.contains(binding.chipPersonal.text.toString())
        binding.chipUrgent.isChecked = cats.contains(binding.chipUrgent.text.toString())
        binding.chipFeedback.isChecked = cats.contains(binding.chipFeedback.text.toString())
    }

    private fun savePlaylist() {
        val name = binding.nameInput.text.toString().trim()
        val day = binding.daySelectAutocomplete.text.toString().trim()
        val notifications = binding.notificationsSwitch.isChecked
        val analytics = binding.analyticsSwitch.isChecked

        val categories = mutableListOf<String>()
        if (binding.chipWork.isChecked) categories.add(binding.chipWork.text.toString())
        if (binding.chipPersonal.isChecked) categories.add(binding.chipPersonal.text.toString())
        if (binding.chipUrgent.isChecked) categories.add(binding.chipUrgent.text.toString())
        if (binding.chipFeedback.isChecked) categories.add(binding.chipFeedback.text.toString())

        if (name.isEmpty() || day.isEmpty()) {
            binding.nameInputLayout.error = if (name.isEmpty()) "Enter day name" else null
            binding.daySelectInputLayout.error = if (day.isEmpty()) "Select a day" else null
            return
        }

        val playlist = Playlist(
            name = name,
            day = day,
            notificationsEnabled = notifications,
            analyticsEnabled = analytics,
            categories = categories.joinToString(", ")
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (playlistId == null) {
                db.playlistDao().insertPlaylist(playlist)
            } else {
                db.playlistDao().updatePlaylist(playlist.copy(id = playlistId!!))
            }
            finish()
        }
    }
}
