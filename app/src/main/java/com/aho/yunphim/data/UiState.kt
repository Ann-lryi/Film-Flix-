package com.aho.yunphim.data

/**
 * Trạng thái UI dùng chung cho mọi màn hình. [Error.isSchemaMismatch] tách riêng lỗi mạng
 * (server không phản hồi, timeout...) khỏi lỗi parse JSON (response không khớp model đã giả
 * định) - để khi test thật, biết ngay lỗi nằm ở tầng nào thay vì đoán mò.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(
        val message: String,
        val isSchemaMismatch: Boolean = false,
    ) : UiState<Nothing>
}
