package com.player.chat.network

import com.player.chat.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // 获取用户信息（验证token）
    @GET("/service/user/getUserData")
    suspend fun getUserData(): Response<ApiResponse<User>>

    // 邮箱登录
    @POST("/service/user/loginByEmail")
    suspend fun loginByEmail(@Body request: EmailLoginRequest): Response<ApiResponse<User>>

    // 发送验证码
    @POST("/service/user/sendEmailVertifyCode")
    suspend fun sendEmailVerifyCode(@Body request: SendEmailRequest): Response<ApiResponse<Unit>>

    // 后续可以添加其他接口...
}