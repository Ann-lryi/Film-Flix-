package com.aho.yunphim.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aho.yunphim.di.AppContainer
import com.aho.yunphim.ui.detail.DetailScreen
import com.aho.yunphim.ui.detail.DetailViewModel
import com.aho.yunphim.ui.home.HomeScreen
import com.aho.yunphim.ui.home.HomeViewModel
import com.aho.yunphim.ui.player.PlayerScreen
import com.aho.yunphim.ui.player.PlayerViewModel
import com.aho.yunphim.ui.search.SearchScreen
import com.aho.yunphim.ui.search.SearchViewModel

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Search : Routes("search")

    data object Detail : Routes("detail/{slug}") {
        fun build(slug: String) = "detail/${Uri.encode(slug)}"
    }

    data object Player : Routes("player/{slug}/{serverIndex}/{episodeIndex}") {
        fun build(slug: String, serverIndex: Int, episodeIndex: Int) =
            "player/${Uri.encode(slug)}/$serverIndex/$episodeIndex"
    }
}

/** Tạo ViewModel bằng lambda thay vì lớp Factory riêng - đủ dùng cho DI thủ công không Hilt. */
@Composable
inline fun <reified VM : ViewModel> rememberVm(
    key: String? = null,
    crossinline create: () -> VM,
): VM = viewModel(
    key = key,
    factory = viewModelFactory { initializer { create() } },
)

@Composable
fun YunPhimNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Routes.Home.route) {
        composable(Routes.Home.route) {
            HomeScreen(
                viewModel = rememberVm { HomeViewModel(container.movieRepository) },
                onMovieClick = { slug -> navController.navigate(Routes.Detail.build(slug)) },
                onSearchClick = { navController.navigate(Routes.Search.route) },
            )
        }

        composable(Routes.Search.route) {
            SearchScreen(
                viewModel = rememberVm { SearchViewModel(container.movieRepository) },
                onBack = { navController.popBackStack() },
                onMovieClick = { slug -> navController.navigate(Routes.Detail.build(slug)) },
            )
        }

        composable(
            route = Routes.Detail.route,
            arguments = listOf(navArgument("slug") { type = NavType.StringType }),
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("slug").orEmpty()
            DetailScreen(
                viewModel = rememberVm(key = slug) { DetailViewModel(container.movieRepository, slug) },
                onBack = { navController.popBackStack() },
                onPlayEpisode = { serverIndex, episodeIndex ->
                    navController.navigate(Routes.Player.build(slug, serverIndex, episodeIndex))
                },
            )
        }

        composable(
            route = Routes.Player.route,
            arguments = listOf(
                navArgument("slug") { type = NavType.StringType },
                navArgument("serverIndex") { type = NavType.IntType; defaultValue = 0 },
                navArgument("episodeIndex") { type = NavType.IntType; defaultValue = 0 },
            ),
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            val slug = args?.getString("slug").orEmpty()
            val serverIndex = args?.getInt("serverIndex") ?: 0
            val episodeIndex = args?.getInt("episodeIndex") ?: 0
            PlayerScreen(
                viewModel = rememberVm(key = "$slug-$serverIndex-$episodeIndex") {
                    PlayerViewModel(
                        repository = container.movieRepository,
                        streamcResolver = container.streamcResolver,
                        slug = slug,
                        serverIndex = serverIndex,
                        episodeIndex = episodeIndex,
                    )
                },
                onBack = { navController.popBackStack() },
                onSwitchEpisode = { newServerIndex, newEpisodeIndex ->
                    navController.navigate(Routes.Player.build(slug, newServerIndex, newEpisodeIndex)) {
                        popUpTo(Routes.Player.route) { inclusive = true }
                    }
                },
            )
        }
    }
}
