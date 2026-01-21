// AppModule.kt
package com.player.chat.di

import android.content.Context
import android.util.Log
import com.player.chat.chat.repository.UserRepository
import com.player.chat.local.DataStoreManager
import com.player.chat.network.ApiService
import com.player.chat.network.NetworkInterceptor
import com.player.chat.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.nio.Buffer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        networkInterceptor: NetworkInterceptor
    ): OkHttpClient {
        // 详细的请求响应日志拦截器
        val detailedInterceptor = DetailedLoggingInterceptor()

        return OkHttpClient.Builder()
            .addInterceptor(detailedInterceptor)  // 添加详细日志拦截器
            .addInterceptor(networkInterceptor)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.73.8:3000")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideNetworkInterceptor(dataStoreManager: DataStoreManager): NetworkInterceptor {
        return NetworkInterceptor(dataStoreManager)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        dataStoreManager: DataStoreManager
    ): UserRepository {
        return UserRepository(apiService, dataStoreManager)
    }

    @Provides
    @Singleton
    fun provideChatRepository(apiService: ApiService): ChatRepository {
        return ChatRepository(apiService)
    }
}

// 详细的日志拦截器
class DetailedLoggingInterceptor : Interceptor {
    companion object {
        private const val TAG = "NetworkLog"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // 打印请求信息
        logRequest(request)

        val startTime = System.nanoTime()
        var response: Response? = null
        var exception: Exception? = null

        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            exception = e
            throw e
        } finally {
            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1_000_000.0

            if (exception != null) {
                Log.e(TAG, "═══════════════════════════════════════════════════")
                Log.e(TAG, "❌ 请求失败: ${exception.message}")
                Log.e(TAG, "URL: ${request.url}")
                Log.e(TAG, "Method: ${request.method}")
                Log.e(TAG, "耗时: ${duration}ms")
                Log.e(TAG, "═══════════════════════════════════════════════════")
            } else if (response != null) {
                logResponse(response, duration)
            }
        }

        return response!!
    }

    private fun logRequest(request: Request) {
        Log.d(TAG, "═══════════════════════════════════════════════════")
        Log.d(TAG, "📤 发起请求")
        Log.d(TAG, "URL: ${request.url}")
        Log.d(TAG, "Method: ${request.method}")
        Log.d(TAG, "Headers:")
        request.headers.forEach { header ->
            Log.d(TAG, "  ${header.first}: ${header.second}")
        }

        // 打印请求体（如果有）
        val requestBody = request.body
        if (requestBody != null) {
            try {
                val buffer = okio.Buffer()
                requestBody.writeTo(buffer)
                val requestBodyString = buffer.readUtf8()
                Log.d(TAG, "Body: $requestBodyString")
            } catch (e: Exception) {
                Log.d(TAG, "Body: [无法读取请求体]")
            }
        }
        Log.d(TAG, "═══════════════════════════════════════════════════")
    }

    private fun logResponse(response: Response, duration: Double) {
        Log.d(TAG, "═══════════════════════════════════════════════════")
        Log.d(TAG, "📥 收到响应")
        Log.d(TAG, "URL: ${response.request.url}")
        Log.d(TAG, "状态码: ${response.code} ${response.message}")
        Log.d(TAG, "耗时: ${duration}ms")
        Log.d(TAG, "Headers:")
        response.headers.forEach { header ->
            Log.d(TAG, "  ${header.first}: ${header.second}")
        }

        // 复制响应体以便读取
        val responseBody = response.body
        val source = responseBody?.source()
        source?.request(Long.MAX_VALUE)
        val buffer = source?.buffer?.clone()

        try {
            val contentType = responseBody?.contentType()
            val content = buffer?.readUtf8()

            if (content != null && content.isNotBlank()) {
                Log.d(TAG, "Body: $content")
            } else {
                Log.d(TAG, "Body: [空]")
            }
        } catch (e: Exception) {
            Log.d(TAG, "Body: [无法读取响应体]")
        }

        Log.d(TAG, "═══════════════════════════════════════════════════")
    }
}