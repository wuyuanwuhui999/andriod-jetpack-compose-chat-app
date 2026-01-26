package com.player.chat.network

import com.player.chat.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // 账号密码登录
    @POST("/service/user/login")  // 注意：使用POST方法
    suspend fun loginByUserAccount(@Body request: AccountLoginRequest): Response<ApiResponse<User>>

    // 获取用户信息（验证token）
    @GET("/service/user/getUserData")
    suspend fun getUserData(): Response<ApiResponse<User>>

    // 邮箱登录
    @POST("/service/user/loginByEmail")
    suspend fun loginByEmail(@Body request: EmailLoginRequest): Response<ApiResponse<User>>

    // 发送验证码
    @POST("/service/user/sendEmailVertifyCode")
    suspend fun sendEmailVerifyCode(@Body request: SendEmailRequest): Response<ApiResponse<Unit>>

    @GET("/service/chat/getModelList")
    suspend fun getModelList(): Response<ApiResponse<List<ChatModel>>>

    @GET("/service/tenant/getTenantUserList")
    suspend fun getTenantUserList(): Response<TenantUserResponse>

}