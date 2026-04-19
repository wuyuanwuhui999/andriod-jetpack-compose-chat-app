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

    // 发送验证码 - 修改返回类型
    @POST("/service/user/sendEmailVertifyCode")
    suspend fun sendEmailVerifyCode(@Body request: SendEmailRequest): Response<ApiResponse<Int>>

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

    @GET("/service/chat/getChatHistory")
    suspend fun getChatHistory(
        @Query("tenantId") tenantId: String,
        @Query("pageSize") pageSize: Int,
        @Query("pageNum") pageNum: Int
    ): Response<ApiResponse<List<ChatHistory>>>

    @GET("/service/tenant/getTenantUserList")
    suspend fun getTenantUserList(
        @Query("tenantId") tenantId: String,
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int
    ): Response<ApiResponse<List<TenantUser>>>

    // 获取提示词
    @GET("/service/prompt/getPrompt")
    suspend fun getPrompt(
        @Query("tenantId") tenantId: String
    ): Response<ApiResponse<Prompt>>

    // 更新提示词
    @POST("/service/prompt/updatePrompt")
    suspend fun updatePrompt(
        @Body request: UpdatePromptRequest
    ): Response<ApiResponse<Any>>

    /**
     * 更新用户头像
     * @param file 头像文件
     */
    @POST("/service/user/updateAvater")
    @Multipart
    suspend fun updateAvatar(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<String>>

    /**
     * 更新用户信息
     * @param user 用户对象
     */
    @POST("/service/user/updateUser")
    suspend fun updateUser(
        @Body user: User
    ): Response<ApiResponse<Any>>

    /**
     * 修改密码
     * @param request 修改密码请求
     */
    @POST("/service/user/updatePassword")
    suspend fun updatePassword(
        @Body request: UpdatePasswordRequest
    ): Response<ApiResponse<Int>>

    /**
     * 重置密码
     * @param request 重置密码请求
     */
    @POST("/service/user/resetPassword")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiResponse<User>>
}