package com.nguonc.stream.ui.browse

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.theme.FilmFlixIcons
import com.nguonc.stream.ui.components.LoadingBox
import com.nguonc.stream.ui.theme.AccentCyan
import com.nguonc.stream.ui.theme.AccentViolet
import com.nguonc.stream.ui.theme.AppGradients
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.Elevation
import com.nguonc.stream.ui.theme.Motion
import com.nguonc.stream.ui.theme.PremiumTextStyles
import com.nguonc.stream.ui.theme.Primary
import com.nguonc.stream.ui.theme.glowShadow
import com.nguonc.stream.ui.theme.premiumShadow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrowseScreen(
    onCategoryClick: (slug: String, name: String) -> Unit,
    onCountryClick: (slug: String, name: String) -> Unit,
    viewModel: BrowseViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.isLoading -> LoadingBox()
        state.error != null -> ErrorBox(state.error!!, onRetry = viewModel::load)
        else -> Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            // Eyebrow pill — "KHÁM PHÁ"
            Surface(
                shape = AppShapes.Pill,
                color = AccentViolet.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, AccentViolet.copy(alpha = 0.25f))
            ) {
                Text(
                    "KHÁM PHÁ",
                    style = PremiumTextStyles.Eyebrow,
                    color = AccentViolet,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Thế giới điện ảnh\nvô tận đang chờ",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    lineHeight = 34.sp,
                    letterSpacing = (-0.8).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Chọn thể loại hoặc quốc gia yêu thích để bắt đầu",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            PremiumSectionCard(
                title = "Thể loại phim",
                count = state.categories.size,
                icon = FilmFlixIcons.TagOutline,
                gradient = AppGradients.PrimaryGradient,
                iconColor = Primary
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    state.categories.forEach { category ->
                        BrowseChip(
                            label = category.name,
                            onClick = { onCategoryClick(category.slug, category.name) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            PremiumSectionCard(
                title = "Quốc gia & Khu vực",
                count = state.countries.size,
                icon = FilmFlixIcons.GlobeOutline,
                gradient = Brush.linearGradient(listOf(AccentCyan, Color(0xFF00B8D4))),
                iconColor = AccentCyan
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    state.countries.forEach { country ->
                        BrowseChip(
                            label = country.name,
                            flag = countryFlag(country.name),
                            dotColor = AccentCyan,
                            onClick = { onCountryClick(country.slug, country.name) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
private fun BrowseChip(
    label: String,
    modifier: Modifier = Modifier,
    flag: String? = null,
    dotColor: Color = Primary,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "browseChipScale"
    )
    Surface(
        shape = AppShapes.Small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isPressed) dotColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
        ),
        modifier = modifier
            .scale(scale)
            .clip(AppShapes.Small)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (flag != null) {
                Text(text = flag, style = MaterialTheme.typography.bodyMedium)
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PremiumSectionCard(
    title: String,
    count: Int,
    icon: ImageVector,
    gradient: Brush,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        shape = AppShapes.XLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.XS),
        modifier = Modifier.fillMaxWidth().premiumShadow(Elevation.S, AppShapes.XLarge)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(AppShapes.Small)
                            .background(iconColor.copy(alpha = 0.16f))
                            .glowShadow(
                                color = iconColor,
                                shape = AppShapes.Small,
                                glowRadius = 10.dp,
                                elevation = Elevation.None
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$count mục",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(26.dp)
                        .clip(AppShapes.Pill)
                        .background(gradient)
                )
            }
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

private fun countryFlag(name: String): String {
    return when (name.lowercase()) {
        "hàn quốc" -> "🇰🇷"
        "nhật bản", "nhat ban" -> "🇯🇵"
        "trung quốc", "trung quoc" -> "🇨🇳"
        "âu mỹ", "au my", "mỹ", "my" -> "🇺🇸"
        "việt nam", "viet nam" -> "🇻🇳"
        "thái lan", "thai lan" -> "🇹🇭"
        "anh" -> "🇬🇧"
        else -> "🌍"
    }
}
