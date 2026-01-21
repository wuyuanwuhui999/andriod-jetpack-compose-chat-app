// MainViewModel.kt
package com.player.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.chat.repository.UserRepository
import com.player.chat.local.DataStoreManager
import com.player.chat.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            dataStoreManager.getUser().collect { user ->
                _currentUser.value = user
            }
            dataStoreManager.getToken().collect { token ->
                _token.value = token
            }
            _isLoading.value = false
        }
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

    suspend fun getUserData(): Result<Pair<User, String?>> {
        return try {
            _isLoading.value = true
            val result = userRepository.getUserData()

            // 如果成功，数据已经通过 UserRepository 保存到 DataStore
            if (result.isSuccess) {
                // 触发重新加载数据到 StateFlow
                loadUserData()
            }
            _isLoading.value = false
            result
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(e)
        }
    }

    // 简化的 token 验证（只是检查是否存在）
    fun isTokenValid(token: String?): Boolean {
        return !token.isNullOrBlank()
    }
}