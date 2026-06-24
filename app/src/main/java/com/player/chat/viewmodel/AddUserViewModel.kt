// chat/viewmodel/AddUserViewModel.kt
package com.player.chat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.local.DataStoreManager
import com.player.chat.model.AddCompanyUserRequest
import com.player.chat.model.Department
import com.player.chat.model.Position
import com.player.chat.model.SearchUser
import com.player.chat.repository.CompanyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 添加用户 ViewModel
 */
@HiltViewModel
class AddUserViewModel @Inject constructor(
    private val companyRepository: CompanyRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    // 搜索结果
    private val _searchResults = MutableStateFlow<List<SearchUser>>(emptyList())
    val searchResults: StateFlow<List<SearchUser>> = _searchResults.asStateFlow()

    // 搜索关键字
    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    // 当前公司
    private val _currentCompany = MutableStateFlow<com.player.chat.model.Company?>(null)
    val currentCompany: StateFlow<com.player.chat.model.Company?> = _currentCompany.asStateFlow()

    // 是否为超级管理员
    private val _isSuperAdmin = MutableStateFlow(false)
    val isSuperAdmin: StateFlow<Boolean> = _isSuperAdmin.asStateFlow()

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

    // 部门列表
    private val _departments = MutableStateFlow<List<Department>>(emptyList())
    val departments: StateFlow<List<Department>> = _departments.asStateFlow()

    // 职位列表
    private val _positions = MutableStateFlow<List<Position>>(emptyList())
    val positions: StateFlow<List<Position>> = _positions.asStateFlow()

    // 部门加载状态
    private val _isLoadingDepartments = MutableStateFlow(false)
    val isLoadingDepartments: StateFlow<Boolean> = _isLoadingDepartments.asStateFlow()

    // 职位加载状态
    private val _isLoadingPositions = MutableStateFlow(false)
    val isLoadingPositions: StateFlow<Boolean> = _isLoadingPositions.asStateFlow()

    private val pageSize = 20

    /**
     * 加载公司信息
     */
    fun loadCompanyInfo() {
        viewModelScope.launch {
            dataStoreManager.getCurrentCompany().collect { company ->
                company?.let {
                    _currentCompany.value = it
                    _isSuperAdmin.value = it.role == 2
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
     */
    private suspend fun searchUsers(page: Int, isRefresh: Boolean) {
        val company = _currentCompany.value ?: return
        val keyword = _searchKeyword.value
        if (keyword.isBlank()) return

        if (isRefresh) {
            _isLoading.value = true
        } else {
            _isLoadingMore.value = true
        }

        try {
            val result = companyRepository.searchUsersWithCompany(
                companyId = company.id,
                pageNum = page,
                pageSize = pageSize,
                keyword = keyword  // 添加 keyword 参数
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

                Log.d("AddUser", "搜索用户成功: 第${page}页, 数量: ${newList.size}")
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "搜索用户失败"
                Log.e("AddUser", "搜索用户失败: $errorMsg")
                _operationMessage.value = errorMsg
                kotlinx.coroutines.delay(2000)
                _operationMessage.value = null
            }
        } catch (e: Exception) {
            Log.e("AddUser", "搜索用户异常", e)
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
     * 直接添加用户（普通管理员使用，默认角色为普通用户）
     */
    fun addUserDirectly(userId: String) {
        viewModelScope.launch {
            val company = _currentCompany.value ?: return@launch

            try {
                val request = AddCompanyUserRequest(
                    userId = userId,
                    companyId = company.id,
                    role = 0, // 普通用户
                    positionId = null
                )
                val result = companyRepository.addCompanyUser(request)

                if (result.isSuccess) {
                    val data = result.getOrNull() ?: 0
                    if (data > 0) {
                        // 更新列表中的用户状态
                        updateUserCheckedStatus(userId, checked = 1)
                        _operationMessage.value = "添加用户成功"
                        Log.d("AddUser", "添加用户成功: $userId")
                    } else {
                        _operationMessage.value = "添加用户失败"
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "添加用户失败"
                    _operationMessage.value = errorMsg
                    Log.e("AddUser", "添加用户失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("AddUser", "添加用户异常", e)
                _operationMessage.value = e.message ?: "添加用户失败"
            } finally {
                kotlinx.coroutines.delay(2000)
                _operationMessage.value = null
            }
        }
    }

    /**
     * 带角色添加用户（超级管理员使用）
     */
    fun addUserWithRole(userId: String, role: Int, positionId: String?) {
        viewModelScope.launch {
            val company = _currentCompany.value ?: return@launch

            try {
                val request = AddCompanyUserRequest(
                    userId = userId,
                    companyId = company.id,
                    role = role,
                    positionId = positionId
                )
                val result = companyRepository.addCompanyUser(request)

                if (result.isSuccess) {
                    val data = result.getOrNull() ?: 0
                    if (data > 0) {
                        // 更新列表中的用户状态
                        updateUserCheckedStatus(userId, checked = 1)
                        _operationMessage.value = "添加用户成功"
                        Log.d("AddUser", "添加用户成功: $userId, role: $role")
                    } else {
                        _operationMessage.value = "添加用户失败"
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "添加用户失败"
                    _operationMessage.value = errorMsg
                    Log.e("AddUser", "添加用户失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("AddUser", "添加用户异常", e)
                _operationMessage.value = e.message ?: "添加用户失败"
            } finally {
                kotlinx.coroutines.delay(2000)
                _operationMessage.value = null
            }
        }
    }

    /**
     * 更新用户列表中用户的 checked 状态
     */
    private fun updateUserCheckedStatus(userId: String, checked: Int) {
        val currentList = _searchResults.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == userId }
        if (index != -1) {
            val updatedUser = currentList[index].copy(checked = checked)
            currentList[index] = updatedUser
            _searchResults.value = currentList
        }
    }

    /**
     * 加载部门列表
     */
    fun loadDepartments(companyId: String) {
        viewModelScope.launch {
            _isLoadingDepartments.value = true
            try {
                val result = companyRepository.getDepartments(companyId)
                if (result.isSuccess) {
                    _departments.value = result.getOrNull() ?: emptyList()
                    Log.d("AddUser", "加载部门成功: ${_departments.value.size} 条")
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "加载部门失败"
                    Log.e("AddUser", "加载部门失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("AddUser", "加载部门异常", e)
            } finally {
                _isLoadingDepartments.value = false
            }
        }
    }

    /**
     * 加载职位列表
     */
    fun loadPositions(departmentId: String) {
        viewModelScope.launch {
            _isLoadingPositions.value = true
            _positions.value = emptyList()
            try {
                val result = companyRepository.getPositions(departmentId)
                if (result.isSuccess) {
                    _positions.value = result.getOrNull() ?: emptyList()
                    Log.d("AddUser", "加载职位成功: ${_positions.value.size} 条")
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "加载职位失败"
                    Log.e("AddUser", "加载职位失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("AddUser", "加载职位异常", e)
            } finally {
                _isLoadingPositions.value = false
            }
        }
    }

    /**
     * 重置操作提示
     */
    fun resetOperationMessage() {
        _operationMessage.value = null
    }

    /**
     * 重置结束提示
     */
    fun resetEndTip() {
        _showEndTip.value = false
    }
}