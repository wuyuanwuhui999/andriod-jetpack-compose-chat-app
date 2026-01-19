package com.player.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.chat.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _emailCodeState = MutableStateFlow<EmailCodeState>(EmailCodeState.Idle)
    val emailCodeState: StateFlow<EmailCodeState> = _emailCodeState

    // 添加账号密码登录方法
    fun loginByUserAccount(userAccount: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = userRepository.loginByUserAccount(userAccount, password)
            _loginState.value = if (result.isSuccess) {
                LoginState.Success(result.getOrNull()?.first)
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "登录失败")
            }
        }
    }

    fun loginByEmail(email: String, code: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = userRepository.loginByEmail(email, code)
            _loginState.value = if (result.isSuccess) {
                LoginState.Success(result.getOrNull()?.first)
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun sendEmailCode(email: String) {
        viewModelScope.launch {
            _emailCodeState.value = EmailCodeState.Loading
            val result = userRepository.sendEmailVerifyCode(email)
            _emailCodeState.value = if (result.isSuccess) {
                EmailCodeState.Success
            } else {
                EmailCodeState.Error(result.exceptionOrNull()?.message ?: "Failed to send code")
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    fun resetEmailCodeState() {
        _emailCodeState.value = EmailCodeState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: com.player.chat.model.User?) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class EmailCodeState {
    object Idle : EmailCodeState()
    object Loading : EmailCodeState()
    object Success : EmailCodeState()
    data class Error(val message: String) : EmailCodeState()
}