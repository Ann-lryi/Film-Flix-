package com.nguoncflix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nguoncflix.ui.theme.NetflixRed
import com.nguoncflix.ui.theme.NetflixTextSecondary
import com.nguoncflix.ui.theme.NetflixWhite

@Composable
fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Accent bar (Tencent / iQIYI style)
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(22.dp)
                .background(NetflixRed)
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = title,
            color = NetflixWhite,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                letterSpacing = (-0.3).sp
            )
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = "TẤT CẢ",
            color = NetflixTextSecondary,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            modifier = Modifier
                .clickable { onSeeAllClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
