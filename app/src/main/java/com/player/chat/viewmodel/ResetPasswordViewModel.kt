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
 * 重置密码 ViewModel
 */
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * 重置密码
     * @param email 邮箱
     * @param code 验证码
     * @param newPassword 新密码（明文）
     * @return Result<Boolean> 是否重置成功
     */
    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Boolean> {
        return try {

            _isLoading.value = true

            // MD5加密新密码
            val encryptedPassword = CommonUtils.md5(newPassword)
            Log.d("ResetPassword", "新密码(MD5加密后): $encryptedPassword")

            val result = userRepository.resetPassword(email, code, encryptedPassword)

            _isLoading.value = false

            if (result.isSuccess) {
                val pair = result.getOrNull()
                if (pair != null) {
                    val user = pair.first
                    val token = pair.second

                    // 保存用户数据和 token
                    if (user != null && token != null) {
                        dataStoreManager.saveUser(user)
                        dataStoreManager.saveToken(token)
                        dataStoreManager.setLoggedIn(true)
                        Result.success(true)
                    } else {
                        Result.failure(Exception("用户数据为空"))
                    }
                } else {
                    Result.failure(Exception("重置密码失败"))
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "重置密码失败"
                _errorMessage.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            Result.failure(e)
        }
    }
}