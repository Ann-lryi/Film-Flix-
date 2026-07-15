package com.nguoncflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixDark)
            // Use PaddingValues explicitly — this directly matches the first overload
            // and is the ONLY reliable way to avoid the
            // "None of the following candidates is applicable" padding error
            .padding(PaddingValues(top = 52.dp))
    ) {
        // Modern prominent header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, bottom = 8.dp)
        ) {
            Text(
                text = "Tìm kiếm",
                color = NetflixWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(8.dp))

            // VERY LARGE + PROMINENT search bar (modern iQIYI / Netflix style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .clip(RoundedCornerShape(31.dp))
                    .background(NetflixDarkGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = NetflixTextSecondary,
                        modifier = Modifier
                            .padding(start = 18.dp)
                            .size(20.dp)
                    )

                    TextField(
                        value = query,
                        onValueChange = { 
                            query = it
                            viewModel.search(it)
                        },
                        placeholder = {
                            Text(
                                "Tìm phim, diễn viên, thể loại...",
                                color = NetflixTextSecondary.copy(0.65f),
                                fontSize = 15.sp
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = NetflixWhite,
                            unfocusedTextColor = NetflixWhite,
                            cursorColor = NetflixRed,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { keyboardController?.hide() }
                        )
                    )

                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            viewModel.clearSearch()
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Xóa",
                                tint = NetflixTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
        ) {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = NetflixRed,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                query.isNotBlank() && uiState.movies.isEmpty() -> {
                    EmptySearchState(query)
                }

                uiState.movies.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.movies) { movie ->
                            ModernSearchResultRow(movie) {
                                navController.navigate(Screen.MovieDetail.createRoute(movie.slug))
                            }
                        }
                    }
                }

                else -> {
                    SearchDefaultContent(onSuggestionClick = { term ->
                        query = term
                        viewModel.search(term)
                    })
                }
            }
        }
    }

    // Bottom nav
    Box(Modifier.fillMaxSize()) {
        BottomNavBar(
            currentRoute = "search",
            onNavigate = { route ->
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
private fun ModernSearchResultRow(movie: Movie, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NetflixDarkGray)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = movie.thumbUrl.takeIf { it.isNotBlank() } ?: movie.posterUrl,
            contentDescription = movie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(68.dp)
                .height(96.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = movie.name,
                color = NetflixWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 2
            )

            movie.originName?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    color = NetflixTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                movie.year?.let {
                    Text("$it", color = NetflixTextSecondary, fontSize = 12.sp)
                }
                movie.episodeCurrent?.let {
                    Text("  •  $it", color = NetflixTextSecondary, fontSize = 12.sp)
                }
                movie.quality?.let {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(NetflixRed, RoundedCornerShape(3.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            it,
                            color = NetflixWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchDefaultContent(onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            "Tìm kiếm phổ biến",
            color = NetflixWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(12.dp))

        val trending = listOf(
            "Sếp Ơi Mai Đừng Gặp Nhau",
            "Sau Hôn Nhân",
            "Đồng Điệu Yêu Thương",
            "Sát Nhân Huyền Bí",
            "Bóng Ma Báo Thù"
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(trending) { term ->
                Chip(text = term, onClick = { onSuggestionClick(term) })
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "Thể loại",
            color = NetflixWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(12.dp))

        val genres = listOf("Phim Bộ", "Phim Lẻ", "Anime", "Kinh Dị", "Hành Động", "Tâm Lý", "Hài Hước")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(genres) { genre ->
                Chip(text = genre, onClick = { onSuggestionClick(genre) })
            }
        }

        Spacer(Modifier.height(36.dp))

        Text(
            "Nhập tên phim, diễn viên hoặc thể loại để bắt đầu tìm kiếm",
            color = NetflixTextSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun Chip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(NetflixDarkGray)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = NetflixWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptySearchState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔍", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Không tìm thấy kết quả",
            color = NetflixWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp
        )
        Text(
            "Không có phim nào khớp với \"$query\"",
            color = NetflixTextSecondary,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
