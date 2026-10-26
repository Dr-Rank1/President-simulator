package com.presidentsimulator.game.audio

import android.content.Context
import android.media.MediaPlayer
import com.presidentsimulator.game.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SoundManager {
    private var mediaPlayer: MediaPlayer? = null
    private val _isMusicEnabled = MutableStateFlow(true)
    val isMusicEnabled: StateFlow<Boolean> = _isMusicEnabled.asStateFlow()

    fun initialize(context: Context) {
        if (mediaPlayer == null) {
            try {
                mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.fonmusic)?.apply {
                    isLooping = true
                    setVolume(0.4f, 0.4f)
                    if (_isMusicEnabled.value) {
                        start()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleMusic(context: Context) {
        _isMusicEnabled.value = !_isMusicEnabled.value
        if (_isMusicEnabled.value) {
            if (mediaPlayer == null) {
                initialize(context)
            } else {
                try {
                    mediaPlayer?.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            try {
                mediaPlayer?.pause()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resume() {
        try {
            if (_isMusicEnabled.value && mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
