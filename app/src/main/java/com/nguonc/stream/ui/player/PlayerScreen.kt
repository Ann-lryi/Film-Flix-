package com.nguonc.stream.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.theme.FilmFlixIcons
import com.nguonc.stream.ui.components.LoadingBox
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.PremiumTextStyles
import com.nguonc.stream.ui.theme.Primary
import com.nguonc.stream.ui.theme.glowShadow

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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            state.isLoading -> LoadingBox()
            state.error != null -> ErrorBox(message = state.error!!, onRetry = viewModel::load)
            else -> {
                val player = remember { viewModel.player }
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            useController = true
                            keepScreenOn = true
                            setShowNextButton(false)
                            setShowPreviousButton(false)
                            this.player = player
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                // Top scrim + header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.88f), Color.Transparent)
                            )
                        )
                        .align(Alignment.TopCenter)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .align(Alignment.TopStart),
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.58f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                FilmFlixIcons.ChevronLeft,
                                contentDescription = "Quay lại",
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = state.movieName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
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
                                        glowRadius = 8.dp,
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
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                if (state.episodes.size > 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.94f))
                                )
                            )
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                    ) {
                        items(state.episodes, key = { it.slug }) { episode ->
                            val selected = episode.slug == state.currentEpisodeSlug
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.switchEpisode(episode) },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (selected) Icon(
                                            FilmFlixIcons.PlayFilled, null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Text(
                                            episode.name,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
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
}
