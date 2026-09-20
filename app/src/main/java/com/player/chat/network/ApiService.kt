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

    /**
     * 获取模型列表
     */
    @GET("/service/chat/getModelList")
    suspend fun getModelList(
        @Query("companyId") companyId: String,
        @Query("keyword") keyword: String? = null
    ): Response<ApiResponse<List<ChatModel>>>

    /**
     * 添加模型
     */
    @POST("/service/chat/addModel")
    suspend fun addModel(
        @Body request: AddModelRequest
    ): Response<ApiResponse<Int>>

    /**
     * 更新模型
     */
    @PUT("/service/chat/updateModel")
    suspend fun updateModel(
        @Body request: UpdateModelRequest
    ): Response<ApiResponse<Int>>

    /**
     * 删除模型
     */
    @DELETE("/service/chat/deleteModel/{modelId}")
    suspend fun deleteModel(
        @Path("modelId") modelId: String
    ): Response<ApiResponse<Int>>

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

    /**
     * 上传文档
     * 说明：tenantId、directoryId 已从 URL 路径（{tenantId}/{directoryId}）改为请求体字段，
     * 与 splitMethod、chunkSize、permission 一起以 multipart 表单形式提交。
     *
     * @param tenantId 租户ID
     * @param directoryId 目录ID
     * @param splitMethod 分割方式：recursive / paragraph / sentence / fixed
     * @param chunkSize 分割大小，仅当 splitMethod = fixed 时后端生效，默认 1000
     * @param permission 文档权限：private-私密 / tenant-租户内公开 / company-公司内公开
     * @param file 上传的文件
     */
    @POST("/service/chat/uploadDoc")
    @Multipart
    suspend fun uploadDocument(
        @Part("tenantId") tenantId: RequestBody,
        @Part("directoryId") directoryId: RequestBody,
        @Part("splitMethod") splitMethod: RequestBody,
        @Part("chunkSize") chunkSize: RequestBody,
        @Part("permission") permission: RequestBody,
        @Part file: MultipartBody.Part?
    ): Response<ApiResponse<Int>>

    // 获取目录下的文档列表
    @GET("/service/chat/getDocListByDirId")
    suspend fun getDocListByDirId(
        @Query("tenantId") tenantId: String,
        @Query("directoryId") directoryId: String
    ): Response<ApiResponse<List<Document>>>

    /**
     * 删除文档
     * @param docId 文档ID（路径参数）
     * 返回 data > 0 表示删除成功
     */
    @DELETE("/service/chat/deleteDoc/{docId}")
    suspend fun deleteDocument(@Path("docId") docId: String): Response<ApiResponse<Int>>

    /**
     * 修改文档权限
     * @param docId 文档ID（路径参数）
     * @param request 请求体，包含 permission：private-私密 / tenant-租户内公开 / company-公司内公开
     * 返回 data > 0 表示修改成功，msg 为后端提示语
     */
    @PUT("/service/chat/updateDocPermission/{docId}")
    suspend fun updateDocPermission(
        @Path("docId") docId: String,
        @Body request: UpdateDocPermissionRequest
    ): Response<ApiResponse<Int>>

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
    // 获取提示词 - 修改，添加 promptId 参数
    @GET("/service/prompt/getPrompt")
    suspend fun getPrompt(
        @Query("tenantId") tenantId: String,
        @Query("promptId") promptId: String? = null
    ): Response<ApiResponse<Prompt>>

    // 获取提示词列表
    @GET("/service/prompt/getPromptList")
    suspend fun getPromptList(
        @Query("tenantId") tenantId: String,
        @Query("keyword") keyword: String? = null
    ): Response<ApiResponse<List<Prompt>>>

    // 新增提示词
    @POST("/service/prompt/insertPrompt")
    suspend fun insertPrompt(
        @Body request: InsertPromptRequest
    ): Response<ApiResponse<Int>>

    // 更新提示词
    @PUT("/service/prompt/updatePrompt")
    suspend fun updatePrompt(
        @Body request: UpdatePromptRequest
    ): Response<ApiResponse<Int>>

    // 删除提示词
    @DELETE("/service/prompt/deletePrompt/{promptId}")
    suspend fun deletePrompt(
        @Path("promptId") promptId: String
    ): Response<ApiResponse<Int>>

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
    @GET("/service/company/searchUsers")
    suspend fun searchUsersWithCompany(
        @Query("companyId") companyId: String,
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int,
        @Query("keyword") keyword: String? = null
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

    /**
     * 取消管理员
     * @param tenantId 租户ID
     * @param userId 用户ID
     */
    @PUT("/service/tenant/cancelAdmin/{tenantId}/{userId}")
    suspend fun cancelAdmin(
        @Path("tenantId") tenantId: String,
        @Path("userId") userId: String
    ): Response<ApiResponse<Int>>

    /**
     * 设为管理员
     * @param tenantId 租户ID
     * @param userId 用户ID
     */
    @PUT("/service/tenant/addAdmin/{tenantId}/{userId}")
    suspend fun addAdmin(
        @Path("tenantId") tenantId: String,
        @Path("userId") userId: String
    ): Response<ApiResponse<Int>>

    /**
     * 搜索用户（按关键字，用于租户添加）
     * @param keyword 搜索关键字
     * @param tenantId 租户ID（用于过滤已在租户内的用户）
     * @param companyId 公司ID（必填）
     * @param pageNum 页码
     * @param pageSize 每页数量
     */
    @GET("/service/tenant/searchTenantUsers")
    suspend fun searchTenantUsers(
        @Query("tenantId") tenantId: String,
        @Query("keyword") keyword: String,
        @Query("companyId") companyId: String,  // 新增：公司ID，必填
        @Query("pageSize") pageSize: Int,
        @Query("pageNum") pageNum: Int
    ): Response<ApiResponse<List<SearchUser>>>

    /**
     * 获取提示词列表（支持分页和关键字搜索）
     * @param tenantId 租户ID
     * @param keyword 搜索关键字（可选）
     * @param pageSize 每页数量
     * @param pageNum 页码
     */
    @GET("/service/prompt/getPromptList")
    suspend fun getPromptList(
        @Query("tenantId") tenantId: String,
        @Query("keyword") keyword: String? = null,
        @Query("pageSize") pageSize: Int = 20,
        @Query("pageNum") pageNum: Int = 1
    ): Response<ApiResponse<List<Prompt>>>

    /**
     * 删除提示词（新接口，带tenantId）
     */
    @DELETE("/service/prompt/deletePrompt/{promptId}/{tenantId}")
    suspend fun deletePromptWithTenant(
        @Path("promptId") promptId: String,
        @Path("tenantId") tenantId: String
    ): Response<ApiResponse<Int>>
}