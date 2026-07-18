package com.aho.yunphim.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aho.yunphim.data.model.MovieSummary
import com.aho.yunphim.ui.theme.YunPhimColors

@Composable
fun MovieCard(
    movie: MovieSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .background(YunPhimColors.SurfaceVariant),
        ) {
            AsyncImage(
                model = movie.displayImage,
                contentDescription = movie.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (!movie.quality.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(YunPhimColors.Accent)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = movie.quality.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(YunPhimColors.ScrimTransparent, YunPhimColors.ScrimTop),
                        ),
                    ),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = movie.name.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = YunPhimColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!movie.originName.isNullOrBlank()) {
            Text(
                text = movie.originName.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = YunPhimColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = YunPhimColors.Accent)
    }
}

@Composable
fun FullScreenError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isSchemaMismatch: Boolean = false,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = YunPhimColors.Error,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(12.dp))
            if (isSchemaMismatch) {
                Text(
                    text = "API trả dữ liệu khác giả định",
                    style = MaterialTheme.typography.titleMedium,
                    color = YunPhimColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = YunPhimColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(onClick = onRetry) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Thử lại")
            }
        }
    }
}
