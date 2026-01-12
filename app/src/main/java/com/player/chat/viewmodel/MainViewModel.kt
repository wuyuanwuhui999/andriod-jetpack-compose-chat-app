package com.player.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import com.player.chat.local.DataStoreManager
import com.player.chat.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
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

    fun isTokenValid(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        return try {
            val decoded = JWT.decode(token)
            val expiresAt = decoded.expiresAt
            expiresAt != null && expiresAt.after(Date())
        } catch (e: JWTVerificationException) {
            false // 无效的 JWT 格式
        } catch (e: Exception) {
            false // 其他异常也视为无效
        }
    }
}
