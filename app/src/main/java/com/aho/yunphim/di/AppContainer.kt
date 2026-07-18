package com.aho.yunphim.di

import com.aho.yunphim.data.remote.NguonCApi
import com.aho.yunphim.data.remote.NguonCEndpoints
import com.aho.yunphim.data.repository.MovieRepository
import com.aho.yunphim.ui.player.StreamcResolver
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.net.InetAddress
import java.net.URLEncoder
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
     * phim.nguonc.com đứng sau Cloudflare, NHƯNG theo source code plugin CloudStream NguonC
     * đang chạy thật của Aho: endpoint /api/... "usually doesn't block" request trực tiếp -
     * Cloudflare challenge chủ yếu chặn trang HTML cho người dùng thường, không chặn JSON API.
     * Vì vậy KHÔNG cần WebView/cf_clearance cho tầng API này. Vẫn giữ User-Agent giống trình
     * duyệt thật + DNS ưu tiên IPv4 (theo đúng pattern đã kiểm chứng hiệu quả với domain này) làm
     * lớp phòng thủ rẻ, và thêm 1 tầng fallback qua CORS proxy công khai nếu direct request thất
     * bại - port từ đúng cơ chế fallback plugin gốc đang dùng thật.
     */
    private val cookieJar = object : CookieJar {
        private val store = mutableMapOf<String, List<Cookie>>()
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            store[url.host] = cookies
        }
        override fun loadForRequest(url: HttpUrl): List<Cookie> = store[url.host] ?: emptyList()
    }

    // Dns KHÔNG phải "fun interface" trong OkHttp - lambda `Dns { hostname -> ... }` không hợp
    // lệ (đã xác nhận qua lỗi compile thật: "Interface 'interface Dns : Any' does not have
    // constructors"). Dùng object : Dns tường minh, cú pháp này luôn đúng bất kể interface có
    // phải fun interface hay không.
    private val ipv4PreferringDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            val all = Dns.SYSTEM.lookup(hostname)
            val v4Only = all.filter { it.address.size == 4 }
            return v4Only.ifEmpty { all }
        }
    }

    // User-Agent này copy nguyên văn từ plugin CloudStream NguonC đang chạy thật (đã kiểm chứng
    // hoạt động với domain này) - KHÔNG phải UA tự bịa như bản đầu.
    private val confirmedUserAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    private val userAgentInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .header("User-Agent", confirmedUserAgent)
                .build()
            return chain.proceed(request)
        }
    }

    /**
     * Nếu request trực tiếp lỗi (exception hoặc HTTP không thành công), thử lại đúng 1 lần qua
     * CORS proxy công khai - port từ cơ chế fallback đã chứng minh hiệu quả trong plugin gốc.
     * Chỉ áp dụng cho GET đơn giản (endpoint API của app này toàn bộ là GET).
     */
    private val corsProxyFallbackInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val direct = try {
                chain.proceed(original)
            } catch (_: Exception) {
                null
            }
            if (direct != null && direct.isSuccessful) return direct
            direct?.close()

            val encodedUrl = URLEncoder.encode(original.url.toString(), "UTF-8")
            val proxied = original.newBuilder()
                .url("https://api.allorigins.win/raw?url=$encodedUrl")
                .build()
            return chain.proceed(proxied)
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // BODY để log nguyên văn JSON trả về - cần thiết để đối chiếu với model khi test thật
        // trên thiết bị. Đây là build debug, không phát hành, nên log verbose không phải vấn đề.
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .dns(ipv4PreferringDns)
            .cookieJar(cookieJar)
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(corsProxyFallbackInterceptor)
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

    // Dùng chung okHttpClient (UA + DNS IPv4 + cookie jar) cho luồng resolve streamc.xyz.
    val streamcResolver: StreamcResolver by lazy { StreamcResolver(okHttpClient) }
}
