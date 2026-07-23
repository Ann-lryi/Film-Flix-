package com.nguonc.stream.ui.anime

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nguonc.stream.data.remote.AnimeVietsubApi
import com.nguonc.stream.debug.AppLogger
import com.nguonc.stream.debug.LogTags
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.FilmFlixIcons
import com.nguonc.stream.ui.theme.Primary

// ======================================================
// ANIME DETAIL SCREEN
// ======================================================

@Composable
fun AnimeDetailScreen(
    slug: String,
    onBack: () -> Unit,
    onPlay: (String) -> Unit,
    viewModel: AnimeDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(slug) { viewModel.load(slug) }

    BackHandler { onBack() }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(40.dp))
            }
            state.error != null -> Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Lỗi: ${state.error}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Surface(shape = AppShapes.Small, color = Primary, modifier = Modifier.clickable { viewModel.load(slug) }) {
                    Text("Thử lại", color = Color.White, modifier = Modifier.padding(16.dp, 8.dp))
                }
            }
            state.detail != null -> AnimeDetailContent(
                detail = state.detail!!,
                selectedEpisodeUrl = state.selectedEpisodeUrl,
                playerIframeUrl = state.playerIframeUrl,
                isPlayerLoading = state.isPlayerLoading,
                onBack = onBack,
                onEpisodeClick = { ep ->
                    viewModel.selectEpisode(ep.url)
                },
            )
        }
    }
}

@Composable
private fun AnimeDetailContent(
    detail: AnimeVietsubApi.AVSDetail,
    selectedEpisodeUrl: String?,
    playerIframeUrl: String?,
    isPlayerLoading: Boolean,
    onBack: () -> Unit,
    onEpisodeClick: (AnimeVietsubApi.AVSEpisode) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header with back button + poster
        item {
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                // Backdrop
                AsyncImage(
                    model = detail.posterUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent, MaterialTheme.colorScheme.background)
                            )
                        )
                )
                // Back button
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBack)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(FilmFlixIcons.ChevronLeft, "Quay lại", tint = Color.White)
                    }
                }
                // Title at bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        detail.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (detail.year.isNotBlank()) {
                        Text(
                            detail.year,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }

        // Synopsis
        if (detail.synopsis.isNotBlank()) {
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Giới thiệu", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(8.dp))
                    Text(detail.synopsis, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Genres
        if (detail.genres.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Thể loại", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(8.dp))
                    Text(detail.genres.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Player
        if (playerIframeUrl != null && playerIframeUrl.isNotBlank()) {
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color.Black)
                ) {
                    AnimeWebViewPlayer(iframeUrl = playerIframeUrl)
                }
            }
        } else if (isPlayerLoading) {
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp))
                }
            }
        }

        // Episode list
        if (detail.episodes.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Danh sách tập (${detail.episodes.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            items(detail.episodes.chunked(4)) { rowEps ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                ) {
                    rowEps.forEach { ep ->
                        val isSelected = ep.url == selectedEpisodeUrl
                        Surface(
                            shape = AppShapes.Small,
                            color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(AppShapes.Small)
                                .clickable { onEpisodeClick(ep) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    ep.number,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    repeat(4 - rowEps.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        item { Spacer(Modifier.height(120.dp)) }
    }
}

// ======================================================
// ANIME WEBVIEW PLAYER
// ======================================================

@Composable
fun AnimeWebViewPlayer(
    iframeUrl: String,
) {
    var loadedUrl by remember { mutableStateOf("") }

    DisposableEffect(iframeUrl) {
        onDispose { }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                setBackgroundColor(android.graphics.Color.BLACK)
                if (iframeUrl.isNotBlank()) {
                    loadUrl(iframeUrl)
                    loadedUrl = iframeUrl
                }
            }
        },
        update = { webview ->
            if (iframeUrl.isNotBlank() && iframeUrl != loadedUrl) {
                webview.loadUrl(iframeUrl)
                loadedUrl = iframeUrl
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}
