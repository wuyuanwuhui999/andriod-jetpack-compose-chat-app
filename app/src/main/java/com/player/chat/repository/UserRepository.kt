package com.player.chat.chat.repository

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

    /**
     * 重置密码
     * @param email 邮箱
     * @param code 验证码
     * @param password 新密码（已MD5加密）
     * @return Result<Pair<User?, String?>> 返回用户信息和token
     */
    suspend fun resetPassword(email: String, code: String, password: String): Result<Pair<User?, String?>> {
        return try {
            val request = ResetPasswordRequest(email, code, password)
            val response = apiService.resetPassword(request)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                val user = response.body()?.data
                val token = response.body()?.token
                Result.success(Pair(user, token))
            } else {
                Result.failure(Exception(response.body()?.message ?: "重置密码失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}