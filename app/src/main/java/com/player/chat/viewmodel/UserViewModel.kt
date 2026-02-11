// UserViewModel.kt
package com.player.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.chat.repository.UserRepository
import com.player.chat.local.DataStoreManager
import com.player.chat.model.Tenant
import com.player.chat.model.TenantStatus
import com.player.chat.navigation.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
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

    init {
        loadCurrentTenant()
        loadTenantList()
    }

    private fun loadCurrentTenant() {
        viewModelScope.launch {
            dataStoreManager.getCurrentTenant().collect { tenant ->
                // 转换 TenantUser 为 Tenant
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
}