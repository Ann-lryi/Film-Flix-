package com.nguoncflix.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nguoncflix.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object MyList : Screen("my_list")
    object Profile : Screen("profile")
    object MovieDetail : Screen("movie_detail/{slug}") {
        fun createRoute(slug: String) = "movie_detail/$slug"
    }
}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(Screen.MyList.route) {
            MyListScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.MovieDetail.route) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("slug") ?: ""
            MovieDetailScreen(
                slug = slug,
                navController = navController
            )
        }
    }
}
