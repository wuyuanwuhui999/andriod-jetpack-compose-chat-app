// viewmodel/UserViewModel.kt
// 修改 loadCurrentTenant 和 loadTenantList 方法

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

    /**
     * 加载当前租户
     * 先从缓存获取租户ID，然后从租户列表中找到对应的租户（包含role字段）
     */
    private fun loadCurrentTenant() {
        viewModelScope.launch {
            // 先获取缓存的租户ID
            val cachedTenantId = dataStoreManager.getTenantId().firstOrNull()

            if (cachedTenantId != null) {
                // 从租户列表中查找对应的租户（包含role字段）
                val matchedTenant = _tenantList.value.find { it.id == cachedTenantId }
                if (matchedTenant != null) {
                    _currentTenant.value = matchedTenant
                    // 更新缓存，确保role字段被保存
                    dataStoreManager.saveCurrentTenant(matchedTenant)
                    return@launch
                }
            }

            // 如果没有匹配的租户，尝试从缓存直接读取
            dataStoreManager.getCurrentTenant().collect { tenant ->
                tenant?.let {
                    _currentTenant.value = Tenant(
                        id = it.id,
                        name = it.name,
                        code = it.code,
                        description = it.description,
                        status = it.status,
                        createDate = it.createDate,
                        updateDate = it.updateDate,
                        createdBy = it.createdBy ?: "",
                        updatedBy = it.updatedBy,
                        role = it.role // 确保role字段被传递
                    )
                }
            }
        }
    }

    /**
     * 加载租户列表
     * 获取到租户列表后，更新当前租户（如果当前租户在列表中）
     */
    private fun loadTenantList() {
        viewModelScope.launch {
            // 获取当前用户的 companyId
            val currentUser = dataStoreManager.getUser().firstOrNull()
            val companyKey = if (currentUser != null) "company_id_${currentUser.id}" else "company_id"
            val cachedCompanyId = dataStoreManager.getString(companyKey).firstOrNull()

            if (cachedCompanyId.isNullOrBlank()) {
                _tenantList.value = emptyList()
                return@launch
            }

            val result = userRepository.getTenantList(cachedCompanyId)
            if (result.isSuccess) {
                val tenantList = result.getOrNull() ?: emptyList()
                _tenantList.value = tenantList

                // 租户列表获取成功后，更新当前租户
                updateCurrentTenantFromList(tenantList)
            }
        }
    }

    /**
     * 从租户列表中更新当前租户
     * @param tenantList 租户列表
     */
    private suspend fun updateCurrentTenantFromList(tenantList: List<Tenant>) {
        val cachedTenantId = dataStoreManager.getTenantId().firstOrNull()

        if (cachedTenantId != null) {
            // 从租户列表中查找匹配的租户
            val matchedTenant = tenantList.find { it.id == cachedTenantId }
            if (matchedTenant != null) {
                _currentTenant.value = matchedTenant
                // 更新缓存，确保role字段被保存
                dataStoreManager.saveCurrentTenant(matchedTenant)
                return
            }
        }

        // 如果没有匹配的租户，使用第一个租户或默认租户
        if (tenantList.isNotEmpty()) {
            val firstTenant = tenantList.first()
            _currentTenant.value = firstTenant
            dataStoreManager.saveTenantId(firstTenant.id)
            dataStoreManager.saveCurrentTenant(firstTenant)
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