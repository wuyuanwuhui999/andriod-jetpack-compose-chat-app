package com.player.chat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.local.DataStoreManager
import com.player.chat.model.AddCompanyUserRequest
import com.player.chat.model.Company
import com.player.chat.model.SearchUser
import com.player.chat.model.User
import com.player.chat.repository.CompanyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 用户管理 ViewModel
 */
@HiltViewModel
class UserManageViewModel @Inject constructor(
    private val companyRepository: CompanyRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    // 公司用户列表
    private val _userList = MutableStateFlow<List<User>>(emptyList())
    val userList: StateFlow<List<User>> = _userList.asStateFlow()

    // 当前选中的公司
    private val _currentCompany = MutableStateFlow<Company?>(null)
    val currentCompany: StateFlow<Company?> = _currentCompany.asStateFlow()

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

    // 搜索关键字
    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    // 操作结果提示
    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    private val pageSize = 20

    init {
        loadCurrentCompany()
    }

    /**
     * 加载当前公司信息
     */
    private fun loadCurrentCompany() {
        viewModelScope.launch {
            dataStoreManager.getCurrentCompany().collect { company ->
                company?.let {
                    _currentCompany.value = it
                    // 加载公司用户列表
                    refreshUserList()
                }
            }
        }
    }

    /**
     * 刷新用户列表
     */
    fun refreshUserList() {
        viewModelScope.launch {
            _currentPage.value = 1
            _hasMoreData.value = true
            loadUserList(1, isRefresh = true)
        }
    }

    /**
     * 加载更多用户
     */
    fun loadMoreUserList() {
        viewModelScope.launch {
            val nextPage = _currentPage.value + 1
            if (_hasMoreData.value && !_isLoadingMore.value) {
                loadUserList(nextPage, isRefresh = false)
            } else if (!_hasMoreData.value) {
                _showEndTip.value = true
                kotlinx.coroutines.delay(2000)
                _showEndTip.value = false
            }
        }
    }

    /**
     * 加载用户列表
     */
    private suspend fun loadUserList(page: Int, isRefresh: Boolean) {
        val company = _currentCompany.value ?: return
        val keyword = _searchKeyword.value

        if (isRefresh) {
            _isLoading.value = true
        } else {
            _isLoadingMore.value = true
        }

        try {
            val result = companyRepository.getCompanyUsers(
                companyId = company.id,
                pageNum = page,
                pageSize = pageSize,
                keyword = keyword.ifBlank { null }
            )

            if (result.isSuccess) {
                val newList = result.getOrNull() ?: emptyList()

                if (isRefresh) {
                    _userList.value = newList
                } else {
                    val currentList = _userList.value.toMutableList()
                    currentList.addAll(newList)
                    _userList.value = currentList
                }

                _currentPage.value = page
                _hasMoreData.value = newList.size >= pageSize

                Log.d("UserManage", "加载用户成功: 第${page}页, 数量: ${newList.size}")
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "加载用户失败"
                Log.e("UserManage", "加载用户失败: $errorMsg")
                _operationMessage.value = errorMsg
                kotlinx.coroutines.delay(2000)
                _operationMessage.value = null
            }
        } catch (e: Exception) {
            Log.e("UserManage", "加载用户异常", e)
            _operationMessage.value = e.message ?: "加载用户失败"
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
     * 更新搜索关键字
     */
    fun updateSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword
        // 延迟搜索，避免频繁请求
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            if (_searchKeyword.value == keyword) {
                refreshUserList()
            }
        }
    }

    /**
     * 删除用户
     */
    fun removeUser(user: User) {
        viewModelScope.launch {
            val company = _currentCompany.value ?: return@launch

            try {
                val result = companyRepository.removeUser(user.id, company.id)

                if (result.isSuccess) {
                    val data = result.getOrNull() ?: 0
                    if (data > 0) {
                        // 删除成功，从列表中移除
                        val currentList = _userList.value.toMutableList()
                        currentList.removeAll { it.id == user.id }
                        _userList.value = currentList

                        _operationMessage.value = "删除用户成功"
                        Log.d("UserManage", "删除用户成功: ${user.username}")
                    } else {
                        _operationMessage.value = "删除用户失败"
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "删除用户失败"
                    _operationMessage.value = errorMsg
                    Log.e("UserManage", "删除用户失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("UserManage", "删除用户异常", e)
                _operationMessage.value = e.message ?: "删除用户失败"
            } finally {
                kotlinx.coroutines.delay(2000)
                _operationMessage.value = null
            }
        }
    }

    /**
     * 设置管理员
     */
    fun setAdmin(user: User) {
        viewModelScope.launch {
            val company = _currentCompany.value ?: return@launch

            try {
                val request = AddCompanyUserRequest(
                    userId = user.id,
                    companyId = company.id,
                    role = 1, // 管理员
                    positionId = null
                )
                val result = companyRepository.addCompanyUser(request)

                if (result.isSuccess) {
                    val data = result.getOrNull() ?: 0
                    if (data > 0) {
                        // 更新本地列表中的用户角色
                        val currentList = _userList.value.toMutableList()
                        val index = currentList.indexOfFirst { it.id == user.id }
                        if (index != -1) {
                            // 注意：User 中 role 是 String 类型，这里使用 "1" 表示管理员
                            // 实际可能需要根据后端返回调整
                            val updatedUser = user.copy(role = "1")
                            currentList[index] = updatedUser
                            _userList.value = currentList
                        }
                        _operationMessage.value = "设为管理员成功"
                        Log.d("UserManage", "设为管理员成功: ${user.username}")
                    } else {
                        _operationMessage.value = "设为管理员失败"
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "设为管理员失败"
                    _operationMessage.value = errorMsg
                    Log.e("UserManage", "设为管理员失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("UserManage", "设为管理员异常", e)
                _operationMessage.value = e.message ?: "设为管理员失败"
            } finally {
                kotlinx.coroutines.delay(2000)
                _operationMessage.value = null
            }
        }
    }

    /**
     * 取消管理员
     */
    fun cancelAdmin(user: User) {
        viewModelScope.launch {
            val company = _currentCompany.value ?: return@launch

            try {
                val request = AddCompanyUserRequest(
                    userId = user.id,
                    companyId = company.id,
                    role = 0, // 普通用户
                    positionId = null
                )
                val result = companyRepository.addCompanyUser(request)

                if (result.isSuccess) {
                    val data = result.getOrNull() ?: 0
                    if (data > 0) {
                        // 更新本地列表中的用户角色
                        val currentList = _userList.value.toMutableList()
                        val index = currentList.indexOfFirst { it.id == user.id }
                        if (index != -1) {
                            val updatedUser = user.copy(role = "0")
                            currentList[index] = updatedUser
                            _userList.value = currentList
                        }
                        _operationMessage.value = "取消管理员成功"
                        Log.d("UserManage", "取消管理员成功: ${user.username}")
                    } else {
                        _operationMessage.value = "取消管理员失败"
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "取消管理员失败"
                    _operationMessage.value = errorMsg
                    Log.e("UserManage", "取消管理员失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("UserManage", "取消管理员异常", e)
                _operationMessage.value = e.message ?: "取消管理员失败"
            } finally {
                kotlinx.coroutines.delay(2000)
                _operationMessage.value = null
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