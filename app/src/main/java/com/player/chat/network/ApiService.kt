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
    suspend fun getModelList(@Query("companyId") companyId: String? = null): Response<ApiResponse<List<ChatModel>>>

    @GET("/service/tenant/getTenantList")
    suspend fun getTenantList(
        @Query("companyId") companyId: String  // 改为必传参数
    ): Response<ApiResponse<List<Tenant>>>

    @GET("/service/chat/getDirectoryList")
    suspend fun getDirectoryList(@Query("tenantId") tenantId: String): Response<ApiResponse<List<Directory>>>

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

    /**
     * 用户注册
     * @param user 用户信息
     */
    @POST("/service/user/register")
    suspend fun register(
        @Body user: User
    ): Response<ApiResponse<User>>

    /**
     * 校验用户是否存在（账号或邮箱）
     * @param request 校验请求
     */
    @POST("/service/user/vertifyUser")
    suspend fun verifyUser(
        @Body request: VerifyUserRequest
    ): Response<ApiResponse<Int>>

    /**
     * 获取租户用户信息（当前用户在该租户下的信息）
     * @param tenantId 租户ID
     * @return 单个租户用户信息对象
     */
    @GET("/service/tenant/getTenantUser")
    suspend fun getTenantUser(
        @Query("tenantId") tenantId: String?
    ): Response<ApiResponse<TenantUserInfo>>  // 改为单个对象，不是列表

    /**
     * 删除租户用户
     * @param tenantId 租户ID
     * @param userId 用户ID
     */
    @DELETE("/service/tenant/deleteTenantUser/{tenantId}/{userId}")
    suspend fun deleteTenantUser(
        @Path("tenantId") tenantId: String,
        @Path("userId") userId: String
    ): Response<ApiResponse<Int>>

    /**
     * 搜索用户（按关键字）
     * @param keyword 搜索关键字
     * @param tenantId 租户ID（用于过滤已在租户内的用户）
     * @param pageNum 页码
     * @param pageSize 每页数量
     */
    @GET("/service/user/searchUsers")
    suspend fun searchUsers(
        @Query("keyword") keyword: String,
        @Query("tenantId") tenantId: String,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 100
    ): Response<ApiResponse<List<SearchUser>>>

    /**
     * 添加租户用户
     * @param tenantId 租户ID
     * @param userId 用户ID
     */
    @POST("/service/tenant/addTenantUser/{tenantId}/{userId}")
    suspend fun addTenantUser(
        @Path("tenantId") tenantId: String,
        @Path("userId") userId: String
    ): Response<ApiResponse<Int>>

    /**
    * 获取公司列表
    */
    @GET("/service/company/getCompanyList")
    suspend fun getCompanyList(): Response<ApiResponse<List<Company>>>

    /**
     * 获取公司下的用户列表
     */
    @GET("/service/company/getCompanyUsers")
    suspend fun getCompanyUsers(
        @Query("companyId") companyId: String,
        @Query("pageSize") pageSize: Int,
        @Query("pageNum") pageNum: Int,
        @Query("keyword") keyword: String? = null
    ): Response<ApiResponse<List<User>>>

    /**
     * 删除公司用户
     */
    @DELETE("/service/company/removeUser/{userId}/{companyId}")
    suspend fun removeUser(
        @Path("userId") userId: String,
        @Path("companyId") companyId: String
    ): Response<ApiResponse<Int>>

    /**
     * 搜索用户（带公司过滤）
     */
    @GET("/service/user/searchUsers")
    suspend fun searchUsersWithCompany(
        @Query("companyId") companyId: String,
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int
    ): Response<ApiResponse<List<SearchUser>>>

    /**
     * 获取部门列表
     */
    @GET("/service/company/getDepartments")
    suspend fun getDepartments(
        @Query("companyId") companyId: String
    ): Response<ApiResponse<List<Department>>>

    /**
     * 获取职位列表
     */
    @GET("/service/company/getPositions")
    suspend fun getPositions(
        @Query("departmentId") departmentId: String
    ): Response<ApiResponse<List<Position>>>

    /**
     * 添加公司用户
     */
    @POST("/service/company/addUser")
    suspend fun addCompanyUser(
        @Body request: AddCompanyUserRequest
    ): Response<ApiResponse<Int>>
}