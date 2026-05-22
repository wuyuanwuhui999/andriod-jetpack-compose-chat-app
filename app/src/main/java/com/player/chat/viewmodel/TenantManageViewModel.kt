package com.player.chat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.local.DataStoreManager
import com.player.chat.model.SearchUser
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

    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchUser>>(emptyList())
    val searchResults: StateFlow<List<SearchUser>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // 添加成功提示
    private val _addSuccessMessage = MutableStateFlow<String?>(null)
    val addSuccessMessage: StateFlow<String?> = _addSuccessMessage.asStateFlow()



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

    /**
     * 删除租户用户
     * @param tenantUser 要删除的租户用户对象
     */
    fun deleteTenantUser(tenantUser: TenantUser) {
        viewModelScope.launch {
            try {
                val tenantId = _currentTenant.value?.id ?: return@launch

                val result = tenantRepository.deleteTenantUser(tenantId, tenantUser.userId)

                if (result.isSuccess) {
                    val deletedCount = result.getOrNull() ?: 0
                    if (deletedCount > 0) {
                        // 删除成功，从列表中移除该用户
                        val currentList = _tenantUserList.value.toMutableList()
                        currentList.removeAll { it.id == tenantUser.id }
                        _tenantUserList.value = currentList

                        // 如果删除后列表少于10条且有更多数据，重新加载第一页
                        if (currentList.size < 10 && _hasMoreData.value) {
                            refreshTenantUserList()
                        }

                        Log.d("TenantManage", "删除用户成功: ${tenantUser.username}")
                    } else {
                        Log.e("TenantManage", "删除用户失败: 返回数据为0")
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "删除用户失败"
                    Log.e("TenantManage", "删除用户失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("TenantManage", "删除用户异常", e)
            }
        }
    }

    /**
     * 更新搜索关键字
     * 当关键字变化时自动触发搜索
     */
    fun updateSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword
        if (keyword.isNotBlank()) {
            performSearch(keyword)
        } else {
            _searchResults.value = emptyList()
        }
    }

    /**
     * 执行搜索
     * @param keyword 搜索关键字
     */
    private fun performSearch(keyword: String) {
        viewModelScope.launch {
            val tenantId = _currentTenant.value?.id ?: return@launch

            _isSearching.value = true
            try {
                val result = tenantRepository.searchUsers(keyword, tenantId)
                if (result.isSuccess) {
                    val users = result.getOrNull() ?: emptyList()
                    _searchResults.value = users
                    Log.d("TenantManage", "搜索用户成功: 找到${users.size}个用户")
                } else {
                    _searchResults.value = emptyList()
                    val errorMsg = result.exceptionOrNull()?.message ?: "搜索失败"
                    Log.e("TenantManage", "搜索用户失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("TenantManage", "搜索用户异常", e)
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    /**
     * 清除搜索结果
     */
    fun clearSearchResults() {
        _searchKeyword.value = ""
        _searchResults.value = emptyList()
    }

    /**
     * 添加用户到当前租户
     * @param user 要添加的用户
     */
    fun addUserToTenant(user: SearchUser) {
        viewModelScope.launch {
            val tenantId = _currentTenant.value?.id ?: return@launch

            try {
                val result = tenantRepository.addTenantUser(tenantId, user.id)
                if (result.isSuccess && (result.getOrNull() ?: 0) > 0) {
                    // 添加成功，显示成功提示
                    _addSuccessMessage.value = "已添加用户: ${user.username}"
                    // 2秒后自动清除提示
                    kotlinx.coroutines.delay(2000)
                    _addSuccessMessage.value = null

                    // 清除搜索结果
                    clearSearchResults()

                    // 刷新租户用户列表
                    refreshTenantUserList()
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "添加用户失败"
                    Log.e("TenantManage", "添加用户失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("TenantManage", "添加用户异常", e)
            }
        }
    }

    /**
     * 重置添加成功提示
     */
    fun resetAddSuccessMessage() {
        _addSuccessMessage.value = null
    }


    fun resetEndTip() {
        _showEndTip.value = false
    }

    override fun onCleared() {
        super.onCleared()
    }
}