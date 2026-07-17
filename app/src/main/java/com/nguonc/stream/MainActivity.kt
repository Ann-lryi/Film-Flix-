package com.nguonc.stream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nguonc.stream.ui.navigation.NguonCNavHost
import com.nguonc.stream.ui.navigation.Routes
import com.nguonc.stream.ui.theme.NguonCTheme
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
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val topDestinations = listOf(
    TopDestination(Routes.HOME, "Trang chủ", Icons.Filled.Home),
    TopDestination(Routes.SEARCH, "Tìm kiếm", Icons.Filled.Search),
    TopDestination(Routes.BROWSE, "Khám phá", Icons.Filled.Widgets),
    TopDestination(Routes.LIBRARY, "Thư viện", Icons.Filled.Bookmarks),
)

@Composable
fun NguonCStreamApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    // Chỉ hiện bottom bar ở 4 màn gốc; detail/player chiếm toàn màn hình
    val showBottomBar = currentRoute in topDestinations.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topDestinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            // Màn detail/player tự xử lý insets để tràn viền toàn màn hình
            modifier = if (showBottomBar) Modifier.padding(innerPadding) else Modifier
        ) {
            NguonCNavHost(navController = navController)
        }
    }
}
