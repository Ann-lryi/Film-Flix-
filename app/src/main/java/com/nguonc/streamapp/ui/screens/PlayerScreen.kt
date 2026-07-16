package com.nguonc.streamapp.ui.screens

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.nguonc.streamapp.data.model.NetworkResult
import com.nguonc.streamapp.ui.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val detailState by viewModel.detailState.collectAsState()
    val currentServerIndex by viewModel.currentServerIndex.collectAsState()
    val currentEpisode by viewModel.currentEpisode.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val aspectRatioMode by viewModel.aspectRatioMode.collectAsState()

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }

    // Gesture HUD states
    var showBrightnessHud by remember { mutableStateOf(false) }
    var brightnessPercent by remember { mutableStateOf(50) }
    var showVolumeHud by remember { mutableStateOf(false) }
    var volumePercent by remember { mutableStateOf(50) }
    var showSeekHud by remember { mutableStateOf(false) }
    var seekDeltaSeconds by remember { mutableStateOf(0) }

    // Bottom Sheets state
    var showEpisodeSheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = (playbackState == Player.STATE_BUFFERING)
                if (playbackState == Player.STATE_READY) {
                    durationMs = exoPlayer.duration.coerceAtLeast(0L)
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Keep position updated every second
    LaunchedEffect(isPlaying, exoPlayer) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            durationMs = exoPlayer.duration.coerceAtLeast(0L)
            kotlinx.coroutines.delay(1000L)
        }
    }

    // Update Stream URL when currentEpisode changes
    LaunchedEffect(currentEpisode) {
        currentEpisode?.let { ep ->
            val streamUrl = ep.getBestStreamUrl()
            if (streamUrl.isNotEmpty()) {
                val mediaItem = MediaItem.fromUri(streamUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            }
        }
    }

    // Update Speed
    LaunchedEffect(playbackSpeed) {
        exoPlayer.setPlaybackSpeed(playbackSpeed)
    }

    // Keep screen on while playing video
    val view = LocalView.current
    DisposableEffect(view) {
        val activity = view.context as? Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(isLocked) {
                if (!isLocked) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val screenWidth = size.width
                            if (offset.x < screenWidth * 0.4f) {
                                showBrightnessHud = true
                            } else if (offset.x > screenWidth * 0.6f) {
                                showVolumeHud = true
                            } else {
                                showSeekHud = true
                                seekDeltaSeconds = 0
                            }
                        },
                        onDragEnd = {
                            if (showSeekHud && seekDeltaSeconds != 0) {
                                val newPos = (exoPlayer.currentPosition + seekDeltaSeconds * 1000L)
                                    .coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
                                exoPlayer.seekTo(newPos)
                                currentPositionMs = newPos
                            }
                            showBrightnessHud = false
                            showVolumeHud = false
                            showSeekHud = false
                        },
                        onDragCancel = {
                            showBrightnessHud = false
                            showVolumeHud = false
                            showSeekHud = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val screenWidth = size.width
                            val screenHeight = size.height
                            val activity = context as? Activity

                            if (showBrightnessHud && activity != null) {
                                val delta = -dragAmount.y / (screenHeight * 0.5f)
                                val window = activity.window
                                val attrs = window.attributes
                                var currentBrightness = attrs.screenBrightness
                                if (currentBrightness < 0) currentBrightness = 0.5f
                                val newBrightness = (currentBrightness + delta).coerceIn(0.01f, 1.0f)
                                attrs.screenBrightness = newBrightness
                                window.attributes = attrs
                                brightnessPercent = (newBrightness * 100).toInt()
                            } else if (showVolumeHud) {
                                val delta = (-dragAmount.y / (screenHeight * 0.5f)) * maxVolume
                                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                val newVol = (currentVol + delta.toInt()).coerceIn(0, maxVolume)
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                volumePercent = if (maxVolume > 0) (newVol * 100 / maxVolume) else 50
                            } else if (showSeekHud) {
                                val deltaSec = (dragAmount.x / (screenWidth * 0.3f)) * 30f
                                seekDeltaSeconds += deltaSec.toInt()
                            }
                        }
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showControls = !showControls
                    }
                )
            }
    ) {
        // ExoPlayer Video View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = when (aspectRatioMode) {
                    1 -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    2 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // Gesture HUD Overlay
        if (showBrightnessHud) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 32.dp)
                    .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(text = "☀ Độ sáng: $brightnessPercent%", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        if (showVolumeHud) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 32.dp)
                    .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(text = "🔊 Âm lượng: $volumePercent%", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        if (showSeekHud) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                val sign = if (seekDeltaSeconds >= 0) "+" else ""
                Text(
                    text = "Tua: $sign${seekDeltaSeconds}s",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Lock Screen Button (Always accessible when locked and controls tapped)
        if (isLocked && showControls) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
            ) {
                IconButton(
                    onClick = { viewModel.toggleLock() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Red.copy(alpha = 0.8f), shape = RoundedCornerShape(24.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Mở khóa",
                        tint = Color.White
                    )
                }
            }
        }

        // Main Controls Overlay (When unlocked & showControls = true)
        AnimatedVisibility(
            visible = showControls && !isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            exoPlayer.stop()
                            onBack()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val movieTitle = (detailState as? NetworkResult.Success)?.data?.movie?.name ?: ""
                        Column {
                            Text(
                                text = movieTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentEpisode?.name ?: "Tập 1",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Aspect Ratio Mode Button
                        IconButton(onClick = { viewModel.cycleAspectRatio() }) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "Đổi tỷ lệ màn hình",
                                tint = Color.White
                            )
                        }
                        // Lock Screen Toggle
                        IconButton(onClick = { viewModel.toggleLock() }) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Khóa màn hình",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Center Play/Pause & Fast Seek Buttons
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Lùi 10s",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(36.dp),
                        modifier = Modifier.size(72.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition + 10_000L).coerceAtMost(durationMs)
                            exoPlayer.seekTo(newPos)
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Tua 10s",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                // Bottom Bar (Seek Slider, Time, Speed, Episodes)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPositionMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatTime(durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Slider(
                        value = if (durationMs > 0) currentPositionMs.toFloat() else 0f,
                        onValueChange = { valPos ->
                            currentPositionMs = valPos.toLong()
                        },
                        onValueChangeFinished = {
                            exoPlayer.seekTo(currentPositionMs)
                        },
                        valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Speed Button
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.clickable { showSpeedSheet = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Tốc độ",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${playbackSpeed}x",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Episodes Switcher Button
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable { showEpisodeSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = "Chọn tập",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Danh Sách Tập",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Episode Selection Sheet
    if (showEpisodeSheet) {
        val servers = (detailState as? NetworkResult.Success)?.data?.episodes ?: emptyList()
        val currentServer = servers.getOrNull(currentServerIndex) ?: servers.firstOrNull()
        val episodes = currentServer?.getEpisodeList() ?: emptyList()

        ModalBottomSheet(onDismissRequest = { showEpisodeSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Chọn Tập Phim (${episodes.size} tập)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    episodes.forEach { ep ->
                        val isSelected = currentEpisode?.slug == ep.slug
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.switchEpisode(ep)
                                showEpisodeSheet = false
                            },
                            label = {
                                Text(
                                    text = ep.name ?: "Tập ?",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Speed Selection Sheet
    if (showSpeedSheet) {
        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        ModalBottomSheet(onDismissRequest = { showSpeedSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Tốc độ phát",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    speeds.forEach { spd ->
                        val isSelected = playbackSpeed == spd
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setPlaybackSpeed(spd)
                                showSpeedSheet = false
                            },
                            label = {
                                Text(
                                    text = "${spd}x",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
