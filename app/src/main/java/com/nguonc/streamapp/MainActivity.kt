package com.nguonc.streamapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nguonc.streamapp.ui.components.BottomNavBar
import com.nguonc.streamapp.ui.screens.DetailScreen
import com.nguonc.streamapp.ui.screens.FavoritesScreen
import com.nguonc.streamapp.ui.screens.HomeScreen
import com.nguonc.streamapp.ui.screens.PlayerScreen
import com.nguonc.streamapp.ui.screens.SearchScreen
import com.nguonc.streamapp.ui.screens.SettingsScreen
import com.nguonc.streamapp.ui.theme.NguonCStreamAppTheme
import com.nguonc.streamapp.ui.viewmodel.DetailViewModel
import com.nguonc.streamapp.ui.viewmodel.DetailViewModelFactory
import com.nguonc.streamapp.ui.viewmodel.FavoritesViewModel
import com.nguonc.streamapp.ui.viewmodel.FavoritesViewModelFactory
import com.nguonc.streamapp.ui.viewmodel.HomeViewModel
import com.nguonc.streamapp.ui.viewmodel.HomeViewModelFactory
import com.nguonc.streamapp.ui.viewmodel.PlayerViewModel
import com.nguonc.streamapp.ui.viewmodel.PlayerViewModelFactory
import com.nguonc.streamapp.ui.viewmodel.SearchViewModel
import com.nguonc.streamapp.ui.viewmodel.SearchViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as NguonCApplication).repository

        setContent {
            NguonCStreamAppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "home"

                val isPlayerScreen = currentRoute.startsWith("player")

                Scaffold(
                    bottomBar = {
                        if (!isPlayerScreen) {
                            BottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (isPlayerScreen) Modifier else Modifier.padding(bottom = innerPadding.calculateBottomPadding()))
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "home"
                        ) {
                            composable("home") {
                                val homeViewModel: HomeViewModel = viewModel(
                                    factory = HomeViewModelFactory(repository)
                                )
                                HomeScreen(
                                    viewModel = homeViewModel,
                                    onMovieClick = { slug ->
                                        navController.navigate("detail/$slug")
                                    },
                                    onPlayMovieClick = { slug ->
                                        navController.navigate("detail/$slug")
                                    }
                                )
                            }

                            composable(
                                route = "detail/{slug}",
                                arguments = listOf(navArgument("slug") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val slug = backStackEntry.arguments?.getString("slug") ?: ""
                                val detailViewModel: DetailViewModel = viewModel(
                                    factory = DetailViewModelFactory(repository)
                                )
                                androidx.compose.runtime.LaunchedEffect(slug) {
                                    detailViewModel.loadDetail(slug)
                                }

                                DetailScreen(
                                    viewModel = detailViewModel,
                                    onBack = { navController.popBackStack() },
                                    onPlayEpisode = { movieSlug, epSlug ->
                                        navController.navigate("player/$movieSlug/$epSlug")
                                    }
                                )
                            }

                            composable(
                                route = "player/{slug}/{episodeSlug}",
                                arguments = listOf(
                                    navArgument("slug") { type = NavType.StringType },
                                    navArgument("episodeSlug") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val slug = backStackEntry.arguments?.getString("slug") ?: ""
                                val epSlug = backStackEntry.arguments?.getString("episodeSlug") ?: ""
                                val playerViewModel: PlayerViewModel = viewModel(
                                    factory = PlayerViewModelFactory(repository)
                                )
                                androidx.compose.runtime.LaunchedEffect(slug, epSlug) {
                                    playerViewModel.loadMovieAndEpisode(slug, epSlug)
                                }

                                PlayerScreen(
                                    viewModel = playerViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("search") {
                                val searchViewModel: SearchViewModel = viewModel(
                                    factory = SearchViewModelFactory(repository)
                                )
                                SearchScreen(
                                    viewModel = searchViewModel,
                                    onMovieClick = { slug ->
                                        navController.navigate("detail/$slug")
                                    }
                                )
                            }

                            composable("favorites") {
                                val favoritesViewModel: FavoritesViewModel = viewModel(
                                    factory = FavoritesViewModelFactory(repository)
                                )
                                FavoritesScreen(
                                    viewModel = favoritesViewModel,
                                    onMovieClick = { slug ->
                                        navController.navigate("detail/$slug")
                                    }
                                )
                            }

                            composable("settings") {
                                SettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
