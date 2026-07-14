package com.nguoncflix.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    NavigationBar(
        modifier = modifier,
        containerColor = NetflixDarkGray,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple("home", "Trang chủ", Icons.Default.Home),
            Triple("search", "Tìm kiếm", Icons.Default.Search),
            Triple("my_list", "Danh sách", Icons.Default.List),
            Triple("profile", "Hồ sơ", Icons.Default.Person)
        )

        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                icon = { Icon(imageVector = icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NetflixRed,
                    selectedTextColor = NetflixWhite,
                    unselectedIconColor = NetflixTextSecondary,
                    unselectedTextColor = NetflixTextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
