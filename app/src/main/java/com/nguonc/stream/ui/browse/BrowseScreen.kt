package com.nguonc.stream.ui.browse

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.components.LoadingBox
import com.nguonc.stream.ui.theme.Aurora
import com.nguonc.stream.ui.theme.AuroraCyan
import com.nguonc.stream.ui.theme.AuroraViolet
import com.nguonc.stream.ui.theme.BrandCherry
import com.nguonc.stream.ui.theme.OnDarkSurfaceVariant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrowseScreen(
    onCategoryClick: (slug: String, name: String) -> Unit,
    onCountryClick: (slug: String, name: String) -> Unit,
    viewModel: BrowseViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient backgrounds
        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-40).dp)
                .drawBehind {
                    drawCircle(brush = Aurora.AmbientViolet, radius = 380.dp.toPx())
                }
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 80.dp, y = 100.dp)
                .drawBehind {
                    drawCircle(brush = Aurora.AmbientCyan, radius = 320.dp.toPx())
                }
        )

        when {
            state.isLoading -> LoadingBox()
            state.error != null -> ErrorBox(state.error!!, onRetry = viewModel::load)
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 140.dp),
            ) {
                // Hero header
                BrowseHeader(
                    categoryCount = state.categories.size,
                    countryCount = state.countries.size,
                )

                Spacer(Modifier.height(28.dp))

                PremiumSectionCard(
                    title = "Thể loại phim",
                    count = state.categories.size,
                    subtitle = "Khám phá theo thể loại yêu thích",
                    icon = Icons.Filled.Category,
                    accent = Aurora.BrandLinear,
                    iconColor = BrandCherry,
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        state.categories.forEach { category ->
                            CategoryChip(
                                name = category.name,
                                accent = false,
                                onClick = { onCategoryClick(category.slug, category.name) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                PremiumSectionCard(
                    title = "Quốc gia & Khu vực",
                    count = state.countries.size,
                    subtitle = "Điện ảnh từ mọi nền văn hóa",
                    icon = Icons.Filled.Public,
                    accent = Brush.linearGradient(listOf(AuroraCyan, Color(0xFF00B8D4))),
                    iconColor = AuroraCyan,
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        state.countries.forEach { country ->
                            CategoryChip(
                                name = "${countryFlag(country.name)} ${country.name}",
                                accent = true,
                                onClick = { onCountryClick(country.slug, country.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrowseHeader(
    categoryCount: Int,
    countryCount: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = AuroraViolet.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, AuroraViolet.copy(alpha = 0.32f)),
        ) {
            Text(
                "KHÁM PHÁ",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                color = AuroraViolet,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Box(
            modifier = Modifier
                .height(1.dp)
                .width(60.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.16f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
    Spacer(Modifier.height(12.dp))
    Text(
        text = "Thế giới điện ảnh\nvô tận đang chờ",
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Black,
            lineHeight = 32.sp,
            letterSpacing = (-0.6).sp
        ),
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "$categoryCount thể loại  •  $countryCount quốc gia",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = OnDarkSurfaceVariant
    )
}

@Composable
private fun PremiumSectionCard(
    title: String,
    count: Int,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Brush,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f)
            )
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
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
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(iconColor.copy(alpha = 0.16f))
                            .border(
                                BorderStroke(1.dp, iconColor.copy(alpha = 0.3f)),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(accent)
                        .shadow(6.dp, RoundedCornerShape(100.dp), ambientColor = iconColor.copy(alpha = 0.6f))
                )
            }
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
private fun CategoryChip(
    name: String,
    accent: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (accent) AuroraCyan.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        border = BorderStroke(
            1.dp,
            if (accent) AuroraCyan.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (accent) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AuroraCyan)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(BrandCherry)
                )
            }
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun countryFlag(name: String): String = when (name.lowercase()) {
    "hàn quốc" -> "🇰🇷"
    "nhật bản", "nhat ban" -> "🇯🇵"
    "trung quốc", "trung quoc" -> "🇨🇳"
    "âu mỹ", "au my", "mỹ", "my" -> "🇺🇸"
    "việt nam", "viet nam" -> "🇻🇳"
    "thái lan", "thai lan" -> "🇹🇭"
    "anh" -> "🇬🇧"
    else -> "🌍"
}
