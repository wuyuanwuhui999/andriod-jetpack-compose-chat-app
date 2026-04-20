package com.player.chat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.player.chat.chat.repository.UserRepository
import com.player.chat.local.DataStoreManager
import com.player.chat.utils.CommonUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 注册 ViewModel
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * 注册用户
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
    ): Result<Boolean> {
        return try {
            Log.d("Register", "========== 开始注册 ==========")
            Log.d("Register", "账号: $userAccount")
            Log.d("Register", "用户名: $username")
            Log.d("Register", "密码(明文): $password")
            Log.d("Register", "电话: $telephone")
            Log.d("Register", "邮箱: $email")
            Log.d("Register", "性别: $sex")
            Log.d("Register", "出生日期: $birthday")
            Log.d("Register", "地区: $region")
            Log.d("Register", "个性签名: $sign")

            _isLoading.value = true

            // MD5加密密码
            val encryptedPassword = CommonUtils.md5(password)
            Log.d("Register", "密码(MD5加密后): $encryptedPassword")

            val result = userRepository.register(
                userAccount = userAccount,
                username = username,
                password = encryptedPassword,
                telephone = telephone,
                email = email,
                sex = sex,
                birthday = birthday,
                region = region,
                sign = sign
            )

            _isLoading.value = false

            if (result.isSuccess) {
                val pair = result.getOrNull()
                if (pair != null) {
                    val user = pair.first
                    val token = pair.second
                    Log.d("Register", "注册成功 - user: $user, token: $token")

                    // 保存用户数据和 token
                    if (user != null && token != null) {
                        dataStoreManager.saveUser(user)
                        dataStoreManager.saveToken(token)
                        dataStoreManager.setLoggedIn(true)
                        Log.d("Register", "用户数据和Token保存成功")
                        Result.success(true)
                    } else {
                        Log.e("Register", "用户数据为空: user=$user, token=$token")
                        Result.failure(Exception("用户数据为空"))
                    }
                } else {
                    Log.e("Register", "注册失败，返回结果为null")
                    Result.failure(Exception("注册失败"))
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "注册失败"
                Log.e("Register", "注册失败: $errorMsg")
                _errorMessage.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("Register", "注册异常", e)
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }

    // RegisterViewModel.kt - 添加 checkUserExists 方法

    /**
     * 校验用户是否存在（账号或邮箱）
     * @param userAccount 账号（可选）
     * @param email 邮箱（可选）
     * @return true 表示存在，false 表示不存在
     */
    suspend fun checkUserExists(userAccount: String?, email: String?): Boolean {
        return try {
            Log.d("Register", "========== 校验用户是否存在 ==========")
            Log.d("Register", "userAccount: $userAccount")
            Log.d("Register", "email: $email")

            val result = userRepository.checkUserExists(userAccount, email)

            if (result.isSuccess) {
                val data = result.getOrNull()
                val exists = data != null && data > 0
                Log.d("Register", "校验结果: ${if (exists) "存在" else "不存在"}")
                exists
            } else {
                Log.e("Register", "校验失败: ${result.exceptionOrNull()?.message}")
                false
            }
        } catch (e: Exception) {
            Log.e("Register", "校验异常", e)
            false
        }
    }
}