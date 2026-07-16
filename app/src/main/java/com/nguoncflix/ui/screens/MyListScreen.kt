package com.nguoncflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nguoncflix.ui.navigation.Screen
import com.nguoncflix.ui.theme.NetflixDark
import com.nguoncflix.ui.theme.NetflixDarkGray
import com.nguoncflix.ui.theme.NetflixRed
import com.nguoncflix.ui.theme.NetflixTextSecondary
import com.nguoncflix.ui.theme.NetflixWhite
import androidx.compose.foundation.clickable

@Composable
fun MyListScreen(navController: NavController) {
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
                .padding(bottom = 96.dp) // breathing room above BottomNav
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Premium header
            Column {
                Text(
                    "Danh sách của tôi",
                    color = NetflixWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Những bộ phim bạn yêu thích và đang theo dõi",
                    color = NetflixTextSecondary,
                    fontSize = 14.sp
                )
            }

            // 3 Quick action cards (depth + meaning, not just empty text)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    icon = Icons.Default.Favorite,
                    label = "Yêu thích",
                    color = NetflixRed,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Default.Bookmark,
                    label = "Đã lưu",
                    color = androidx.compose.ui.graphics.Color(0xFF4FC3F7),
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Default.History,
                    label = "Lịch sử",
                    color = androidx.compose.ui.graphics.Color(0xFFFFB300),
                    modifier = Modifier.weight(1f)
                )
            }

            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NetflixDarkGray.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🎬", fontSize = 56.sp)
                    Text(
                        "Chưa có phim nào",
                        color = NetflixWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Khám phá và thêm phim vào danh sách\nđể xem lại bất cứ lúc nào.",
                        color = NetflixTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(NetflixRed)
                            .clickable { navController.navigate(Screen.Home.route) }
                            .padding(horizontal = 22.dp, vertical = 10.dp)
                    ) {
                        Text(
                            "Khám phá ngay",
                            color = NetflixWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        BottomNavBar(
            currentRoute = "my_list",
            onNavigate = { route: String ->
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

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NetflixDarkGray.copy(alpha = 0.7f))
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color)
        }
        Text(
            label,
            color = NetflixWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
