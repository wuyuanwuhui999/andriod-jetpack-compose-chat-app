package com.player.chat.chat.repository

import com.player.chat.local.DataStoreManager
import com.player.chat.network.ApiService
import com.player.chat.model.*
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
}