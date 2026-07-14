package com.nguoncflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nguoncflix.data.models.Movie
import com.nguoncflix.ui.navigation.Screen
import com.nguoncflix.ui.theme.*
import com.nguoncflix.viewmodel.SearchViewModel

@Composable
fun SearchScreen(navController: NavController) {
    val viewModel: SearchViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixDark)
            .padding(bottom = 60.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = { 
                query = it
                viewModel.search(it)
            },
            placeholder = { Text("Tìm kiếm phim...", color = NetflixTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NetflixRed,
                unfocusedBorderColor = NetflixGray,
                focusedTextColor = NetflixWhite,
                unfocusedTextColor = NetflixWhite
            ),
            singleLine = true
        )

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NetflixRed)
                }
            }
            uiState.movies.isEmpty() && query.isNotBlank() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy kết quả", color = NetflixTextSecondary)
                }
            }
            else -> {
                LazyColumn {
                    items(uiState.movies) { movie ->
                        SearchMovieRow(movie) {
                            navController.navigate(Screen.MovieDetail.createRoute(movie.slug))
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BottomNavBar(
            currentRoute = "search",
            onNavigate = { route: String ->
                when (route) {
                    "home" -> navController.navigate(Screen.Home.route)
                    "search" -> {}
                    "my_list" -> navController.navigate(Screen.MyList.route)
                    "profile" -> navController.navigate(Screen.Profile.route)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SearchMovieRow(movie: Movie, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = movie.thumbUrl,
            contentDescription = movie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(80.dp)
                .height(110.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = movie.name,
                fontWeight = FontWeight.SemiBold,
                color = NetflixWhite
            )
            movie.originName?.let {
                Text(it, color = NetflixTextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                movie.year?.let { Text("$it", color = NetflixTextSecondary) }
                movie.episodeCurrent?.let { Text(it, color = NetflixTextSecondary) }
            }
        }
    }
}
