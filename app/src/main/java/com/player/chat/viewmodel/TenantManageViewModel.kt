package com.player.chat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.local.DataStoreManager
import com.player.chat.model.Tenant
import com.player.chat.model.TenantUser
import com.player.chat.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TenantManageViewModel @Inject constructor(
    private val tenantRepository: TenantRepository,  // 改为使用 TenantRepository
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _tenantUserList = MutableStateFlow<List<TenantUser>>(emptyList())
    val tenantUserList: StateFlow<List<TenantUser>> = _tenantUserList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _currentTenant = MutableStateFlow<Tenant?>(null)
    val currentTenant: StateFlow<Tenant?> = _currentTenant.asStateFlow()

    private val _showEndTip = MutableStateFlow(false)
    val showEndTip: StateFlow<Boolean> = _showEndTip.asStateFlow()

    private val pageSize = 20

    init {
        loadCurrentTenant()
    }

    private fun loadCurrentTenant() {
        viewModelScope.launch {
            dataStoreManager.getCurrentTenant().collect { tenant ->
                tenant?.let {
                    _currentTenant.value = it
                    // 加载租户用户列表
                    refreshTenantUserList()
                }
            }
        }
    }

    fun refreshTenantUserList() {
        viewModelScope.launch {
            _currentPage.value = 1
            _hasMoreData.value = true
            loadTenantUserList(1, isRefresh = true)
        }
    }

    fun loadMoreTenantUserList() {
        viewModelScope.launch {
            val nextPage = _currentPage.value + 1
            if (_hasMoreData.value && !_isLoadingMore.value) {
                loadTenantUserList(nextPage, isRefresh = false)
            } else if (!_hasMoreData.value) {
                // 已经最后一页，显示提示
                _showEndTip.value = true
                // 2秒后自动隐藏提示
                kotlinx.coroutines.delay(2000)
                _showEndTip.value = false
            }
        }
    }

    private suspend fun loadTenantUserList(page: Int, isRefresh: Boolean) {
        val tenantId = _currentTenant.value?.id ?: return

        if (isRefresh) {
            _isLoading.value = true
        } else {
            _isLoadingMore.value = true
        }

        try {
            // 使用 TenantRepository 获取租户用户列表
            val result = tenantRepository.getTenantUserList(tenantId, page, pageSize)

            if (result.isSuccess) {
                val newList = result.getOrNull() ?: emptyList()

                if (isRefresh) {
                    _tenantUserList.value = newList
                } else {
                    val currentList = _tenantUserList.value.toMutableList()
                    currentList.addAll(newList)
                    _tenantUserList.value = currentList
                }

                _currentPage.value = page
                _hasMoreData.value = newList.size >= pageSize

                Log.d("TenantManage", "加载租户用户成功: 第${page}页, 数量: ${newList.size}")
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "加载租户用户失败"
                Log.e("TenantManage", "加载租户用户失败: $errorMessage")
                // 可以在这里添加错误提示状态
            }
        } catch (e: Exception) {
            Log.e("TenantManage", "加载租户用户异常", e)
        } finally {
            if (isRefresh) {
                _isLoading.value = false
            } else {
                _isLoadingMore.value = false
            }
        }
    }

    fun resetEndTip() {
        _showEndTip.value = false
    }

    override fun onCleared() {
        super.onCleared()
    }
}