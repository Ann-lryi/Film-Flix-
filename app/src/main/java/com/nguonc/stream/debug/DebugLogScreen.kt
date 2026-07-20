package com.nguonc.stream.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.FilmFlixIcons
import kotlinx.coroutines.delay

/**
 * Màu sắc cho từng log level — dễ nhận biết:
 *  - SUCCESS: xanh lá (đậm) — thành công
 *  - ERROR:   đỏ (đậm) — lỗi nghiêm trọng
 *  - WARN:    vàng/cam — cảnh báo
 *  - INFO:    xanh dương — thông tin
 *  - DEBUG:   xám — debug chi tiết
 *  - VERBOSE: xám nhạt — log dư thừa
 */
private fun LogLevel.color(): Color = when (this) {
    LogLevel.SUCCESS -> Color(0xFF22C55E)        // bright green
    LogLevel.ERROR -> Color(0xFFFF3B5C)          // bright red
    LogLevel.WARN -> Color(0xFFFFC94A)           // amber
    LogLevel.INFO -> Color(0xFF30E7F0)           // cyan
    LogLevel.DEBUG -> Color(0xFF9CA0B8)          // gray
    LogLevel.VERBOSE -> Color(0xFF6A708B)        // muted gray
}

private fun LogLevel.bgColor(): Color = when (this) {
    LogLevel.SUCCESS -> Color(0xFF0F2A1A)        // dark green tint
    LogLevel.ERROR -> Color(0xFF2A0A12)          // dark red tint
    LogLevel.WARN -> Color(0xFF2B2310)           // dark amber tint
    LogLevel.INFO -> Color(0xFF0A2229)           // dark cyan tint
    LogLevel.DEBUG -> Color(0xFF11121A)          // surface
    LogLevel.VERBOSE -> Color(0xFF0C0C12)        // darker surface
}

@Composable
fun DebugLogScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var filterLevel by remember { mutableStateOf<LogLevel?>(null) }
    var autoScroll by remember { mutableStateOf(true) }

    // Observe AppLogger entries — re-render on tick
    val tick by AppLogger.tick.collectAsStateWithLifecycle()
    val allEntries = AppLogger.entries
    val filteredEntries = remember(tick, filterLevel) {
        if (filterLevel != null) allEntries.filter { it.level == filterLevel }
        else allEntries
    }

    // Auto-scroll to bottom khi có log mới
    LaunchedEffect(filteredEntries.size, autoScroll) {
        if (autoScroll && filteredEntries.isNotEmpty()) {
            delay(50)
            listState.animateScrollToItem(filteredEntries.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ---- Header ----
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                            Icon(
                                FilmFlixIcons.ChevronLeft,
                                contentDescription = "Quay lại",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column {
                            Text(
                                "Debug Logs",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${filteredEntries.size} entries${if (filterLevel != null) " (filtered: ${filterLevel!!.label})" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Auto-scroll toggle
                        Surface(
                            shape = AppShapes.Pill,
                            color = if (autoScroll) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { autoScroll = !autoScroll }
                        ) {
                            Text(
                                if (autoScroll) "AUTO ✓" else "AUTO",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (autoScroll) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        // Copy to clipboard
                        IconButton(
                            onClick = {
                                val text = AppLogger.export()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                                    as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("App Logs", text))
                                Toast.makeText(context, "Đã copy ${filteredEntries.size} logs", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                FilmFlixIcons.ShareOutline,
                                contentDescription = "Copy logs",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // Clear
                        IconButton(
                            onClick = { AppLogger.clear() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                FilmFlixIcons.TrashOutline,
                                contentDescription = "Xoá log",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ---- Filter chips ----
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        label = "ALL",
                        selected = filterLevel == null,
                        color = Color.White,
                        bgColor = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { filterLevel = null }
                    )
                    LogLevel.values().forEach { level ->
                        FilterChip(
                            label = level.shortLabel,
                            selected = filterLevel == level,
                            color = level.color(),
                            bgColor = level.bgColor(),
                            onClick = { filterLevel = if (filterLevel == level) null else level }
                        )
                    }
                }
            }
        }

        // ---- Log list ----
        if (filteredEntries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (filterLevel != null) "Không có log nào ở level ${filterLevel!!.label}"
                    else "Chưa có log nào.\nMở phim và xem player để xem log.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredEntries, key = { it.id }) { entry ->
                    LogEntryRow(entry)
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    color: Color,
    bgColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        shape = AppShapes.Pill,
        color = if (selected) bgColor else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Black else FontWeight.Bold
            ),
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun LogEntryRow(entry: LogEntry) {
    val levelColor = entry.level.color()
    val rowBg = entry.level.bgColor()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(rowBg.copy(alpha = 0.6f))
            .border(
                width = 0.5.dp,
                color = levelColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Left: level short label badge (colored)
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(levelColor.copy(alpha = 0.18f))
                .border(1.dp, levelColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                entry.level.shortLabel,
                color = levelColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.size(8.dp))

        // Middle: content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    entry.timeFormatted,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    "[${entry.tag}]",
                    color = levelColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                entry.message,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp,
                overflow = TextOverflow.Visible
            )
            // Stack trace nếu có
            entry.throwableTrace?.let { trace ->
                Spacer(Modifier.height(2.dp))
                Text(
                    trace,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
