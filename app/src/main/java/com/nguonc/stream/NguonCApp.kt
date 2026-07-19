package com.nguonc.stream

import android.app.Application
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltAndroidApp
class NguonCApp : Application() {

    override fun onCreate() {
        super.onCreate()
        installCrashLogger()
    }

    /**
     * Ghi log crash ra file TRƯỚC KHI để crash xảy ra như bình thường — không nuốt exception,
     * chỉ chain thêm bước ghi log rồi vẫn gọi handler mặc định của hệ thống (app vẫn dừng/khởi
     * động lại đúng hành vi chuẩn của Android). Trước đây app không có bất kỳ cơ chế log crash
     * nào nên không có cách xác định nguyên nhân cụ thể khi thoát bất thường; log này giúp debug
     * bằng cách đọc file qua Shizuku/adb (adb pull /data/data/com.nguonc.stream/files/crash_logs).
     */
    private fun installCrashLogger() {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching { writeCrashLog(thread, throwable) }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun writeCrashLog(thread: Thread, throwable: Throwable) {
        val dir = File(filesDir, "crash_logs").apply { mkdirs() }
        // Chỉ giữ 10 log gần nhất — tránh phình dung lượng vô hạn qua thời gian
        dir.listFiles()
            ?.sortedBy { it.lastModified() }
            ?.dropLast(9)
            ?.forEach { it.delete() }

        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val file = File(dir, "crash_$timestamp.txt")
        runCatching {
            file.writeText(
                buildString {
                    appendLine("Thời điểm: $timestamp")
                    appendLine("Thread: ${thread.name}")
                    appendLine(
                        "Thiết bị: ${Build.MANUFACTURER} ${Build.MODEL} " +
                            "(Android ${Build.VERSION.RELEASE}, SDK ${Build.VERSION.SDK_INT})"
                    )
                    appendLine("App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    appendLine()
                    appendLine(throwable.stackTraceToString())
                }
            )
        }
    }
}
