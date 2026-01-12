package com.player.chat.network

import android.content.Context
import com.player.chat.local.DataStoreManager
import okhttp3.Interceptor

import okhttp3.Response
import javax.inject.Inject

class NetworkInterceptor @Inject constructor(
    private val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // 添加公共请求头
        requestBuilder.addHeader("Content-Type", "application/json")
        requestBuilder.addHeader("Accept", "application/json")

        return chain.proceed(requestBuilder.build())
    }
}

class AuthInterceptor @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): Response {
        val token = dataStoreManager.getToken().firstOrNull()
        val requestBuilder = chain.request().newBuilder()

        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        return chain.proceed(requestBuilder.build())
    }
}