package com.nguoncflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nguoncflix.ui.navigation.Screen
import com.nguoncflix.ui.theme.NetflixDark
import com.nguoncflix.ui.theme.NetflixDarkGray
import com.nguoncflix.ui.theme.NetflixGray
import com.nguoncflix.ui.theme.NetflixRed
import com.nguoncflix.ui.theme.NetflixTextSecondary
import com.nguoncflix.ui.theme.NetflixWhite
import androidx.compose.foundation.clickable

@Composable
fun ProfileScreen(navController: NavController) {
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
                .padding(bottom = 96.dp)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Premium header
            Text(
                "Hồ sơ",
                color = NetflixWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )

            // User card with depth
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(NetflixDarkGray.copy(alpha = 0.7f))
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar with gradient ring
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NetflixRed, Color(0xFFFF6B6B))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = NetflixWhite,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "NguoncFlix Viewer",
                        color = NetflixWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Khách xem miễn phí",
                        color = NetflixTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Stats row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChip(value = "0", label = "Phim đã xem", modifier = Modifier.weight(1f))
                StatChip(value = "0", label = "Yêu thích", modifier = Modifier.weight(1f))
                StatChip(value = "0", label = "Đang theo dõi", modifier = Modifier.weight(1f))
            }

            // Settings list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(NetflixDarkGray.copy(alpha = 0.7f))
            ) {
                SettingsRow(
                    icon = Icons.Default.Notifications,
                    title = "Thông báo",
                    subtitle = "Bật thông báo phim mới"
                )
                Divider()
                SettingsRow(
                    icon = Icons.Default.Download,
                    title = "Tải xuống",
                    subtitle = "Quản lý bộ nhớ tải về"
                )
                Divider()
                SettingsRow(
                    icon = Icons.Default.Subtitles,
                    title = "Phụ đề",
                    subtitle = "Tiếng Việt • Mặc định"
                )
                Divider()
                SettingsRow(
                    icon = Icons.Default.DarkMode,
                    title = "Giao diện tối",
                    subtitle = "Đang bật"
                )
                Divider()
                SettingsRow(
                    icon = Icons.Default.Settings,
                    title = "Cài đặt chung",
                    subtitle = "Ngôn ngữ, chất lượng phát"
                )
            }

            // App info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "NguoncFlix v1.0.0",
                    color = NetflixTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Dữ liệu từ phim.nguonc.com",
                    color = NetflixTextSecondary.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }

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

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.7.dp)
            .background(NetflixGray.copy(alpha = 0.35f))
    )
}

@Composable
private fun StatChip(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NetflixDarkGray.copy(alpha = 0.7f))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            value,
            color = NetflixRed,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            label,
            color = NetflixTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* future */ }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NetflixGray.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = NetflixWhite,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = NetflixWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                color = NetflixTextSecondary,
                fontSize = 11.sp
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = NetflixTextSecondary.copy(alpha = 0.6f)
        )
    }
}
