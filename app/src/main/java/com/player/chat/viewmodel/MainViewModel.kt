// MainViewModel.kt
package com.player.chat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.chat.repository.UserRepository
import com.player.chat.local.DataStoreManager
import com.player.chat.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    private val _isTokenValid = MutableStateFlow<Boolean?>(null)

    // 用于在 LaunchPage 中监听验证结果
    val isTokenValid: StateFlow<Boolean?> = _isTokenValid.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // 1. 获取缓存的用户数据
                val cachedUser = dataStoreManager.getUser().firstOrNull()
                _currentUser.value = cachedUser

                // 2. 获取缓存的 token
                var cachedToken = dataStoreManager.getToken().firstOrNull()

                // 测试：如果 token 为空，使用写死的 token
//                if (cachedToken.isNullOrBlank()) {
//                    cachedToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3Njk4NDc2ODQsInN1YiI6IntcImF2YXRlclwiOlwiL3N0YXRpYy91c2VyL2F2YXRlci_lkLTml7blkLTliLsuanBnXCIsXCJiaXJ0aGRheVwiOlwiMTk5MC0xMC04XCIsXCJjcmVhdGVEYXRlXCI6XCIyMDE5LTA4LTEyIDAwOjAwOjAwXCIsXCJkaXNhYmxlZFwiOjAsXCJlbWFpbFwiOlwiMjc1MDE4NzIzQHFxLmNvbVwiLFwiaWRcIjpcImY3MWQ2YzAxNmZhOTRjZDI5ZjlkYjUzZjcxZWM3YjYyXCIsXCJwZXJtaXNzaW9uXCI6MCxcInJvbGVcIjpcInB1YmxpY1wiLFwic2V4XCI6MCxcInNpZ25cIjpcIuaXoOaXtuaXoOWIu-S4jeaDs-S9oFwiLFwidGVsZXBob25lXCI6XCIxNTMwMjY4Njk0N1wiLFwidXBkYXRlRGF0ZVwiOlwiMjAyNC0wMS0xOSAyMzoxNzoyOVwiLFwidXNlckFjY291bnRcIjpcIuWQtOaXtuWQtOWIu1wiLFwidXNlcm5hbWVcIjpcIuWQtOaXtuWQtOWIu1wifSIsImV4cCI6MTc3MjQzOTY4NH0.g0TEfj9KeNWymjQvos64yw1ucB8bB41VlBIHSk9rV30"
//                    dataStoreManager.saveToken(cachedToken ?: "")
//                }

                _token.value = cachedToken

            } catch (e: Exception) {
                // 静默处理异常
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 验证 token 有效性（调用接口验证）
     */
    suspend fun validateToken(): Boolean {
        return try {
            // 从 StateFlow 获取当前 token
            val currentToken = _token.value

            if (currentToken.isNullOrBlank()) {
                _isTokenValid.value = false
                return false
            }

            // 调用接口验证 token
            val result = userRepository.getUserData()

            if (result.isSuccess) {
                val user = result.getOrNull()?.first
                val newToken = result.getOrNull()?.second

                // 更新用户和 token
                user?.let { updateUser(it) }
                newToken?.let { updateToken(it) }

                _isTokenValid.value = true
                true
            } else {
                _isTokenValid.value = false
                false
            }

        } catch (e: Exception) {
            _isTokenValid.value = false
            false
        }
    }

    /**
     * 本地检查 token 是否存在（不验证有效性）
     */
    fun hasTokenLocally(): Boolean {
        return !_token.value.isNullOrBlank()
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            dataStoreManager.saveUser(user)
            _currentUser.value = user
        }
    }

    fun updateToken(token: String) {
        viewModelScope.launch {
            dataStoreManager.saveToken(token)
            _token.value = token
        }
    }

    fun clearUserData() {
        viewModelScope.launch {
            dataStoreManager.clearAll()
            _currentUser.value = null
            _token.value = null
        }
    }
}