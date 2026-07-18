package com.nguonc.stream.ui.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nguonc.stream.ui.components.EmptyBox
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.components.MoviePosterCard
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.FilmFlixIcons
import com.nguonc.stream.ui.theme.GoldStar
import com.nguonc.stream.ui.theme.Motion
import com.nguonc.stream.ui.theme.PremiumTextStyles
import com.nguonc.stream.ui.theme.Primary
import com.nguonc.stream.ui.theme.glowShadow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onMovieClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val shouldLoadMore by remember {
        derivedStateOf {
            val info = gridState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - 6
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Premium search header
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Tìm kiếm",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = AppShapes.Pill,
                        color = Primary.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, Primary.copy(alpha = 0.25f))
                    ) {
                        Text(
                            "BETA",
                            style = PremiumTextStyles.Eyebrow,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Tìm tên phim, diễn viên, đạo diễn...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Surface(
                            shape = AppShapes.Small,
                            color = Primary.copy(alpha = 0.16f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    FilmFlixIcons.SearchOutline,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            val clearInteraction = remember { MutableInteractionSource() }
                            val isPressed by clearInteraction.collectIsPressedAsState()
                            val clearScale by androidx.compose.animation.core.animateFloatAsState(
                                targetValue = if (isPressed) Motion.PressScale else 1f,
                                animationSpec = Motion.PressSpring,
                                label = "clearBtnScale"
                            )
                            IconButton(
                                onClick = { viewModel.onQueryChange("") },
                                interactionSource = clearInteraction,
                                modifier = Modifier.scale(clearScale)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Icon(
                                        FilmFlixIcons.ClearOutline,
                                        contentDescription = "Xoá",
                                        modifier = Modifier.padding(6.dp).size(16.dp)
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    shape = AppShapes.Large,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary.copy(alpha = 0.65f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.40f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
                        cursorColor = Primary
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = { keyboardController?.hide() }
                    ),
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = when {
                    state.query.isBlank() -> SearchPhase.Suggestions
                    state.error != null -> SearchPhase.Error
                    state.items.isEmpty() && state.isLoading -> SearchPhase.Loading
                    state.items.isEmpty() -> SearchPhase.Empty
                    else -> SearchPhase.Results
                },
                transitionSpec = {
                    fadeIn(Motion.standard(Motion.DurationM)) togetherWith
                        fadeOut(Motion.standard(Motion.DurationS))
                },
                label = "searchPhase"
            ) { phase ->
                when (phase) {
                    SearchPhase.Suggestions -> SearchSuggestions(
                        onKeywordClick = { viewModel.onQueryChange(it) }
                    )
                    SearchPhase.Error -> ErrorBox(state.error!!, onRetry = viewModel::retry)
                    SearchPhase.Loading -> Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                    SearchPhase.Empty -> EmptyBox("Không tìm thấy kết quả cho \"${state.query}\"")
                    SearchPhase.Results -> LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Adaptive(minSize = 118.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.items, key = { it.id }) { movie ->
                            MoviePosterCard(movie = movie, onClick = { onMovieClick(movie.slug) })
                        }
                        if (state.isLoadingMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Primary,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class SearchPhase { Suggestions, Loading, Empty, Results, Error }

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun SearchSuggestions(onKeywordClick: (String) -> Unit) {
    val hotKeywords = remember {
        listOf("Trảm Thần", "Đấu Phá Thương Khung", "One Piece", "Solo Leveling", "Thế Giới Hoàn Mỹ", "Thần Ẩn", "Linh Vực", "Naruto", "Doraemon 2025", "Avengers")
    }
    Column(modifier = Modifier.padding(20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = AppShapes.Small,
                color = GoldStar.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, GoldStar.copy(alpha = 0.25f)),
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = FilmFlixIcons.FlameFilled,
                        contentDescription = null,
                        tint = GoldStar,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = "Đang thịnh hành",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = AppShapes.Pill,
                color = Primary.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.25f))
            ) {
                Text(
                    "LIVE",
                    style = PremiumTextStyles.Eyebrow,
                    color = Primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            hotKeywords.forEach { keyword ->
                val interaction = remember { MutableInteractionSource() }
                val isPressed by interaction.collectIsPressedAsState()
                val scale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isPressed) Motion.PressScale else 1f,
                    animationSpec = Motion.PressSpring,
                    label = "hotKwScale"
                )
                Surface(
                    shape = AppShapes.Pill,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (isPressed) Primary.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .scale(scale)
                        .clip(AppShapes.Pill)
                        .clickable(interactionSource = interaction, indication = null) {
                            onKeywordClick(keyword)
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "#",
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = keyword,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(30.dp))
        // Recent searches (mock)
        Text(
            "Tìm kiếm gần đây",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("One Piece Film Red", "Solo Leveling SS2", "Naruto Shippuden").forEach { recent ->
                val interaction = remember { MutableInteractionSource() }
                val isPressed by interaction.collectIsPressedAsState()
                val bgAlpha by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isPressed) 0.85f else 0f,
                    animationSpec = Motion.snappy(),
                    label = "recentBg"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.Small)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = bgAlpha))
                        .clickable(interactionSource = interaction, indication = null) {
                            onKeywordClick(recent)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = AppShapes.Small,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                FilmFlixIcons.ClockOutline, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        recent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        FilmFlixIcons.ClearOutline, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
