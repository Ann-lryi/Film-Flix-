package com.aho.yunphim.ui.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.aho.yunphim.data.model.ServerGroup
import com.aho.yunphim.ui.components.FullScreenError
import com.aho.yunphim.ui.components.FullScreenLoading
import com.aho.yunphim.ui.theme.YunPhimColors
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    onSwitchEpisode: (serverIndex: Int, episodeIndex: Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        hideSystemBars(activity)
        onDispose {
            activity?.requestedOrientation = originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            showSystemBars(activity)
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply { playWhenReady = true }
    }
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                viewModel.onDirectPlaybackFailed()
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    // Nạp lại nguồn phát mỗi khi streamUrl đổi (kể cả sau khi WebView resolve xong link mới).
    LaunchedEffect(state) {
        val ready = state as? PlayerUiState.Ready ?: return@LaunchedEffect
        if (!ready.isResolvingFallback) {
            val currentUri = exoPlayer.currentMediaItem?.localConfiguration?.uri?.toString()
            if (currentUri != ready.streamUrl) {
                exoPlayer.setMediaItem(MediaItem.fromUri(ready.streamUrl))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
        }
    }

    // Khi ViewModel bật cờ resolving, tự chạy WebViewStreamResolver rồi báo kết quả ngược lại.
    LaunchedEffect(state) {
        val ready = state as? PlayerUiState.Ready ?: return@LaunchedEffect
        if (ready.isResolvingFallback) {
            val embedUrl = viewModel.embedUrlForFallback()
            val resolved = if (embedUrl.isNullOrBlank()) {
                null
            } else {
                WebViewStreamResolver.resolve(context, embedUrl, viewModel.streamcResolver)
            }
            if (resolved != null) viewModel.onFallbackResolved(resolved) else viewModel.onFallbackError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when (val s = state) {
            PlayerUiState.Loading -> FullScreenLoading()

            is PlayerUiState.Error -> FullScreenError(
                message = s.message,
                isSchemaMismatch = s.isSchemaMismatch,
                onRetry = viewModel::retry,
            )

            is PlayerUiState.Ready -> {
                PlayerSurface(exoPlayer = exoPlayer)
                if (s.isResolvingFallback) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = YunPhimColors.Accent)
                            Spacer(Modifier.height(8.dp))
                            Text("Đang dò link phát dự phòng qua WebView...", color = Color.White)
                        }
                    }
                } else {
                    PlayerControlsOverlay(
                        exoPlayer = exoPlayer,
                        state = s,
                        onBack = onBack,
                        onSwitchEpisode = onSwitchEpisode,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerSurface(exoPlayer: ExoPlayer) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PlayerControlsOverlay(
    exoPlayer: ExoPlayer,
    state: PlayerUiState.Ready,
    onBack: () -> Unit,
    onSwitchEpisode: (Int, Int) -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }

    var controlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var duration by remember { mutableLongStateOf(exoPlayer.duration.coerceAtLeast(0L)) }
    var position by remember { mutableLongStateOf(exoPlayer.currentPosition.coerceAtLeast(0L)) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPreview by remember { mutableFloatStateOf(0f) }
    var showEpisodeSheet by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf<String?>(null) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }

            override fun onEvents(player: Player, events: Player.Events) {
                duration = player.duration.coerceAtLeast(0L)
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    LaunchedEffect(isPlaying, isSeeking) {
        while (isPlaying && !isSeeking) {
            position = exoPlayer.currentPosition.coerceAtLeast(0L)
            delay(500)
        }
    }

    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(3000)
            controlsVisible = false
        }
    }

    LaunchedEffect(feedbackText) {
        if (feedbackText != null) {
            delay(700)
            feedbackText = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { controlsVisible = !controlsVisible },
                    onDoubleTap = { offset ->
                        val isRightSide = offset.x > size.width / 2
                        val delta = if (isRightSide) 10_000L else -10_000L
                        val newPos = (exoPlayer.currentPosition + delta)
                            .coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
                        exoPlayer.seekTo(newPos)
                        position = newPos
                        feedbackText = if (isRightSide) "+10s" else "-10s"
                    },
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        val isRightSide = change.position.x > size.width / 2
                        if (isRightSide) {
                            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            val delta = (-dragAmount / 20f).toInt()
                            val newVolume = (currentVolume + delta).coerceIn(0, maxVolume)
                            if (newVolume != currentVolume) {
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                            }
                            feedbackText = "Âm lượng ${newVolume * 100 / maxVolume}%"
                        } else {
                            val window = activity?.window
                            if (window != null) {
                                val lp = window.attributes
                                val currentBrightness = if (lp.screenBrightness < 0f) 0.5f else lp.screenBrightness
                                val newBrightness = (currentBrightness - dragAmount / 1000f).coerceIn(0.02f, 1f)
                                lp.screenBrightness = newBrightness
                                window.attributes = lp
                                feedbackText = "Độ sáng ${(newBrightness * 100).toInt()}%"
                            }
                        }
                    },
                )
            },
    ) {
        feedbackText?.let { text ->
            Surface(
                modifier = Modifier.align(Alignment.Center),
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.6f),
            ) {
                Text(text, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.55f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.65f),
                            ),
                        ),
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                        Text(
                            text = state.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = state.episodeLabel,
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    if (state.servers.isNotEmpty()) {
                        IconButton(onClick = { showEpisodeSheet = true }) {
                            Icon(Icons.Filled.PlaylistPlay, contentDescription = "Chọn tập", tint = Color.White)
                        }
                    }
                }

                IconButton(
                    onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                    modifier = Modifier.align(Alignment.Center).size(64.dp),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    if (state.fallbackFailed) {
                        Text(
                            text = "Không dò được link phát dự phòng - phim này có thể chưa được hỗ trợ đầy đủ.",
                            color = YunPhimColors.Error,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        state.streamcFailureDetail?.let { detail ->
                            Text(
                                text = detail,
                                color = YunPhimColors.TextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                    }
                    val displayPosition = if (isSeeking) seekPreview.toLong() else position
                    Slider(
                        value = displayPosition.toFloat(),
                        valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
                        onValueChange = {
                            isSeeking = true
                            seekPreview = it
                        },
                        onValueChangeFinished = {
                            exoPlayer.seekTo(seekPreview.toLong())
                            position = seekPreview.toLong()
                            isSeeking = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = YunPhimColors.Accent,
                            activeTrackColor = YunPhimColors.Accent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                        ),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatMillis(displayPosition), color = Color.White, style = MaterialTheme.typography.labelSmall)
                        Text(formatMillis(duration), color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    if (showEpisodeSheet) {
        EpisodeSwitcherSheet(
            servers = state.servers,
            currentServerIndex = state.serverIndex,
            currentEpisodeIndex = state.episodeIndex,
            onDismiss = { showEpisodeSheet = false },
            onSelect = { s, e -> showEpisodeSheet = false; onSwitchEpisode(s, e) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EpisodeSwitcherSheet(
    servers: List<ServerGroup>,
    currentServerIndex: Int,
    currentEpisodeIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int, Int) -> Unit,
) {
    var serverIndex by remember { mutableIntStateOf(currentServerIndex) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = YunPhimColors.Surface) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(servers) { index, server ->
                    val isSelected = index == serverIndex
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) YunPhimColors.Accent else YunPhimColors.SurfaceVariant,
                        modifier = Modifier.clickable { serverIndex = index },
                    ) {
                        Text(
                            text = server.displayName?.takeIf { it.isNotBlank() } ?: "Server ${index + 1}",
                            color = if (isSelected) Color.White else YunPhimColors.TextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                servers.getOrNull(serverIndex)?.episodes.orEmpty().forEachIndexed { index, ep ->
                    val isCurrent = serverIndex == currentServerIndex && index == currentEpisodeIndex
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCurrent) YunPhimColors.Accent else YunPhimColors.SurfaceVariant,
                        modifier = Modifier.clickable { onSelect(serverIndex, index) },
                    ) {
                        Text(
                            text = ep.displayName,
                            color = if (isCurrent) Color.White else YunPhimColors.TextPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun formatMillis(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

private fun hideSystemBars(activity: Activity?) {
    val window = activity?.window ?: return
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val controller = WindowInsetsControllerCompat(window, window.decorView)
    controller.hide(WindowInsetsCompat.Type.systemBars())
    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}

private fun showSystemBars(activity: Activity?) {
    val window = activity?.window ?: return
    WindowCompat.setDecorFitsSystemWindows(window, true)
    val controller = WindowInsetsControllerCompat(window, window.decorView)
    controller.show(WindowInsetsCompat.Type.systemBars())
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
