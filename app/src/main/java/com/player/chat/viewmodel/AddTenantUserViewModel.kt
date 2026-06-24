package com.player.chat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.local.DataStoreManager
import com.player.chat.model.SearchUser
import com.player.chat.model.Tenant
import com.player.chat.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 添加租户用户 ViewModel
 */
@HiltViewModel
class AddTenantUserViewModel @Inject constructor(
    private val tenantRepository: TenantRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    // 搜索结果
    private val _searchResults = MutableStateFlow<List<SearchUser>>(emptyList())
    val searchResults: StateFlow<List<SearchUser>> = _searchResults.asStateFlow()

    // 搜索关键字
    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    // 当前租户
    private val _currentTenant = MutableStateFlow<Tenant?>(null)
    val currentTenant: StateFlow<Tenant?> = _currentTenant.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // 分页状态
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _showEndTip = MutableStateFlow(false)
    val showEndTip: StateFlow<Boolean> = _showEndTip.asStateFlow()

    // 操作结果提示
    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    private val pageSize = 20

    /**
     * 加载当前租户信息
     */
    fun loadTenantInfo() {
        viewModelScope.launch {
            dataStoreManager.getCurrentTenant().collect { tenant ->
                tenant?.let {
                    _currentTenant.value = it
                }
            }
        }
    }

    /**
     * 更新搜索关键字
     */
    fun updateSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword
        // 延迟搜索，避免频繁请求
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            if (_searchKeyword.value == keyword && keyword.isNotBlank()) {
                refreshSearchResults()
            } else if (keyword.isBlank()) {
                _searchResults.value = emptyList()
                _currentPage.value = 1
                _hasMoreData.value = true
            }
        }
    }

    /**
     * 刷新搜索结果
     */
    fun refreshSearchResults() {
        viewModelScope.launch {
            val keyword = _searchKeyword.value
            if (keyword.isBlank()) return@launch

            _currentPage.value = 1
            _hasMoreData.value = true
            searchUsers(1, isRefresh = true)
        }
    }

    /**
     * 加载更多搜索结果
     */
    fun loadMoreSearchResults() {
        viewModelScope.launch {
            val nextPage = _currentPage.value + 1
            if (_hasMoreData.value && !_isLoadingMore.value) {
                searchUsers(nextPage, isRefresh = false)
            } else if (!_hasMoreData.value) {
                _showEndTip.value = true
                kotlinx.coroutines.delay(2000)
                _showEndTip.value = false
            }
        }
    }

    /**
     * 搜索用户
     * @param page 页码
     * @param isRefresh 是否刷新
     */
    private suspend fun searchUsers(page: Int, isRefresh: Boolean) {
        val tenant = _currentTenant.value ?: return
        val keyword = _searchKeyword.value
        if (keyword.isBlank()) return

        if (isRefresh) {
            _isLoading.value = true
        } else {
            _isLoadingMore.value = true
        }

        try {
            val result = tenantRepository.searchUsers(
                keyword = keyword,
                tenantId = tenant.id,
                pageNum = page,
                pageSize = pageSize
            )

            if (result.isSuccess) {
                val newList = result.getOrNull() ?: emptyList()

                if (isRefresh) {
                    _searchResults.value = newList
                } else {
                    val currentList = _searchResults.value.toMutableList()
                    currentList.addAll(newList)
                    _searchResults.value = currentList
                }

                _currentPage.value = page
                _hasMoreData.value = newList.size >= pageSize

                Log.d("AddTenantUser", "搜索用户成功: 第${page}页, 数量: ${newList.size}")
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "搜索用户失败"
                Log.e("AddTenantUser", "搜索用户失败: $errorMsg")
                _operationMessage.value = errorMsg
                kotlinx.coroutines.delay(2000)
                _operationMessage.value = null
            }
        } catch (e: Exception) {
            Log.e("AddTenantUser", "搜索用户异常", e)
            _operationMessage.value = e.message ?: "搜索用户失败"
            kotlinx.coroutines.delay(2000)
            _operationMessage.value = null
        } finally {
            if (isRefresh) {
                _isLoading.value = false
            } else {
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * 添加用户到租户
     * @param user 要添加的用户
     */
    fun addUserToTenant(user: SearchUser) {
        viewModelScope.launch {
            val tenant = _currentTenant.value ?: return@launch

            try {
                val result = tenantRepository.addTenantUser(tenant.id, user.id)

                if (result.isSuccess) {
                    val data = result.getOrNull()
                    if (data != null && data > 0) {
                        // 添加成功，更新用户列表中的 checked 状态
                        updateUserCheckedStatus(user.id, checked = 1)
                        _operationMessage.value = "添加用户成功"
                        Log.d("AddTenantUser", "添加用户成功: ${user.username}")
                    } else {
                        _operationMessage.value = "添加用户失败"
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "添加用户失败"
                    _operationMessage.value = errorMsg
                    Log.e("AddTenantUser", "添加用户失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("AddTenantUser", "添加用户异常", e)
                _operationMessage.value = e.message ?: "添加用户失败"
            } finally {
                kotlinx.coroutines.delay(2000)
                _operationMessage.value = null
            }
        }
    }

    /**
     * 更新用户列表中用户的 checked 状态
     * 使用 map 方式创建新列表，避免 copy 方法对非空参数的要求
     */
    private fun updateUserCheckedStatus(userId: String, checked: Int) {
        val currentList = _searchResults.value
        val updatedList = currentList.map { user ->
            if (user.id == userId) {
                // 创建新的 SearchUser 对象，直接设置 checked 字段
                SearchUser(
                    id = user.id,
                    userAccount = user.userAccount,
                    createDate = user.createDate,
                    updateDate = user.updateDate,
                    username = user.username,
                    telephone = user.telephone,
                    email = user.email,
                    avatar = user.avatar,
                    birthday = user.birthday,
                    sex = user.sex,
                    password = user.password,
                    sign = user.sign,
                    region = user.region,
                    disabled = user.disabled,
                    permission = user.permission,
                    checked = checked
                )
            } else {
                user
            }
        }
        _searchResults.value = updatedList
    }

    /**
     * 重置结束提示
     */
    fun resetEndTip() {
        _showEndTip.value = false
    }

    /**
     * 重置操作提示
     */
    fun resetOperationMessage() {
        _operationMessage.value = null
    }
}