package com.nguonc.stream.debug

import android.util.Log
import androidx.compose.runtime.Immutable
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

/**
 * AppLogger — Bộ ghi log tập trung cho toàn app.
 *
 * Tính năng:
 *  - Buffer in-memory (max 500 entries, FIFO)
 *  - 5 levels: VERBOSE, DEBUG, INFO, WARN, ERROR — mỗi level 1 màu
 *  - Tag theo component (API, REPO, VM, PLAYER, WEBVIEW, NAV, UI)
 *  - Expose StateFlow<Long> tick để UI observe realtime
 *  - Cũng log ra logcat (Log.d/w/e) cho adb logcat
 *  - Export to text (cho copy/share)
 *
 * ⚠️ THREAD-SAFE: AppLogger có thể được gọi từ bất kỳ thread nào
 * (Dispatchers.IO trong Repository, main thread trong UI, v.v.)
 * — Dùng CopyOnWriteArrayList + synchronized để đảm bảo an toàn.
 */
object AppLogger {

    private const val MAX_ENTRIES = 500
    private val idCounter = AtomicLong(0)

    // Thread-safe list — CopyOnWriteArrayList cho read-heavy, write-light pattern.
    // SnapshotStateList KHÔNG thread-safe → không dùng được từ background thread.
    private val _entries: CopyOnWriteArrayList<LogEntry> = CopyOnWriteArrayList()

    /** Public read-only snapshot của entries (copy ra list mới để UI iterate an toàn). */
    val entries: List<LogEntry> get() = _entries.toList()

    private val _tick = MutableStateFlow(0L)
    val tick: StateFlow<Long> = _tick.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    // ---------- Public API ----------

    fun v(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.VERBOSE, tag, message, throwable)

    fun d(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.DEBUG, tag, message, throwable)

    fun i(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.INFO, tag, message, throwable)

    fun w(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.WARN, tag, message, throwable)

    fun e(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.ERROR, tag, message, throwable)

    /** SUCCESS — custom level cho "thành công" (xanh lá, đậm). */
    fun success(tag: String, message: String) =
        log(LogLevel.SUCCESS, tag, message, null)

    fun clear() {
        _entries.clear()
        _tick.update { it + 1 }
    }

    /** Export toàn bộ log ra text (cho copy/share). */
    fun export(): String {
        val snapshot = _entries.toList()
        return buildString {
            for (entry in snapshot) {
                val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                    .format(Date(entry.timestamp))
                append("[${entry.level.label}] ")
                append(time)
                append(" ")
                append(entry.tag)
                append(": ")
                append(entry.message)
                entry.throwableTrace?.let { append("\n").append(it) }
                append("\n")
            }
        }
    }

    // ---------- Internal ----------

    private fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val entry = LogEntry(
            id = idCounter.incrementAndGet(),
            level = level,
            tag = tag,
            message = message,
            timestamp = System.currentTimeMillis(),
            timeFormatted = timeFormat.format(Date()),
            throwableTrace = throwable?.let { Log.getStackTraceString(it) }
                ?.takeIf { it.isNotBlank() },
        )

        // Thread-safe add + trim (FIFO)
        synchronized(_entries) {
            _entries.add(entry)
            while (_entries.size > MAX_ENTRIES) {
                _entries.removeAt(0)
            }
        }
        // Trigger UI recomposition via tick (StateFlow is thread-safe)
        _tick.update { it + 1 }

        // Also log to logcat (để xem qua adb logcat nếu cần)
        val logcatTag = "AppLogger/$tag"
        val logcatMsg = message + (entry.throwableTrace?.let { "\n$it" } ?: "")
        when (level) {
            LogLevel.VERBOSE -> Log.v(logcatTag, logcatMsg)
            LogLevel.DEBUG -> Log.d(logcatTag, logcatMsg)
            LogLevel.INFO -> Log.i(logcatTag, logcatMsg)
            LogLevel.WARN -> Log.w(logcatTag, logcatMsg)
            LogLevel.ERROR -> Log.e(logcatTag, logcatMsg)
            LogLevel.SUCCESS -> Log.i(logcatTag, "✓ $logcatMsg")
        }
    }
}

// ---------- Models ----------

enum class LogLevel(val label: String, val shortLabel: String) {
    VERBOSE("VERBOSE", "V"),
    DEBUG("DEBUG", "D"),
    INFO("INFO", "I"),
    WARN("WARN", "W"),
    ERROR("ERROR", "E"),
    SUCCESS("SUCCESS", "✓");
}

@Immutable
data class LogEntry(
    val id: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val timestamp: Long,
    val timeFormatted: String,
    val throwableTrace: String?,
)

// ---------- Standard tags ----------

object LogTags {
    const val API = "API"
    const val REPO = "REPO"
    const val HOME_VM = "HOME_VM"
    const val DETAIL_VM = "DETAIL_VM"
    const val PLAYER_VM = "PLAYER_VM"
    const val SEARCH_VM = "SEARCH_VM"
    const val NAV = "NAV"
    const val UI = "UI"
    const val PLAYER = "PLAYER"
    const val WEBVIEW = "WEBVIEW"
    const val HILT = "HILT"
}
