package com.player.chat.network

import com.player.chat.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    @GET("/service/tenant/getUserTenantList")
    suspend fun getUserTenantList(): Response<ApiResponse<List<Tenant>>>

    @GET("/service/chat/getDirectoryList")
    suspend fun getDirectoryList(@Query("tenantId") tenantId: String): Response<DirectoryListResponse>

    @POST("/service/chat/createDir")
    @Multipart
    suspend fun createDirectory(
        @Part("directory") directory: RequestBody,
        @Part("tenantId") tenantId: RequestBody
    ): Response<ApiResponse<Directory>>

    @POST("/service/chat/uploadDoc/{tenantId}/{directoryId}")
    @Multipart
    suspend fun uploadDocument(
        @Path("tenantId") tenantId: String,
        @Path("directoryId") directoryId: String,
        @Part file: MultipartBody.Part?
    ): Response<ApiResponse<Int>>

    // 获取目录下的文档列表
    @GET("/service/chat/getDocListByDirId")
    suspend fun getDocListByDirId(
        @Query("tenantId") tenantId: String,
        @Query("directoryId") directoryId: String
    ): Response<ApiResponse<List<Document>>>

    // 删除文档
    @POST("/service/chat/deleteDoc/{docId}")
    suspend fun deleteDocument(@Path("docId") docId: String): Response<ApiResponse<Int>>

}