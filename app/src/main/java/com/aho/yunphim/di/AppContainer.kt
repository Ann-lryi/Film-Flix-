package com.aho.yunphim.di

import com.aho.yunphim.data.remote.NguonCApi
import com.aho.yunphim.data.remote.NguonCEndpoints
import com.aho.yunphim.data.repository.MovieRepository
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.net.InetAddress
import java.util.concurrent.TimeUnit

/**
 * DI thủ công (không Hilt) để giảm bề mặt lỗi build - KSP/kapt + version-matrix Hilt là nguồn
 * lỗi CI phổ biến nhất theo checklist build. Toàn bộ phụ thuộc là singleton `by lazy`, khởi tạo
 * 1 lần trong [com.aho.yunphim.YunPhimApp].
 */
class AppContainer {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * phim.nguonc.com đứng sau Cloudflare (đã xác nhận: fetch trực tiếp bị bot detection chặn).
     * CookieJar giữ cf_clearance qua các request trong cùng phiên để không bị thách thức lại
     * liên tục, DNS ưu tiên IPv4, User-Agent giả lập trình duyệt thật - tái dùng đúng pattern đã
     * chứng minh hiệu quả với chính domain này ở plugin CloudStream NguonC trước đây (không phải
     * suy đoán mới, mà áp lại giải pháp đã kiểm chứng).
     */
    private val cookieJar = object : CookieJar {
        private val store = mutableMapOf<String, List<Cookie>>()
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            store[url.host] = cookies
        }
        override fun loadForRequest(url: HttpUrl): List<Cookie> = store[url.host] ?: emptyList()
    }

    private val ipv4PreferringDns = Dns { hostname ->
        val all = Dns.SYSTEM.lookup(hostname)
        val v4Only = all.filter { it.address.size == 4 }
        v4Only.ifEmpty { all }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // BODY để log nguyên văn JSON trả về - cần thiết để đối chiếu với model đã giả định
        // khi test thật trên thiết bị, đây là build debug nên không phát hành nên log verbose
        // không phải vấn đề.
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .dns(ipv4PreferringDns)
            .cookieJar(cookieJar)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 14; Honor GT Pro) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36",
                    )
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(NguonCEndpoints.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val nguonCApi: NguonCApi by lazy { retrofit.create(NguonCApi::class.java) }

    val movieRepository: MovieRepository by lazy { MovieRepository(nguonCApi) }
}
