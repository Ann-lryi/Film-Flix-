package com.nguoncflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import com.nguoncflix.ui.theme.NetflixDark
import com.nguoncflix.ui.theme.NetflixDarkGray
import com.nguoncflix.ui.theme.NetflixRed
import com.nguoncflix.ui.theme.NetflixTextSecondary
import com.nguoncflix.ui.theme.NetflixWhite

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        NetflixDark.copy(alpha = 0.95f),
                        NetflixDarkGray
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Top hairline divider for visual depth
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(NetflixTextSecondary.copy(alpha = 0.12f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                NavItem("home", "Trang chủ", Icons.Default.Home),
                NavItem("search", "Tìm kiếm", Icons.Default.Search),
                NavItem("my_list", "Danh sách", Icons.Default.List),
                NavItem("profile", "Hồ sơ", Icons.Default.Person)
            )

            items.forEach { (route, label, icon) ->
                BottomNavItem(
                    icon = icon,
                    label = label,
                    selected = currentRoute == route,
                    onClick = { onNavigate(route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint = if (selected) NetflixWhite else NetflixTextSecondary
    val textColor = if (selected) NetflixRed else NetflixTextSecondary
    val indicatorColor = if (selected) NetflixRed.copy(alpha = 0.18f) else Color.Transparent

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(indicatorColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
