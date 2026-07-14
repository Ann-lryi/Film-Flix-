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
fun ProfileScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Hồ sơ",
            color = NetflixWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "NguoncFlix\nDemo App\n\nBuild by GitHub CI/CD",
            color = NetflixTextSecondary
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BottomNavBar(
            currentRoute = "profile",
            onNavigate = { route: String ->
                when (route) {
                    "home" -> navController.navigate(Screen.Home.route)
                    "search" -> navController.navigate(Screen.Search.route)
                    "my_list" -> navController.navigate(Screen.MyList.route)
                    "profile" -> {}
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
