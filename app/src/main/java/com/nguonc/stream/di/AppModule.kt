package com.nguonc.stream.di

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.nguonc.stream.data.local.AppDatabase
import com.nguonc.stream.data.local.FavoriteDao
import com.nguonc.stream.data.local.HistoryDao
import com.nguonc.stream.data.remote.NguoncApi
import com.nguonc.stream.data.remote.PhimApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

// Qualifier để phân biệt 2 Retrofit instance (nếu cần dùng sau này)
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PhimApiClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NguoncApiClient

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true       // API hay thêm field mới — tuyệt đối không crash
        coerceInputValues = true       // null/kiểu sai -> dùng default thay vì throw
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            // Một số endpoint /v1/api chặn request không có User-Agent
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/126.0 Mobile Safari/537.36"
                    )
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun providePhimApi(client: OkHttpClient, json: Json): PhimApi =
        Retrofit.Builder()
            .baseUrl(PhimApi.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PhimApi::class.java)

    /**
     * NguoncApi — dùng base URL https://phim.nguonc.com/api/
     * Dùng chung OkHttpClient + Json với PhimApi.
     */
    @Provides
    @Singleton
    fun provideNguoncApi(client: OkHttpClient, json: Json): NguoncApi =
        Retrofit.Builder()
            .baseUrl(NguoncApi.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NguoncApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "nguonc_stream.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao()
}
