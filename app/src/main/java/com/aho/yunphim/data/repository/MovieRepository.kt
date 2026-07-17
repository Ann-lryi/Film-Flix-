package com.aho.yunphim.data.repository

import com.aho.yunphim.data.UiState
import com.aho.yunphim.data.model.MovieDetail
import com.aho.yunphim.data.model.MovieSummary
import com.aho.yunphim.data.remote.NguonCApi
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

data class MoviePage(
    val items: List<MovieSummary>,
    val currentPage: Int,
    val lastPage: Int,
)

class MovieRepository(private val api: NguonCApi) {

    suspend fun fetchList(path: String, page: Int): UiState<MoviePage> = safeCall {
        val response = api.getMovieList(path, page)
        val pagination = response.effectivePagination
        MoviePage(
            items = response.items,
            currentPage = pagination?.page ?: page,
            lastPage = pagination?.lastPage ?: page,
        )
    }

    suspend fun fetchDetail(slug: String): UiState<MovieDetail> = safeCall {
        val response = api.getMovieDetail(slug)
        response.movie ?: throw IllegalStateException("Response JSON không có field 'movie'.")
    }

    suspend fun search(keyword: String, page: Int): UiState<MoviePage> = safeCall {
        val response = api.search(keyword, page)
        val pagination = response.effectivePagination
        MoviePage(
            items = response.items,
            currentPage = pagination?.page ?: page,
            lastPage = pagination?.lastPage ?: page,
        )
    }

    /**
     * inline + tham số kiểu `suspend () -> T` để lời gọi suspend bên trong [block] được inline
     * thẳng vào hàm suspend gọi nó (fetchList/fetchDetail/search) - không cần đánh dấu safeCall
     * là suspend riêng. CancellationException phải được ném lại nguyên vẹn, không được nuốt,
     * nếu không sẽ phá vỡ structured concurrency khi coroutine bị huỷ (vd. rời màn hình giữa
     * lúc đang tải).
     */
    private inline fun <T> safeCall(block: suspend () -> T): UiState<T> {
        return try {
            UiState.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: SerializationException) {
            UiState.Error(
                message = "JSON trả về không khớp model đã giả định (schema mismatch). " +
                    "Xem Logcat tag OkHttp lấy JSON thật, đối chiếu NguonCApi.kt/MovieModels.kt.",
                isSchemaMismatch = true,
            )
        } catch (e: HttpException) {
            UiState.Error(message = "Server trả lỗi HTTP ${e.code()}: ${e.message()}")
        } catch (e: IOException) {
            UiState.Error(message = "Lỗi mạng: ${e.message ?: e::class.simpleName}")
        } catch (e: IllegalStateException) {
            UiState.Error(
                message = e.message ?: "Dữ liệu trả về thiếu field bắt buộc.",
                isSchemaMismatch = true,
            )
        } catch (e: Exception) {
            UiState.Error(message = "Lỗi không xác định: ${e.message ?: e::class.simpleName}")
        }
    }
}
