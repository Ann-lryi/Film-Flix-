package com.nguonc.stream.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.nguonc.stream.data.remote.dto.EpisodeDto
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.components.LoadingBox
import com.nguonc.stream.ui.theme.AccentCyan
import com.nguonc.stream.ui.theme.AccentViolet
import com.nguonc.stream.ui.theme.AppGradients
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.Elevation
import com.nguonc.stream.ui.theme.FilmFlixIcons
import com.nguonc.stream.ui.theme.Motion
import com.nguonc.stream.ui.theme.PlayerGlassBorder
import com.nguonc.stream.ui.theme.PlayerGlassSurface
import com.nguonc.stream.ui.theme.PlayerIconActive
import com.nguonc.stream.ui.theme.PlayerIconIdle
import com.nguonc.stream.ui.theme.PlayerScrimBottom
import com.nguonc.stream.ui.theme.PlayerScrimTop
import com.nguonc.stream.ui.theme.PlayerSeekBarBuffered
import com.nguonc.stream.ui.theme.PlayerSeekBarTrack
import com.nguonc.stream.ui.theme.PlayerTimeText
import com.nguonc.stream.ui.theme.PremiumTextStyles
import com.nguonc.stream.ui.theme.Primary
import com.nguonc.stream.ui.theme.glowShadow
import com.nguonc.stream.ui.theme.serverAccentColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    slug: String,
    requestedEpisodeSlug: String?,
    requestedServerIndex: Int,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Bottom sheets state
    var showEpisodeSheet by remember { mutableStateOf(false) }
    var showServerSheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }

    // Toast-y transient hint
    var seekHintMs by remember { mutableStateOf<Long?>(null) }

    // ----- Immersive mode + landscape lock -----
    DisposableEffect(view) {
        val activity = context as? Activity
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousOrientation =
            activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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

    // ----- Back press handling -----
    // Single handler: dismiss sheets → unlock → exit
    BackHandler {
        when {
            showEpisodeSheet -> showEpisodeSheet = false
            showServerSheet -> showServerSheet = false
            showSpeedSheet -> showSpeedSheet = false
            state.isLocked -> viewModel.toggleLock()
            else -> onBack()
        }
    }

    // Auto-hide controls after 4s of inactivity when playing
    LaunchedEffect(state.controlsVisible, state.isPlaying, state.isLocked) {
        if (state.controlsVisible && state.isPlaying && !state.isLocked) {
            delay(4_000)
            viewModel.setControlsVisible(false)
        }
    }

    // Clear seek hint
    LaunchedEffect(seekHintMs) {
        if (seekHintMs != null) {
            delay(800)
            seekHintMs = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            state.isLoading -> PlayerLoadingState(state)
            state.error != null -> PlayerErrorState(
                state = state,
                onRetry = viewModel::load,
                onBack = onBack,
            )
            else -> {
                // Chọn player: WebView (cho embed URL từ NguoncApi) hoặc ExoPlayer (cho m3u8)
                if (state.useWebView) {
                    EmbedWebViewPlayer(
                        embedUrl = state.currentPlayUrl,
                        isLocked = state.isLocked,
                        onToggleControls = viewModel::toggleControls,
                    )
                } else {
                    val player = remember { viewModel.player }
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false // we draw our own controls
                                keepScreenOn = true
                                setShowNextButton(false)
                                setShowPreviousButton(false)
                                this.player = player
                                // Hide buffer spinner, we draw our own
                                setKeepContentOnPlayerReset(true)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(state.isLocked) {
                                if (state.isLocked) return@pointerInput
                                detectTapGestures(
                                    onTap = {
                                        viewModel.toggleControls()
                                    },
                                    onDoubleTap = { offset ->
                                        val width = size.width.toFloat()
                                        if (offset.x < width / 3) {
                                            viewModel.seekRelative(-10_000)
                                            seekHintMs = -10_000
                                        } else if (offset.x > width * 2 / 3) {
                                            viewModel.seekRelative(10_000)
                                            seekHintMs = 10_000
                                        } else {
                                            viewModel.togglePlayPause()
                                        }
                                    },
                                )
                            },
                    )
                }

                // Poster placeholder while buffering at the very start
                if (state.isBuffering && state.positionMs <= 0L && state.posterUrl.isNotBlank()) {
                    AsyncImage(
                        model = state.posterUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.42f))
                    )
                }

                // ---------- Custom Controls Overlay ----------
                AnimatedVisibility(
                    visible = state.controlsVisible && !state.isLocked,
                    enter = fadeIn(Motion.standard(Motion.DurationM)),
                    exit = fadeOut(Motion.standard(Motion.DurationS)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    PlayerControlsOverlay(
                        state = state,
                        seekHintMs = seekHintMs,
                        onBack = onBack,
                        onPlayPause = viewModel::togglePlayPause,
                        onSeek = viewModel::seekTo,
                        onSeekRelative = { delta ->
                            viewModel.seekRelative(delta)
                            seekHintMs = delta
                        },
                        onSkipNextEpisode = state.nextEpisode?.let { next ->
                            { viewModel.switchEpisode(next) }
                        },
                        onShowEpisodes = { showEpisodeSheet = true },
                        onShowServers = { showServerSheet = true },
                        onShowSpeed = { showSpeedSheet = true },
                        onToggleLock = viewModel::toggleLock,
                    )
                }

                // ---------- Lock FAB when locked ----------
                AnimatedVisibility(
                    visible = state.isLocked,
                    enter = fadeIn(Motion.standard(Motion.DurationM)),
                    exit = fadeOut(Motion.standard(Motion.DurationS)),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    LockedHintBadge(onUnlock = viewModel::toggleLock)
                }
            }
        }
    }

    // ---------- Bottom sheets ----------
    if (showEpisodeSheet) {
        EpisodeBottomSheet(
            state = state,
            onDismiss = { showEpisodeSheet = false },
            onSelectEpisode = { ep ->
                viewModel.switchEpisode(ep)
                showEpisodeSheet = false
            },
        )
    }
    if (showServerSheet) {
        ServerBottomSheet(
            state = state,
            onDismiss = { showServerSheet = false },
            onSelectServer = { idx ->
                viewModel.switchServer(idx)
                showServerSheet = false
            },
        )
    }
    if (showSpeedSheet) {
        SpeedBottomSheet(
            currentSpeed = state.playbackSpeed,
            onDismiss = { showSpeedSheet = false },
            onSelectSpeed = { speed ->
                viewModel.setPlaybackSpeed(speed)
                showSpeedSheet = false
            },
        )
    }
}

