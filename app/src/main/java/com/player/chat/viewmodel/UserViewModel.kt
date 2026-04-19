package com.player.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.chat.repository.UserRepository
import com.player.chat.local.DataStoreManager
import com.player.chat.model.Tenant
import com.player.chat.model.TenantStatus
import com.player.chat.model.TenantUser
import com.player.chat.navigation.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _tenantList = MutableStateFlow<List<Tenant>>(emptyList())
    val tenantList: StateFlow<List<Tenant>> = _tenantList.asStateFlow()

    private val _currentTenant = MutableStateFlow<Tenant?>(null)
    val currentTenant: StateFlow<Tenant?> = _currentTenant.asStateFlow()

    private val _showTenantDialog = MutableStateFlow(false)
    val showTenantDialog: StateFlow<Boolean> = _showTenantDialog.asStateFlow()

    private val _showLogoutDialog = MutableStateFlow(false)
    val showLogoutDialog: StateFlow<Boolean> = _showLogoutDialog.asStateFlow()

    // 更新结果状态
    private val _updateResult = MutableStateFlow<UpdateResult?>(null)
    val updateResult: StateFlow<UpdateResult?> = _updateResult.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    init {
        loadCurrentTenant()
        loadTenantList()
    }

    private fun loadCurrentTenant() {
        viewModelScope.launch {
            dataStoreManager.getCurrentTenant().collect { tenant ->
                tenant?.let {
                    _currentTenant.value = Tenant(
                        id = it.id,
                        name = it.name,
                        code = it.code,
                        description = null,
                        status = TenantStatus.ENABLED,
                        createDate = null,
                        updateDate = null,
                        createdBy = it.createdBy,
                        updatedBy = null
                    )
                }
            }
        }
    }

    private fun loadTenantList() {
        viewModelScope.launch {
            val result = userRepository.getUserTenantList()
            if (result.isSuccess) {
                _tenantList.value = result.getOrNull() ?: emptyList()
            }
        }
    }

    fun showTenantDialog() {
        _showTenantDialog.value = true
    }

    fun hideTenantDialog() {
        _showTenantDialog.value = false
    }

    fun selectTenant(tenant: Tenant) {
        viewModelScope.launch {
            _currentTenant.value = tenant
            dataStoreManager.saveTenantId(tenant.id)
            dataStoreManager.saveCurrentTenant(tenant)
            _showTenantDialog.value = false
        }
    }

    fun showLogoutDialog() {
        _showLogoutDialog.value = true
    }

    fun hideLogoutDialog() {
        _showLogoutDialog.value = false
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    /**
     * 更新用户头像
     * @param file 头像文件
     */
    fun updateAvatar(file: File) {
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val result = userRepository.updateAvatar(file)
                if (result.isSuccess) {
                    val avatarPath = result.getOrNull()
                    avatarPath?.let {
                        _updateResult.value = UpdateResult.Success("头像更新成功")
                    }
                } else {
                    _updateResult.value = UpdateResult.Error(result.exceptionOrNull()?.message ?: "头像更新失败")
                }
            } catch (e: Exception) {
                _updateResult.value = UpdateResult.Error(e.message ?: "头像更新失败")
            } finally {
                _isUpdating.value = false
            }
        }
    }

    /**
     * 更新用户信息
     * @param user 更新后的用户对象
     */
    fun updateUserInfo(user: com.player.chat.model.User) {
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val result = userRepository.updateUser(user)
                if (result.isSuccess) {
                    _updateResult.value = UpdateResult.Success("用户信息更新成功")
                } else {
                    _updateResult.value = UpdateResult.Error(result.exceptionOrNull()?.message ?: "用户信息更新失败")
                }
            } catch (e: Exception) {
                _updateResult.value = UpdateResult.Error(e.message ?: "用户信息更新失败")
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun resetUpdateResult() {
        _updateResult.value = null
    }
}

/**
 * 更新结果密封类
 */
sealed class UpdateResult {
    data class Success(val message: String) : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}