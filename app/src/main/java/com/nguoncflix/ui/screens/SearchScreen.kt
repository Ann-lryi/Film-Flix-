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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nguoncflix.data.models.Movie
import com.nguoncflix.ui.navigation.Screen
import com.nguoncflix.ui.theme.*
import com.nguoncflix.viewmodel.SearchViewModel
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(navController: NavController) {
    val viewModel: SearchViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var query by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Debounce typing -> reduce API spam & race conditions
    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            delay(350)
            viewModel.search(query)
        } else {
            viewModel.clearSearch()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NetflixDark, NetflixDarkGray, NetflixDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp)
        ) {
            // Header & search bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Tìm kiếm",
                    color = NetflixWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )

                Spacer(Modifier.height(12.dp))

                // Prominent search bar with subtle border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(29.dp))
                        .background(NetflixDarkGray.copy(alpha = 0.8f))
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
                            onValueChange = { query = it },
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
                    .padding(bottom = 96.dp)
            ) {
                when {
                    // Show "no input yet" state
                    query.isBlank() -> {
                        SearchDefaultContent(onSuggestionClick = { term ->
                            query = term
                        })
                    }

                    // Loading
                    uiState.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = NetflixRed,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Đang tìm \"$query\"...",
                                    color = NetflixTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Error
                    uiState.error != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚠️", fontSize = 40.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    uiState.error ?: "",
                                    color = NetflixTextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Empty result
                    uiState.movies.isEmpty() -> {
                        EmptySearchState(query)
                    }

                    // Results
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    "${uiState.movies.size} kết quả",
                                    color = NetflixTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(uiState.movies, key = { it.id }) { movie ->
                                ModernSearchResultRow(movie) {
                                    navController.navigate(
                                        Screen.MovieDetail.createRoute(movie.slug)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

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
            .background(NetflixDarkGray.copy(alpha = 0.7f))
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

            Spacer(Modifier.height(6.dp))

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
            "Người Ấy Là Ai",
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

        val genres = listOf(
            "Phim Bộ", "Phim Lẻ", "Anime", "Kinh Dị",
            "Hành Động", "Tâm Lý", "Hài Hước"
        )
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
            .background(NetflixDarkGray.copy(alpha = 0.7f))
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
            modifier = Modifier.padding(top = 6.dp),
            textAlign = TextAlign.Center
        )
    }
}
