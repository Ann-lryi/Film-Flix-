package com.nguonc.stream.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.nguonc.stream.data.remote.dto.EpisodeDto
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.components.LoadingBox
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.FilmFlixIcons
import com.nguonc.stream.ui.theme.Motion
import com.nguonc.stream.ui.theme.PremiumTextStyles
import com.nguonc.stream.ui.theme.Primary
import com.nguonc.stream.ui.theme.glowShadow
import kotlinx.coroutines.delay
import java.util.Locale

/** Thời gian (ms) không thao tác trước khi tự ẩn control layer trong lúc đang phát. */
private const val AUTO_HIDE_DELAY_MS = 3_800L
private const val SEEK_STEP_MS = 10_000L

@Composable
fun PlayerScreen(
    slug: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val player = remember { viewModel.player }

    // ---- Khoá màn hình ngang + ẩn system bar khi vào Player, khôi phục khi thoát ----
    DisposableEffect(view) {
        val activity = context as? Activity
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        controller?.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            viewModel.saveCurrentProgress()
            viewModel.pause()
            controller?.show(WindowInsetsCompat.Type.systemBars())
            activity?.requestedOrientation = previousOrientation
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) viewModel.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ---- Trạng thái phát (để biết khi nào được phép tự ẩn control) ----
    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
        }
        player.addListener(listener)
        isPlaying = player.isPlaying
        onDispose { player.removeListener(listener) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            state.isLoading -> LoadingBox()
            state.error != null -> ErrorBox(message = state.error!!, onRetry = viewModel::load, modifier = Modifier.align(Alignment.Center))
            else -> PlayerContent(
                state = state,
                player = player,
                isPlaying = isPlaying,
                onBack = onBack,
                onSwitchEpisode = viewModel::switchEpisode,
                onSwitchServer = viewModel::switchServer,
                onRetryPlayback = viewModel::retryPlayback,
            )
        }
    }
}

