package com.nguonc.stream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nguonc.stream.ui.theme.FilmFlixIcons
import com.nguonc.stream.ui.navigation.NguonCNavHost
import com.nguonc.stream.ui.navigation.Routes
import com.nguonc.stream.ui.theme.AppGradients
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.Elevation
import com.nguonc.stream.ui.theme.Motion
import com.nguonc.stream.ui.theme.NguonCTheme
import com.nguonc.stream.ui.theme.Primary
import com.nguonc.stream.ui.theme.glowShadow
import com.nguonc.stream.ui.theme.premiumShadow
import com.nguonc.stream.ui.theme.topHighlight
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
    val iconSelected: ImageVector
)

private val topDestinations = listOf(
    TopDestination(Routes.HOME, "Trang chủ", FilmFlixIcons.HomeOutline, FilmFlixIcons.HomeOutline),
    TopDestination(Routes.SEARCH, "Tìm kiếm", FilmFlixIcons.SearchOutline, FilmFlixIcons.SearchOutline),
    TopDestination(Routes.BROWSE, "Khám phá", FilmFlixIcons.CompassOutline, FilmFlixIcons.CompassOutline),
    TopDestination(Routes.LIBRARY, "Của tôi", FilmFlixIcons.BookmarkOutline, FilmFlixIcons.BookmarkOutline),
)

@Composable
fun NguonCStreamApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in topDestinations.map { it.route }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Main NavHost
        NguonCNavHost(navController = navController)

        // Floating bottom bar — premium 3.0 glass with glow
        AnimatedVisibility(
            visible = showBottomBar,
            enter = fadeIn(Motion.emphasized(Motion.DurationL)),
            exit = fadeOut(Motion.standard(Motion.DurationS)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Surface(
                    shape = AppShapes.XXLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.92f),
                    shadowElevation = Elevation.XXL,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .premiumShadow(
                            elevation = Elevation.XXL,
                            shape = AppShapes.XXLarge,
                            ambientAlpha = 0.62f,
                            spotAlpha = 0.48f
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.10f), AppShapes.XXLarge)
                ) {
                    // Inner top highlight — "screen sheen"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.16f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        topDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            PremiumBottomItem(
                                dest = dest,
                                selected = selected,
                                onClick = {
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
        }
    }
}

@Composable
private fun PremiumBottomItem(dest: TopDestination, selected: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> Motion.PressScale
            selected -> Motion.SelectedScale
            else -> 1f
        },
        animationSpec = Motion.PressSpring,
        label = "bottomItemScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(AppShapes.Medium)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(AppShapes.Small)
                .background(
                    if (selected) AppGradients.PrimaryHorizontal
                    else Color.White.copy(alpha = 0.06f)
                )
                .border(
                    0.5.dp,
                    if (selected) Color.White.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.06f),
                    AppShapes.Small
                )
                .then(
                    if (selected) Modifier.glowShadow(
                        color = Primary,
                        shape = AppShapes.Small,
                        glowRadius = 12.dp,
                        elevation = Elevation.XS
                    ) else Modifier
                )
                .topHighlight(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = dest.icon,
                contentDescription = dest.label,
                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        // Tiny dot indicator — animated in/out for selected
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(Motion.standard(Motion.DurationS)),
            exit = fadeOut(Motion.standard(Motion.DurationS))
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Primary)
            )
        }
        if (!selected) {
            Box(modifier = Modifier.padding(top = 5.dp).size(4.dp))
        }

        Text(
            text = dest.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.2.sp
            ),
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
