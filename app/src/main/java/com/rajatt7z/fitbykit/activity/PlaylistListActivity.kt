package com.rajatt7z.fitbykit.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rajatt7z.fitbykit.database.PlaylistDatabase
import com.rajatt7z.fitbykit.databinding.ActivityPlaylistListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaylistListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistListBinding
    private lateinit var db: PlaylistDatabase
    private lateinit var adapter: PlaylistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = PlaylistDatabase.getDatabase(this)

        adapter = PlaylistAdapter(
            onEditClick = { playlist ->
                val intent = Intent(this, CreatePlaylistActivity::class.java)
                intent.putExtra("playlist_id", playlist.id)
                startActivity(intent)
            },
            onDeleteClick = { playlist ->
                lifecycleScope.launch {
                    db.playlistDao().deletePlaylist(playlist)
                }
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            db.playlistDao().getAllPlaylists().collectLatest { playlists ->
                adapter.submitList(playlists)
            }
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, CreatePlaylistActivity::class.java))
        }
    }
}
