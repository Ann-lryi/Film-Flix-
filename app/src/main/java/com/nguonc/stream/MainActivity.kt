package com.nguonc.stream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nguonc.stream.data.repository.NowPlayingState
import com.nguonc.stream.ui.navigation.NguonCNavHost
import com.nguonc.stream.ui.navigation.Routes
import com.nguonc.stream.ui.theme.Aurora
import com.nguonc.stream.ui.theme.BrandCherry
import com.nguonc.stream.ui.theme.NguonCTheme
import com.nguonc.stream.ui.theme.OLED7
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NguonCTheme {
                NguonCStreamApp()
            }
        }
    }
}

private data class TopDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector,
)

private val topDestinations = listOf(
    TopDestination(Routes.HOME, "Trang chủ", Icons.Outlined.Home, Icons.Filled.Home),
    TopDestination(Routes.SEARCH, "Khám phá", Icons.Outlined.Search, Icons.Filled.Search),
    TopDestination(Routes.BROWSE, "Danh mục", Icons.Outlined.Widgets, Icons.Filled.Widgets),
    TopDestination(Routes.LIBRARY, "Của tôi", Icons.Outlined.Bookmarks, Icons.Filled.Bookmarks),
)

@Composable
fun NguonCStreamApp(
    nowPlayingVm: NowPlayingViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in topDestinations.map { it.route }
    val nowPlaying by nowPlayingVm.nowPlaying.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NguonCNavHost(navController = navController)

        AnimatedVisibility(
            visible = showBottomBar,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AnimatedVisibility(
                    visible = nowPlaying != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    nowPlaying?.let { state ->
                        MiniPlayer(
                            title = state.title,
                            episode = state.episode,
                            progress = state.progress,
                            isPlaying = state.isPlaying,
                            onPlayPause = nowPlayingVm::togglePlay,
                            onTap = {
                                navController.navigate(Routes.player(state.slug, state.episodeSlug))
                            },
                        )
                    }
                }
                AuroraBottomBar(
                    currentRoute = currentRoute,
                    onNav = { dest ->
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

// ======================================================
// BOTTOM BAR 3.0 — Aurora Capsule
// ======================================================
@Composable
private fun AuroraBottomBar(
    currentRoute: String?,
    onNav: (TopDestination) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(30.dp),
        color = OLED7.copy(alpha = 0.92f),
        shadowElevation = 24.dp,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color.Black.copy(alpha = 0.7f)
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.16f),
                        Color.White.copy(alpha = 0.04f),
                        Color.White.copy(alpha = 0.10f)
                    )
                ),
                RoundedCornerShape(30.dp)
            )
    ) {
        Column {
            // Top hairline highlight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                topDestinations.forEach { dest ->
                    val selected = currentRoute == dest.route
                    AuroraNavItem(
                        dest = dest,
                        selected = selected,
                        onClick = { onNav(dest) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuroraNavItem(
    dest: TopDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val iconScale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else if (selected) 1.08f else 1f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "navIconScale"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "navBg"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 30.dp)
                .graphicsLayer { alpha = bgAlpha }
                .clip(RoundedCornerShape(100.dp))
                .background(Aurora.BrandLinear),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) dest.iconSelected else dest.icon,
                contentDescription = dest.label,
                tint = if (selected) Color.White
                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                modifier = Modifier
                    .size(20.dp)
                    .scale(iconScale)
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = dest.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.2.sp
            ),
            color = if (selected) MaterialTheme.colorScheme.onSurface
                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

// ======================================================
// MINI PLAYER — Floating glass
// ======================================================
@Composable
private fun MiniPlayer(
    title: String,
    episode: String,
    progress: Float,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onTap: () -> Unit,
) {
    val breath = rememberBreath(minScale = 0.96f, maxScale = 1.04f, durationMs = 1400)
    val playScale by animateFloatAsState(
        targetValue = if (isPlaying) breath else 1f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "miniPlay"
    )
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = OLED7.copy(alpha = 0.95f),
        shadowElevation = 18.dp,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.6f)
            )
            .clickable(onClick = onTap)
            .border(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.14f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                RoundedCornerShape(22.dp)
            )
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandCherry.copy(alpha = 0.18f),
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(8.dp, RoundedCornerShape(10.dp), ambientColor = BrandCherry.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = BrandCherry,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    Text(
                        text = episode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier
                        .size(40.dp)
                        .scale(playScale)
                        .clickable(onClick = onPlayPause)
                        .background(Aurora.BrandLinear)
                        .shadow(8.dp, CircleShape, ambientColor = BrandCherry.copy(alpha = 0.6f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(2.dp)
                        .background(Aurora.BrandLinear)
                )
            }
        }
    }
}

@Composable
private fun rememberBreath(minScale: Float, maxScale: Float, durationMs: Int): Float {
    val transition = rememberInfiniteTransition(label = "breath")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathProg"
    )
    return minScale + (maxScale - minScale) * progress
}