// ======================================================
// LOADING & ERROR STATES
// ======================================================

@Composable
private fun PlayerLoadingState(state: PlayerUiState) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (state.posterUrl.isNotBlank()) {
            AsyncImage(
                model = state.posterUrl,
                contentDescription = state.movieName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val transition = rememberInfiniteTransition(label = "playerLoading")
            val alpha by transition.animateFloat(
                initialValue = 0.55f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "playerLoadingAlpha"
            )
            androidx.compose.material3.CircularProgressIndicator(
                color = Primary.copy(alpha = alpha),
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Đang tải phim...",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PlayerErrorState(
    state: PlayerUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (state.posterUrl.isNotBlank()) {
            AsyncImage(
                model = state.posterUrl,
                contentDescription = state.movieName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)))
        }
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Primary.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.32f)),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        FilmFlixIcons.CloudOffOutline,
                        "Lỗi",
                        tint = Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Text(
                text = "Không phát được",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = state.error ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PlayerGlassButton(onClick = onBack, label = "Thoát") {
                    Icon(
                        FilmFlixIcons.ChevronLeft, "Thoát",
                        tint = Color.White, modifier = Modifier.size(18.dp)
                    )
                }
                PlayerGlassButton(
                    onClick = onRetry,
                    label = "Thử lại",
                    accent = true
                ) {
                    Icon(
                        FilmFlixIcons.BoltFilled, "Thử lại",
                        tint = Color.White, modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ======================================================
// CONTROLS OVERLAY — Top bar + Center + Bottom bar + Seekbar
// ======================================================

@Composable
private fun PlayerControlsOverlay(
    state: PlayerUiState,
    seekHintMs: Long?,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekRelative: (Long) -> Unit,
    onSkipNextEpisode: (() -> Unit)?,
    onShowEpisodes: () -> Unit,
    onShowServers: () -> Unit,
    onShowSpeed: () -> Unit,
    onToggleLock: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // ---------- Top scrim + header ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(listOf(PlayerScrimTop, Color.Transparent))
                )
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .align(Alignment.TopStart)
        ) {
            PlayerGlassIconButton(onClick = onBack, size = 42.dp) {
                Icon(FilmFlixIcons.ChevronLeft, "Quay lại", tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.movieName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (state.currentEpisodeName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Primary,
                            shape = AppShapes.XSmall,
                            modifier = Modifier.glowShadow(
                                color = Primary,
                                shape = AppShapes.XSmall,
                                glowRadius = 6.dp,
                                elevation = 0.dp
                            )
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
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))

            // Server badge — opens server sheet
            if (state.hasMultipleServers) {
                val accent = serverAccentColor(state.currentServer?.name ?: "")
                PlayerGlassIconButton(
                    onClick = onShowServers,
                    size = 42.dp,
                    border = accent.copy(alpha = 0.55f),
                ) {
                    Icon(
                        FilmFlixIcons.ServerOutline,
                        "Đổi server",
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Lock button
            PlayerGlassIconButton(onClick = onToggleLock, size = 42.dp) {
                Icon(FilmFlixIcons.LockFilled, "Khoá", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // ---------- Center: play/pause + skip ±10s (chỉ ExoPlayer mode) ----------
        if (!state.useWebView) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(48.dp)
                ) {
                    // Skip back 10s
                    PlayerCenterIconButton(
                        icon = FilmFlixIcons.SkipBackward10,
                        contentDescription = "Lùi 10s",
                        size = 56.dp,
                        iconSize = 30.dp,
                        onClick = { onSeekRelative(-10_000) }
                    )

                    // Main play/pause
                    MainPlayPauseButton(
                        isPlaying = state.isPlaying,
                        isBuffering = state.isBuffering,
                        onClick = onPlayPause,
                    )

                    // Skip forward 10s
                    PlayerCenterIconButton(
                        icon = FilmFlixIcons.SkipForward10,
                        contentDescription = "Tiến 10s",
                        size = 56.dp,
                        iconSize = 30.dp,
                        onClick = { onSeekRelative(10_000) }
                    )
                }

                // Seek hint (transient "±10s" overlay)
                AnimatedVisibility(
                    visible = seekHintMs != null,
                    enter = fadeIn() + scaleIn(initialScale = 0.85f),
                    exit = fadeOut() + scaleOut(targetScale = 0.85f),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    SeekHintBubble(seekHintMs)
                }

                // Buffering spinner overlay
                if (state.isBuffering) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 110.dp) // below the play row
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        } else {
            // WebView mode: hint người dùng điều khiển play/pause/seek trong iframe
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (state.isBuffering) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // ---------- Bottom scrim + controls ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (state.useWebView) 130.dp else 200.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, PlayerScrimBottom))
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Seek bar — chỉ hiện ở ExoPlayer mode (WebView không control được)
            if (!state.useWebView) {
                PlayerSeekBar(
                    positionMs = state.positionMs,
                    durationMs = state.durationMs,
                    bufferedMs = state.bufferedMs,
                    onSeek = onSeek,
                )
            }

            // Bottom action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: speed (chỉ ExoPlayer)
                if (!state.useWebView) {
                    PlayerChipButton(
                        onClick = onShowSpeed,
                        icon = FilmFlixIcons.GaugeOutline,
                        label = "${state.playbackSpeed}x".replace(".0x", "x"),
                    )
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                // Center: episodes
                if (state.hasMultipleEpisodes) {
                    PlayerChipButton(
                        onClick = onShowEpisodes,
                        icon = FilmFlixIcons.ListOutline,
                        label = "Tập phim",
                        accent = true,
                    )
                }

                // Right: skip next
                if (onSkipNextEpisode != null) {
                    PlayerChipButton(
                        onClick = onSkipNextEpisode,
                        icon = FilmFlixIcons.SkipNext,
                        label = "Tập kế",
                    )
                } else if (state.useWebView) {
                    Spacer(Modifier.width(1.dp))
                }
            }
        }
    }
}

// ======================================================
// PLAYER SUB-COMPONENTS
// ======================================================

@Composable
private fun PlayerGlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 44.dp,
    border: Color = PlayerGlassBorder,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "glassIconScale"
    )
    Surface(
        color = PlayerGlassSurface,
        shape = CircleShape,
        border = BorderStroke(1.dp, border),
        modifier = modifier
            .size(size)
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
private fun PlayerCenterIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "centerIconScale"
    )
    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .pointerInput(onClick) {
                detectTapGestures(onTap = { onClick() })
            }
            .graphicsLayer { alpha = 0.92f },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.0f))
        )
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun MainPlayPauseButton(
    isPlaying: Boolean,
    isBuffering: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "playPauseScale"
    )
    val transition = rememberInfiniteTransition(label = "playGlow")
    val glowScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "playGlowScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(88.dp)
            .scale(scale)
            .pointerInput(onClick) {
                detectTapGestures(onTap = { onClick() })
            }
    ) {
        // Ambient glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.alpha = 0.6f * glowScale
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.45f),
                            Primary.copy(alpha = 0.0f)
                        )
                    )
                )
        )
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = Elevation.XXL,
            modifier = Modifier
                .size(72.dp)
                .glowShadow(
                    color = Primary,
                    shape = CircleShape,
                    glowRadius = 22.dp,
                    elevation = Elevation.L
                )
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isBuffering) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) FilmFlixIcons.PauseFilled else FilmFlixIcons.PlayFilled,
                        contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerChipButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accent: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "chipBtnScale"
    )
    Surface(
        shape = AppShapes.Pill,
        color = if (accent) Primary.copy(alpha = 0.95f) else PlayerGlassSurface,
        border = BorderStroke(
            1.dp,
            if (accent) Primary else PlayerGlassBorder
        ),
        modifier = Modifier
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

@Composable
private fun PlayerGlassButton(
    onClick: () -> Unit,
    label: String,
    accent: Boolean = false,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "glassBtnScale"
    )
    Surface(
        shape = AppShapes.Medium,
        color = if (accent) Primary else PlayerGlassSurface,
        border = BorderStroke(
            1.dp,
            if (accent) Primary else PlayerGlassBorder
        ),
        modifier = Modifier
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            content()
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

@Composable
private fun PlayerSeekBar(
    positionMs: Long,
    durationMs: Long,
    bufferedMs: Long,
    onSeek: (Long) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val duration = if (durationMs > 0) durationMs else 0L
    val safeProgress = if (duration > 0) (positionMs.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val safeBuffered = if (duration > 0) (bufferedMs.toFloat() / duration).coerceIn(0f, 1f) else 0f

    val dragProgress = remember { Animatable(safeProgress) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(safeProgress) {
        if (!isDragging) {
            dragProgress.snapTo(safeProgress)
        }
    }

    val barHeight: androidx.compose.ui.unit.Dp = if (isDragging) 6.dp else 4.dp
    val thumbSize: androidx.compose.ui.unit.Dp = if (isDragging) 18.dp else 14.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .pointerInput(duration) {
                    if (duration <= 0) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            val target = (dragProgress.value * duration).toLong()
                            onSeek(target)
                        },
                        onDragCancel = {
                            isDragging = false
                            coroutineScope.launch { dragProgress.snapTo(safeProgress) }
                        },
                    ) { change, delta ->
                        change.consume()
                        val widthPx = size.width.toFloat().coerceAtLeast(1f)
                        val newProgress = (dragProgress.value + delta / widthPx).coerceIn(0f, 1f)
                        coroutineScope.launch { dragProgress.snapTo(newProgress) }
                    }
                }
                .pointerInput(duration) {
                    if (duration <= 0) return@pointerInput
                    detectTapGestures { offset ->
                        val widthPx = size.width.toFloat().coerceAtLeast(1f)
                        val target = ((offset.x / widthPx) * duration).toLong()
                        onSeek(target)
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Track — full width
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(CircleShape)
                    .background(PlayerSeekBarTrack)
            )
            // Buffered layer
            Box(
                modifier = Modifier
                    .fillMaxWidth(safeBuffered)
                    .height(barHeight)
                    .clip(CircleShape)
                    .background(PlayerSeekBarBuffered)
            )
            // Progress layer — gradient, ends exactly at thumb position
            Box(
                modifier = Modifier
                    .fillMaxWidth(dragProgress.value)
                    .height(barHeight)
                    .clip(CircleShape)
                    .background(AppGradients.PrimaryHorizontal)
            )
            // Thumb — aligned to the right edge of progress via fillMaxWidth fraction
            Box(
                modifier = Modifier
                    .fillMaxWidth(dragProgress.value)
                    .height(28.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(thumbSize)
                        .clip(CircleShape)
                        .background(Color.White)
                        .glowShadow(
                            color = Primary,
                            shape = CircleShape,
                            glowRadius = 8.dp,
                            elevation = 0.dp
                        )
                )
            }
        }

        // Time row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(positionMs),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PlayerTimeText
            )
            Text(
                text = formatTime(durationMs),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PlayerTimeText.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun SeekHintBubble(seekHintMs: Long?) {
    val text = when {
        seekHintMs == null -> ""
        seekHintMs > 0 -> "+${seekHintMs / 1000}s"
        else -> "${seekHintMs / 1000}s"
    }
    Surface(
        shape = AppShapes.Large,
        color = Color.Black.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, Primary.copy(alpha = 0.45f)),
        modifier = Modifier.glowShadow(
            color = Primary,
            shape = AppShapes.Large,
            glowRadius = 16.dp,
            elevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = Color.White
            )
            Text(
                text = "double-tap seek",
                style = PremiumTextStyles.Eyebrow,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun LockedHintBadge(onUnlock: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = PlayerGlassSurface,
            border = BorderStroke(1.dp, PlayerGlassBorder),
            modifier = Modifier
                .size(64.dp)
                .clickable(onClick = onUnlock)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    FilmFlixIcons.LockFilled,
                    "Khoá",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = "Màn hình đã khoá\nChạm để mở khoá",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

// ======================================================
// BOTTOM SHEETS — Episodes / Servers / Speed
// ======================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpisodeBottomSheet(
    state: PlayerUiState,
    onDismiss: () -> Unit,
    onSelectEpisode: (EpisodeDto) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = AppShapes.XXLarge,
        dragHandle = { SheetDragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Danh sách tập",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val epCount = state.currentServer?.episodes?.size ?: 0
                    Text(
                        text = "$epCount tập • ${state.currentServer?.name ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (state.currentServer != null) {
                    Surface(
                        shape = AppShapes.Pill,
                        color = Primary.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            "${state.currentServerIndex + 1}/${state.servers.size}",
                            style = PremiumTextStyles.Eyebrow,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            val episodes = state.currentServer?.episodes.orEmpty()
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                items(episodes, key = { it.slug }) { ep ->
                    val selected = ep.slug == state.currentEpisodeSlug
                    EpisodeGridItem(
                        episode = ep,
                        selected = selected,
                        onClick = { onSelectEpisode(ep) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeGridItem(
    episode: EpisodeDto,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "epItemScale"
    )
    Surface(
        shape = AppShapes.Small,
        color = if (selected) Primary else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (selected) Primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .height(46.dp)
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = episode.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold
                ),
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerBottomSheet(
    state: PlayerUiState,
    onDismiss: () -> Unit,
    onSelectServer: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = AppShapes.XXLarge,
        dragHandle = { SheetDragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Chọn nguồn phát",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Vietsub • Thuyết minh • Lồng tiếng",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(state.servers, key = { it.index }) { srv ->
                    val selected = srv.index == state.currentServerIndex
                    val accent = serverAccentColor(srv.name)
                    ServerRow(
                        server = srv,
                        selected = selected,
                        accent = accent,
                        onClick = { onSelectServer(srv.index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerRow(
    server: ServerGroup,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "serverRowScale"
    )
    Surface(
        shape = AppShapes.Large,
        color = if (selected) accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(
            1.dp,
            if (selected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(AppShapes.Medium)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    FilmFlixIcons.ServerOutline,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${server.episodes.size} tập khả dụng",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Surface(
                    shape = AppShapes.Pill,
                    color = accent,
                    modifier = Modifier.glowShadow(
                        color = accent,
                        shape = AppShapes.Pill,
                        glowRadius = 8.dp,
                        elevation = 0.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            FilmFlixIcons.CheckOutline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "ĐANG PHÁT",
                            style = PremiumTextStyles.Badge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeedBottomSheet(
    currentSpeed: Float,
    onDismiss: () -> Unit,
    onSelectSpeed: (Float) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val speeds = remember { listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = AppShapes.XXLarge,
        dragHandle = { SheetDragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Tốc độ phát",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Hiện tại: ${formatSpeed(currentSpeed)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                speeds.forEach { speed ->
                    val selected = kotlin.math.abs(speed - currentSpeed) < 0.01f
                    val interaction = remember { MutableInteractionSource() }
                    val isPressed by interaction.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) Motion.PressScale else 1f,
                        animationSpec = Motion.PressSpring,
                        label = "speedScale"
                    )
                    Surface(
                        shape = AppShapes.Medium,
                        color = if (selected) Primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (selected) Primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .scale(scale)
                            .clickable(interactionSource = interaction, indication = null) {
                                onSelectSpeed(speed)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = formatSpeed(speed),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold
                                ),
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .width(40.dp)
            .height(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    )
}

// ======================================================
// HELPERS
// ======================================================

private fun formatTime(ms: Long): String {
    if (ms < 0) return "00:00"
    val totalSeconds = ms / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.US, "%02d:%02d", m, s)
}

private fun formatSpeed(speed: Float): String {
    return if (speed == speed.toInt().toFloat()) "${speed.toInt()}.0x"
    else "${speed}x"
}

// ======================================================
// EMBED WEBVIEW PLAYER
// Dùng khi NguoncApi chỉ trả embed URL (iframe player),
// không có link_m3u8 trực tiếp.
// ======================================================

@Composable
private fun EmbedWebViewPlayer(
    embedUrl: String,
    isLocked: Boolean,
    onToggleControls: () -> Unit,
) {
    // Remember the loaded URL to avoid reload on every recomposition
    var loadedUrl by remember { mutableStateOf("") }

    AndroidView(
        factory = { ctx ->
            android.webkit.WebView(ctx).apply {
                // ---- Debug (bật logcat để debug) ----
                android.webkit.WebView.setWebContentsDebuggingEnabled(true)

                // ---- WebSettings cho video playback ----
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.mediaPlaybackRequiresUserGesture = false
                // Mixed content: cho phép load HTTP resources từ HTTPS page
                settings.mixedContentMode =
                    android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                // Proper viewport sizing cho player
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                // Cache mode
                settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                // User-Agent: Chrome mobile cho Android
                settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"

                // Hardware accelerated layer cho smooth video
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                // ---- WebViewClient: inject Referer cho tất cả requests ----
                webViewClient = object : android.webkit.WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: android.webkit.WebView?,
                        request: android.webkit.WebResourceRequest?,
                    ): Boolean = false

                    override fun shouldInterceptRequest(
                        view: android.webkit.WebView?,
                        request: android.webkit.WebResourceRequest?,
                    ): android.webkit.WebResourceResponse? {
                        val url = request?.url?.toString() ?: return null
                        // Chỉ intercept các video stream URL (m3u8/mp4/ts từ CDN),
                        // để trang HTML/JS/CSS đi qua WebView bình thường.
                        val isVideoStream = url.contains(".m3u8") ||
                            url.contains(".mp4") ||
                            url.contains(".ts") ||
                            url.contains("phim1280.tv") ||
                            url.contains("kkphimplayer")
                        if (!isVideoStream) return null
                        if (!url.startsWith("http")) return null
                        try {
                            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                            connection.requestMethod = request.method ?: "GET"
                            connection.connectTimeout = 15_000
                            connection.readTimeout = 30_000
                            connection.instanceFollowRedirects = true
                            // Copy headers từ request gốc
                            request.requestHeaders?.forEach { (k, v) ->
                                if (!k.equals("Referer", true) && !k.equals("Host", true)) {
                                    connection.setRequestProperty(k, v)
                                }
                            }
                            // Set Referer — streamc.xyz & phim1280.tv check referer
                            connection.setRequestProperty(
                                "Referer",
                                "https://phim.nguonc.com/",
                            )
                            connection.setRequestProperty(
                                "User-Agent",
                                "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
                                    "(KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36",
                            )

                            val contentType = connection.contentType ?: "application/octet-stream"
                            val mimeType = contentType.split(";").firstOrNull()?.trim()
                                ?: "application/octet-stream"
                            val encoding = if (contentType.contains("charset=", true)) {
                                contentType.substringAfter("charset=").trim()
                            } else "UTF-8"

                            val statusCode = connection.responseCode
                            val reasonPhrase = connection.responseMessage ?: "OK"
                            val responseHeaders = mutableMapOf<String, String>()
                            for ((k, v) in connection.headerFields) {
                                if (v.isNotEmpty()) responseHeaders[k] = v.joinToString(", ")
                            }

                            val inputStream = if (statusCode in 200..299) {
                                connection.inputStream
                            } else {
                                connection.errorStream ?: return null
                            }

                            return android.webkit.WebResourceResponse(
                                mimeType,
                                encoding,
                                statusCode,
                                reasonPhrase,
                                responseHeaders,
                                inputStream,
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("EmbedWebView", "Intercept error for $url: ${e.message}")
                            return null
                        }
                    }

                    override fun onReceivedSslError(
                        view: android.webkit.WebView?,
                        handler: android.webkit.SslErrorHandler?,
                        error: android.net.SslError?,
                    ) {
                        // Bypass SSL errors cho player server (self-signed certs)
                        handler?.proceed()
                    }

                    override fun onConsoleMessage(
                        consoleMessage: android.webkit.ConsoleMessage?,
                    ): Boolean {
                        android.util.Log.d(
                            "EmbedWebView",
                            "JS: ${consoleMessage?.message()} " +
                                "(${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()})"
                        )
                        return true
                    }

                    override fun onReceivedError(
                        view: android.webkit.WebView?,
                        request: android.webkit.WebResourceRequest?,
                        error: android.webkit.WebResourceError?,
                    ) {
                        android.util.Log.e(
                            "EmbedWebView",
                            "Error loading ${request?.url}: ${error?.description}"
                        )
                        super.onReceivedError(view, request, error)
                    }
                }

                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onConsoleMessage(
                        consoleMessage: android.webkit.ConsoleMessage?,
                    ): Boolean {
                        android.util.Log.d(
                            "EmbedWebView",
                            "Chrome JS: ${consoleMessage?.message()}"
                        )
                        return true
                    }
                }

                setBackgroundColor(android.graphics.Color.BLACK)
                isHorizontalScrollBarEnabled = false
                isVerticalScrollBarEnabled = false
                // Cho phép focus để nhận key events
                isFocusable = true
                isFocusableInTouchMode = true

                // Load với Referer header — embed server (streamc.xyz) kiểm tra referer
                if (embedUrl.isNotBlank()) {
                    val headers = mapOf(
                        "Referer" to "https://phim.nguonc.com/",
                    )
                    loadUrl(embedUrl, headers)
                    loadedUrl = embedUrl
                }
            }
        },
        update = { webview ->
            // Chỉ reload khi URL thực sự thay đổi (không reload khi recompose)
            if (embedUrl.isNotBlank() && embedUrl != loadedUrl) {
                val headers = mapOf(
                    "Referer" to "https://phim.nguonc.com/",
                )
                webview.loadUrl(embedUrl, headers)
                loadedUrl = embedUrl
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isLocked) {
                if (isLocked) return@pointerInput
                detectTapGestures(
                    onTap = { onToggleControls() },
                )
            },
    )
}
