package com.rajatt7z.fitbykit.viewModels

import androidx.lifecycle.ViewModel

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor() : ViewModel() {
    var lastPlaybackPosition: Float = 0f
}