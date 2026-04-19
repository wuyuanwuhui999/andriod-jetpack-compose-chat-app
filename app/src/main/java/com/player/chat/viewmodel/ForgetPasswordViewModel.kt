package com.player.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.chat.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 忘记密码 ViewModel
 */
@HiltViewModel
class ForgetPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * 发送验证码
     * @param email 邮箱地址
     * @return Result<String> 返回服务器消息
     */
    suspend fun sendVerificationCode(email: String): Result<String> {
        return try {
            _isLoading.value = true
            val result = userRepository.sendEmailVerifyCode(email)
            _isLoading.value = false

            if (result.isSuccess) {
                Result.success("验证码已发送")
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "发送失败"
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