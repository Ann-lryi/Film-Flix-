package com.nguoncflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nguoncflix.ui.navigation.Screen
import com.nguoncflix.ui.theme.NetflixDark
import com.nguoncflix.ui.theme.NetflixTextSecondary
import com.nguoncflix.ui.theme.NetflixWhite

@Composable
fun MyListScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Danh sách của bạn",
            color = NetflixWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Tính năng này đang phát triển.\nBạn có thể thêm phim vào danh sách sau.",
            color = NetflixTextSecondary
        )
    }

    Box(Modifier.fillMaxSize()) {
        BottomNavBar(
            currentRoute = "my_list",
            onNavigate = { route ->
                when (route) {
                    "home" -> navController.navigate(Screen.Home.route)
                    "search" -> navController.navigate(Screen.Search.route)
                    "my_list" -> {}
                    "profile" -> navController.navigate(Screen.Profile.route)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
