package com.nguonc.stream.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nguonc.stream.ui.browse.BrowseScreen
import com.nguonc.stream.ui.detail.DetailScreen
import com.nguonc.stream.ui.home.HomeScreen
import com.nguonc.stream.ui.library.LibraryScreen
import com.nguonc.stream.ui.list.MovieListScreen
import com.nguonc.stream.ui.list.MovieListSource
import com.nguonc.stream.ui.player.PlayerScreen
import com.nguonc.stream.ui.search.SearchScreen

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val BROWSE = "browse"
    const val LIBRARY = "library"
    const val GRID = "grid/{source}/{key}/{title}"
    const val DETAIL = "detail/{slug}"
    const val PLAYER = "player/{slug}?ep={ep}"

    fun grid(source: MovieListSource, key: String, title: String): String =
        "grid/${source.name.lowercase()}/${Uri.encode(key)}/${Uri.encode(title)}"

    fun detail(slug: String): String = "detail/$slug"

    fun player(slug: String, episodeSlug: String? = null): String =
        if (episodeSlug.isNullOrBlank()) "player/$slug"
        else "player/$slug?ep=${Uri.encode(episodeSlug)}"
}

@Composable
fun NguonCNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onMovieClick = { slug -> navController.navigate(Routes.detail(slug)) },
                onSeeMore = { type, title ->
                    navController.navigate(Routes.grid(MovieListSource.LIST, type, title))
                },
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onMovieClick = { slug -> navController.navigate(Routes.detail(slug)) },
            )
        }
        composable(Routes.BROWSE) {
            BrowseScreen(
                onCategoryClick = { slug, name ->
                    navController.navigate(Routes.grid(MovieListSource.CATEGORY, slug, name))
                },
                onCountryClick = { slug, name ->
                    navController.navigate(Routes.grid(MovieListSource.COUNTRY, slug, name))
                },
            )
        }
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onMovieClick = { slug -> navController.navigate(Routes.detail(slug)) },
                onResumeClick = { slug, ep ->
                    navController.navigate(Routes.player(slug, ep))
                },
            )
        }
        composable(
            route = Routes.GRID,
            arguments = listOf(
                navArgument("source") { type = NavType.StringType },
                navArgument("key") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val source = MovieListSource.valueOf(
                backStackEntry.arguments?.getString("source").orEmpty().uppercase()
            )
            val key = backStackEntry.arguments?.getString("key").orEmpty()
            val title = backStackEntry.arguments?.getString("title").orEmpty()
            MovieListScreen(
                source = source,
                key = key,
                title = title,
                onBack = { navController.popBackStack() },
                onMovieClick = { slug -> navController.navigate(Routes.detail(slug)) },
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("slug") { type = NavType.StringType }),
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("slug").orEmpty()
            DetailScreen(
                slug = slug,
                onBack = { navController.popBackStack() },
                onPlay = { epSlug -> navController.navigate(Routes.player(slug, epSlug)) },
                onCategoryClick = { catSlug, catName ->
                    navController.navigate(Routes.grid(MovieListSource.CATEGORY, catSlug, catName))
                },
            )
        }
        composable(
            route = Routes.PLAYER,
            arguments = listOf(
                navArgument("slug") { type = NavType.StringType },
                navArgument("ep") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("slug").orEmpty()
            PlayerScreen(
                slug = slug,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
