package com.ella.music.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.ella.music.data.model.Song
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executor

class ExoPlayerManager(private val context: Context) {

    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState: StateFlow<Int> = _playbackState.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private var playlist = mutableListOf<Song>()
    private var playerListener: Player.Listener? = null

    private val directExecutor = Executor { it.run() }

    fun connect() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            setupListener()
        }, directExecutor)
    }

    fun disconnect() {
        playerListener?.let { mediaController?.removeListener(it) }
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
    }

    private fun setupListener() {
        playerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _playbackState.value = playbackState
                _duration.value = mediaController?.duration?.coerceAtLeast(0) ?: 0L
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentSong()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _shuffleEnabled.value = shuffleModeEnabled
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = repeatMode
            }

            override fun onPlayerError(error: PlaybackException) {
                skipToNext()
            }
        }
        mediaController?.addListener(playerListener!!)

        _shuffleEnabled.value = mediaController?.shuffleModeEnabled ?: false
        _repeatMode.value = mediaController?.repeatMode ?: Player.REPEAT_MODE_OFF
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        playlist.clear()
        playlist.addAll(songs)

        val mediaItems = songs.map { song ->
            val builder = MediaItem.Builder()
                .setUri(song.path.toUri())
                .setMediaId(song.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .build()
                )

            if (song.mimeType.isNotBlank()) {
                builder.setMimeType(song.mimeType)
            }

            builder.build()
        }

        mediaController?.apply {
            setMediaItems(mediaItems, startIndex, 0L)
            prepare()
            play()
        }
    }

    fun playSong(song: Song) {
        val index = playlist.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            mediaController?.seekToDefaultPosition(index)
            mediaController?.play()
        } else {
            setPlaylist(listOf(song), 0)
        }
    }

    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun skipToNext() {
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    fun toggleShuffle() {
        mediaController?.shuffleModeEnabled = !(mediaController?.shuffleModeEnabled ?: false)
    }

    fun toggleRepeat() {
        val current = mediaController?.repeatMode ?: Player.REPEAT_MODE_OFF
        val next = when (current) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        mediaController?.repeatMode = next
    }

    fun updatePosition() {
        _currentPosition.value = mediaController?.currentPosition?.coerceAtLeast(0) ?: 0L
        _duration.value = mediaController?.duration?.coerceAtLeast(0) ?: 0L
    }

    private fun updateCurrentSong() {
        val currentIndex = mediaController?.currentMediaItemIndex ?: return
        _currentSong.value = if (currentIndex in playlist.indices) playlist[currentIndex] else null
        _duration.value = mediaController?.duration?.coerceAtLeast(0) ?: 0L
    }
}