@Composable
private fun PlayerContent(
    state: PlayerUiState,
    player: ExoPlayer,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onSwitchEpisode: (EpisodeDto) -> Unit,
    onSwitchServer: (Int) -> Unit,
    onRetryPlayback: () -> Unit,
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var isDraggingSeek by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableLongStateOf(0L) }
    var livePositionMs by remember { mutableLongStateOf(0L) }
    var liveDurationMs by remember { mutableLongStateOf(0L) }
    var seekPulseDirection by remember { mutableStateOf(0) } // -1 lùi, 0 ẩn, +1 tiến
    var seekPulseTrigger by remember { mutableStateOf(0) }

    // Vòng lặp cập nhật vị trí/thời lượng hiện tại — tách khỏi StateFlow của ViewModel để
    // không ép toàn màn hình recompose theo tần suất cao (250ms).
    LaunchedEffect(player) {
        while (true) {
            if (!isDraggingSeek) {
                livePositionMs = runCatching { player.currentPosition }.getOrDefault(0L).coerceAtLeast(0L)
                val d = runCatching { player.duration }.getOrDefault(C.TIME_UNSET)
                liveDurationMs = if (d != C.TIME_UNSET && d > 0L) d else 0L
            }
            delay(250)
        }
    }

    // Tự ẩn control sau AUTO_HIDE_DELAY_MS khi: đang hiện + đang phát + không kéo seek + không có lỗi.
    LaunchedEffect(controlsVisible, isPlaying, isDraggingSeek, state.playerError) {
        if (controlsVisible && isPlaying && !isDraggingSeek && state.playerError == null) {
            delay(AUTO_HIDE_DELAY_MS)
            controlsVisible = false
        }
    }

    LaunchedEffect(seekPulseTrigger) {
        if (seekPulseDirection != 0) {
            delay(650)
            seekPulseDirection = 0
        }
    }

    fun seekBy(deltaMs: Long) {
        val cur = runCatching { player.currentPosition }.getOrDefault(0L)
        val dur = runCatching { player.duration }.getOrDefault(C.TIME_UNSET)
        var target = (cur + deltaMs).coerceAtLeast(0L)
        if (dur != C.TIME_UNSET && dur > 0L) target = target.coerceAtMost(dur)
        player.seekTo(target)
        livePositionMs = target
        seekPulseDirection = if (deltaMs > 0) 1 else -1
        seekPulseTrigger++
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    keepScreenOn = true
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    this.player = player
                }
            },
            onRelease = { it.player = null },
            modifier = Modifier.fillMaxSize(),
        )

        // Lớp bắt cử chỉ trong suốt — đặt trên video, dưới mọi control thật, để 1 mình nó
        // quyết định ẩn/hiện, tránh việc control mặc định của ExoPlayer từng gây chồng lấn.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { controlsVisible = !controlsVisible },
                        onDoubleTap = { offset ->
                            val third = size.width / 3f
                            when {
                                offset.x < third -> seekBy(-SEEK_STEP_MS)
                                offset.x > third * 2 -> seekBy(SEEK_STEP_MS)
                                else -> if (player.isPlaying) player.pause() else player.play()
                            }
                        }
                    )
                }
        )

        if (state.isBuffering && state.playerError == null) {
            BufferingSpinner(modifier = Modifier.align(Alignment.Center))
        }

        SeekPulseIndicator(
            visible = seekPulseDirection < 0,
            mirrored = false,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 40.dp)
        )
        SeekPulseIndicator(
            visible = seekPulseDirection > 0,
            mirrored = true,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 40.dp)
        )

        if (state.playerError != null) {
            PlayerErrorOverlay(
                message = state.playerError,
                onRetry = onRetryPlayback,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(Motion.standard(Motion.DurationS)) + slideInVertically(Motion.standard(Motion.DurationS)) { -it / 2 },
            exit = fadeOut(Motion.standard(Motion.DurationS)) + slideOutVertically(Motion.standard(Motion.DurationS)) { -it / 2 },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            PlayerTopBar(state = state, onBack = onBack)
        }

        AnimatedVisibility(
            visible = controlsVisible && state.playerError == null && !state.isBuffering,
            enter = fadeIn(Motion.standard(Motion.DurationS)) + scaleIn(initialScale = 0.85f),
            exit = fadeOut(Motion.standard(Motion.DurationXS)) + scaleOut(targetScale = 0.85f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CenterPlayPauseButton(
                isPlaying = isPlaying,
                onClick = { if (player.isPlaying) player.pause() else player.play() },
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(Motion.standard(Motion.DurationS)) + slideInVertically(Motion.standard(Motion.DurationS)) { it / 2 },
            exit = fadeOut(Motion.standard(Motion.DurationS)) + slideOutVertically(Motion.standard(Motion.DurationS)) { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PlayerBottomControls(
                state = state,
                positionMs = if (isDraggingSeek) dragPositionMs else livePositionMs,
                durationMs = liveDurationMs,
                onSeekChange = { v -> isDraggingSeek = true; dragPositionMs = v },
                onSeekFinished = {
                    player.seekTo(dragPositionMs)
                    livePositionMs = dragPositionMs
                    isDraggingSeek = false
                },
                onSwitchEpisode = onSwitchEpisode,
                onSwitchServer = onSwitchServer,
            )
        }
    }
}

@Composable
private fun PlayerTopBar(state: PlayerUiState, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.82f), Color.Transparent)))
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
        Surface(
            color = Color.Black.copy(alpha = 0.58f),
            shape = CircleShape,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
            modifier = Modifier.size(44.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(FilmFlixIcons.ChevronLeft, contentDescription = "Quay lại", tint = Color.White)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.movieName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (state.currentEpisodeName.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = Primary,
                        shape = AppShapes.XSmall,
                        modifier = Modifier.glowShadow(color = Primary, shape = AppShapes.XSmall, glowRadius = 8.dp, elevation = 0.dp)
                    ) {
                        Text(
                            "ĐANG PHÁT",
                            style = PremiumTextStyles.Badge,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = state.currentEpisodeName,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (state.currentServerName.isNotBlank() && state.servers.size > 1) {
                        Surface(
                            color = Color.White.copy(alpha = 0.14f),
                            shape = AppShapes.XSmall,
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Text(
                                state.currentServerName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun CenterPlayPauseButton(isPlaying: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "centerPlayScale"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .glowShadow(color = Primary, shape = CircleShape, glowRadius = 22.dp, elevation = 8.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Icon(
            imageVector = if (isPlaying) FilmFlixIcons.PauseFilled else FilmFlixIcons.PlayFilled,
            contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
            tint = Color.Black,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun BufferingSpinner(modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.45f),
        modifier = modifier.size(56.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun SeekPulseIndicator(visible: Boolean, mirrored: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(Motion.standard(Motion.DurationXS)),
        exit = fadeOut(Motion.standard(Motion.DurationM)),
        modifier = modifier,
    ) {
        Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.55f), modifier = Modifier.size(72.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        FilmFlixIcons.ReplayOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { scaleX = if (mirrored) -1f else 1f }
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("10s", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PlayerErrorOverlay(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = AppShapes.Large,
        color = Color.Black.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        modifier = modifier.padding(32.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(FilmFlixIcons.CloudOffOutline, null, tint = Primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(240.dp)
            )
            Spacer(Modifier.height(16.dp))
            val interaction = remember { MutableInteractionSource() }
            val isPressed by interaction.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) Motion.PressScale else 1f,
                animationSpec = Motion.PressSpring,
                label = "retryScale"
            )
            Surface(
                shape = AppShapes.Small,
                color = Primary,
                modifier = Modifier
                    .scale(scale)
                    .clip(AppShapes.Small)
                    .clickable(interactionSource = interaction, indication = null, onClick = onRetry)
            ) {
                Text(
                    "Thử lại",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayerBottomControls(
    state: PlayerUiState,
    positionMs: Long,
    durationMs: Long,
    onSeekChange: (Long) -> Unit,
    onSeekFinished: () -> Unit,
    onSwitchEpisode: (EpisodeDto) -> Unit,
    onSwitchServer: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))))
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        Column {
            // ---- Chuyển server (Vietsub / Lồng Tiếng / ...) ----
            if (state.servers.size > 1) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    items(state.servers.size) { idx ->
                        val srv = state.servers[idx]
                        val selected = idx == state.currentServerIndex
                        ServerChip(
                            label = srv.serverName.ifBlank { "Server ${idx + 1}" },
                            selected = selected,
                            onClick = { onSwitchServer(idx) }
                        )
                    }
                }
            }

            // ---- Seek bar + thời gian ----
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Text(
                    text = formatDuration(positionMs),
                    style = PremiumTextStyles.CaptionMono,
                    color = Color.White,
                )
                Slider(
                    value = positionMs.toFloat().coerceIn(0f, durationMs.toFloat().coerceAtLeast(1f)),
                    onValueChange = { onSeekChange(it.toLong()) },
                    onValueChangeFinished = onSeekFinished,
                    valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                    enabled = durationMs > 0L,
                    colors = SliderDefaults.colors(
                        thumbColor = Primary,
                        activeTrackColor = Primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.28f),
                        disabledThumbColor = Color.White.copy(alpha = 0.4f),
                        disabledActiveTrackColor = Color.White.copy(alpha = 0.25f),
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp)
                )
                Text(
                    text = formatDuration(durationMs),
                    style = PremiumTextStyles.CaptionMono,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }

            // ---- Danh sách tập (server hiện tại) ----
            if (state.currentEpisodes.size > 1) {
                Spacer(Modifier.height(6.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.currentEpisodes, key = { it.slug }) { episode ->
                        val selected = episode.slug == state.currentEpisodeSlug
                        FilterChip(
                            selected = selected,
                            onClick = { onSwitchEpisode(episode) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (selected) Icon(FilmFlixIcons.PlayFilled, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Text(episode.name, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                                }
                            },
                            shape = AppShapes.Small,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Black.copy(alpha = 0.55f),
                                labelColor = Color.White,
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color.White.copy(alpha = 0.25f),
                                selectedBorderColor = Primary,
                                enabled = true,
                                selected = selected
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "serverChipScale"
    )
    Surface(
        shape = AppShapes.Pill,
        color = if (selected) Primary else Color.Black.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, if (selected) Primary else Color.White.copy(alpha = 0.25f)),
        modifier = Modifier
            .scale(scale)
            .clip(AppShapes.Pill)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(FilmFlixIcons.LanguageOutline, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSeconds = ms / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.US, "%02d:%02d", m, s)
}
