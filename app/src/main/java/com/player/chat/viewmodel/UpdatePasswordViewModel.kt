// UpdatePasswordViewModel.kt
package com.player.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.chat.repository.UserRepository
import com.player.chat.local.DataStoreManager
import com.player.chat.utils.CommonUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 修改密码 ViewModel
 */
@HiltViewModel
class UpdatePasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    /**
     * 更新密码
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @return 是否修改成功
     */
    suspend fun updatePassword(oldPassword: String, newPassword: String): Boolean {
        return try {
            // MD5加密
            val encryptedOldPassword = CommonUtils.md5(oldPassword)
            val encryptedNewPassword = CommonUtils.md5(newPassword)

            val result = userRepository.updatePassword(encryptedOldPassword, encryptedNewPassword)

            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data != null && data > 0) {
                    _isSuccess.value = true
                    _errorMessage.value = null
                    true
                } else {
                    _errorMessage.value = "密码修改失败，请检查旧密码是否正确"
                    false
                }
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "密码修改失败"
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "网络错误，请稍后重试"
            false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}