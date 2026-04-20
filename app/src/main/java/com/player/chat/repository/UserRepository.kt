package com.player.chat.chat.repository

import android.util.Log
import com.player.chat.local.DataStoreManager
import com.player.chat.network.ApiService
import com.player.chat.model.*
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) {
    suspend fun loginByUserAccount(userAccount: String, password: String): Result<Pair<User, String?>> {
        return try {
            val request = AccountLoginRequest(userAccount, password)
            val response = apiService.loginByUserAccount(request)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                val user = response.body()?.data
                val token = response.body()?.token
                if (user != null) {
                    dataStoreManager.saveUser(user)
                    token?.let { dataStoreManager.saveToken(it) }
                    dataStoreManager.setLoggedIn(true)
                    Result.success(Pair(user, token))
                } else {
                    Result.failure(Exception("Login failed"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserData(): Result<Pair<User, String?>> {
        return try {
            val response = apiService.getUserData()
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                val user = response.body()?.data
                val token = response.body()?.token
                if (user != null) {
                    dataStoreManager.saveUser(user)
                    token?.let { dataStoreManager.saveToken(it) }
                    Result.success(Pair(user, token))
                } else {
                    Result.failure(Exception("User data is null"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Request failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginByEmail(email: String, code: String): Result<Pair<User, String?>> {
        return try {
            val request = EmailLoginRequest(email, code)
            val response = apiService.loginByEmail(request)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                val user = response.body()?.data
                val token = response.body()?.token
                if (user != null) {
                    dataStoreManager.saveUser(user)
                    token?.let { dataStoreManager.saveToken(it) }
                    dataStoreManager.setLoggedIn(true)
                    Result.success(Pair(user, token))
                } else {
                    Result.failure(Exception("Login failed"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendEmailVerifyCode(email: String): Result<Unit> {
        return try {
            val request = SendEmailRequest(email)
            val response = apiService.sendEmailVerifyCode(request)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to send code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        dataStoreManager.clearAll()
    }

    suspend fun getUserTenantList(): Result<List<Tenant>> {
        return try {
            val response = apiService.getUserTenantList()
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "获取租户列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 获取缓存的租户ID
    suspend fun getCachedTenantId(): String? {
        // 这里假设使用DataStore存储租户ID
        // 需要先在DataStoreManager中添加相关方法
        // 为简化，这里直接返回null
        return null
    }

    /**
     * 更新用户头像
     * @param file 头像文件
     * @return Result<String> 返回头像的相对路径
     */
    suspend fun updateAvatar(file: File): Result<String> {
        return try {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = apiService.updateAvatar(filePart)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                val avatarPath = response.body()?.data
                if (!avatarPath.isNullOrBlank()) {
                    // 更新本地缓存的用户头像
                    val currentUser = dataStoreManager.getUser().firstOrNull()
                    currentUser?.let { user ->
                        val updatedUser = user.copy(avatar = avatarPath)
                        dataStoreManager.saveUser(updatedUser)
                    }
                    Result.success(avatarPath)
                } else {
                    Result.failure(Exception("头像上传成功但返回路径为空"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "头像上传失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 更新用户信息
     * @param user 更新后的用户对象
     * @return Result<Boolean> 是否更新成功
     */
    suspend fun updateUser(user: User): Result<Boolean> {
        return try {
            val response = apiService.updateUser(user)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                // 更新本地缓存
                dataStoreManager.saveUser(user)
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.message ?: "更新用户信息失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 修改密码
     * @param oldPassword 旧密码（已MD5加密）
     * @param newPassword 新密码（已MD5加密）
     * @return Result<Int> 返回影响行数
     */
    suspend fun updatePassword(oldPassword: String, newPassword: String): Result<Int> {
        return try {
            val request = UpdatePasswordRequest(oldPassword, newPassword)
            val response = apiService.updatePassword(request)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                val data = response.body()?.data ?: 0
                if (data > 0) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("密码修改失败，请检查旧密码是否正确"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "密码修改失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 在 UserRepository.kt 中添加 resetPassword 方法并打印日志

    /**
     * 重置密码
     * @param email 邮箱
     * @param code 验证码
     * @param password 新密码（已MD5加密）
     * @return Result<Pair<User?, String?>> 返回用户信息和token
     */
    suspend fun resetPassword(email: String, code: String, password: String): Result<Pair<User?, String?>> {
        return try {
            Log.d("UserRepository", "========== 调用重置密码接口 ==========")
            Log.d("UserRepository", "请求地址: /service/user/resetPassword")
            Log.d("UserRepository", "请求参数 - email: $email")
            Log.d("UserRepository", "请求参数 - code: $code")
            Log.d("UserRepository", "请求参数 - password: $password")

            val request = ResetPasswordRequest(email, code, password)

            // 打印完整请求参数
            val gson = com.google.gson.Gson()
            val requestJson = gson.toJson(request)
            Log.d("UserRepository", "请求参数JSON: $requestJson")

            val response = apiService.resetPassword(request)

            Log.d("UserRepository", "响应状态码: ${response.code()}")
            Log.d("UserRepository", "响应是否成功: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("UserRepository", "响应body: ${gson.toJson(body)}")
                Log.d("UserRepository", "响应status: ${body?.status}")
                Log.d("UserRepository", "响应msg: ${body?.message}")
                Log.d("UserRepository", "响应data: ${body?.data}")
                Log.d("UserRepository", "响应token: ${body?.token}")

                if (body?.status == "SUCCESS") {
                    val user = body.data
                    val token = body.token
                    Log.d("UserRepository", "重置密码成功 - user: $user, token: $token")
                    Result.success(Pair(user, token))
                } else {
                    val errorMsg = body?.message ?: "重置密码失败"
                    Log.e("UserRepository", "重置密码失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepository", "网络请求失败 - 状态码: ${response.code()}, 错误信息: $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "重置密码异常", e)
            Result.failure(e)
        }
    }

    /**
     * 注册用户
     * @param userAccount 账号
     * @param username 用户名
     * @param password 密码（已MD5加密）
     * @param telephone 电话
     * @param email 邮箱
     * @param sex 性别 0-男 1-女
     * @param birthday 出生日期
     * @param region 地区
     * @param sign 个性签名
     * @return Result<Pair<User?, String?>> 返回用户信息和token
     */
    suspend fun register(
        userAccount: String,
        username: String,
        password: String,
        telephone: String,
        email: String,
        sex: Int,
        birthday: String,
        region: String,
        sign: String
    ): Result<Pair<User?, String?>> {
        return try {
            Log.d("UserRepository", "========== 调用注册接口 ==========")
            Log.d("UserRepository", "请求地址: /service/user/register")

            val user = User(
                id = "",
                userAccount = userAccount,
                createDate = "",
                updateDate = "",
                username = username,
                telephone = telephone,
                email = email,
                avatar = null,
                birthday = birthday.ifEmpty { null },
                sex = sex,
                role = "",
                password = password,
                sign = sign.ifEmpty { null },
                region = region.ifEmpty { null },
                disabled = 0,
                permission = 0
            )

            val gson = com.google.gson.Gson()
            val requestJson = gson.toJson(user)
            Log.d("UserRepository", "请求参数JSON: $requestJson")

            val response = apiService.register(user)

            Log.d("UserRepository", "响应状态码: ${response.code()}")
            Log.d("UserRepository", "响应是否成功: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("UserRepository", "响应body: ${gson.toJson(body)}")
                Log.d("UserRepository", "响应status: ${body?.status}")
                Log.d("UserRepository", "响应msg: ${body?.message}")
                Log.d("UserRepository", "响应data: ${body?.data}")
                Log.d("UserRepository", "响应token: ${body?.token}")

                if (body?.status == "SUCCESS") {
                    val registeredUser = body.data
                    val token = body.token
                    Log.d("UserRepository", "注册成功 - user: $registeredUser, token: $token")
                    Result.success(Pair(registeredUser, token))
                } else {
                    val errorMsg = body?.message ?: "注册失败"
                    Log.e("UserRepository", "注册失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepository", "网络请求失败 - 状态码: ${response.code()}, 错误信息: $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "注册异常", e)
            Result.failure(e)
        }
    }

    // UserRepository.kt - 添加 checkUserExists 方法

    /**
     * 校验用户是否存在（账号或邮箱）
     * @param userAccount 账号（可选）
     * @param email 邮箱（可选）
     * @return Result<Int> 返回存在数量，>0表示存在
     */
    suspend fun checkUserExists(userAccount: String?, email: String?): Result<Int> {
        return try {
            Log.d("UserRepository", "========== 调用校验用户接口 ==========")
            Log.d("UserRepository", "请求地址: /service/user/vertifyUser")

            val request = VerifyUserRequest(userAccount, email)
            val gson = com.google.gson.Gson()
            Log.d("UserRepository", "请求参数JSON: ${gson.toJson(request)}")

            val response = apiService.verifyUser(request)

            Log.d("UserRepository", "响应状态码: ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("UserRepository", "响应body: ${gson.toJson(body)}")

                if (body?.status == "SUCCESS") {
                    val data = body.data ?: 0
                    Result.success(data)
                } else {
                    Result.failure(Exception(body?.message ?: "校验失败"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepository", "网络请求失败 - 状态码: ${response.code()}, 错误信息: $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "校验异常", e)
            Result.failure(e)
        }
    }
}