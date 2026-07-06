// viewmodel/PromptManageViewModel.kt
package com.player.chat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.local.DataStoreManager
import com.player.chat.model.Prompt
import com.player.chat.repository.PromptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 提示词管理 ViewModel
 */
@HiltViewModel
class PromptManageViewModel @Inject constructor(
    private val promptRepository: PromptRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    // 提示词列表
    private val _promptList = MutableStateFlow<List<Prompt>>(emptyList())
    val promptList: StateFlow<List<Prompt>> = _promptList.asStateFlow()

    // 当前使用的提示词ID
    private val _currentPromptId = MutableStateFlow<String?>(null)
    val currentPromptId: StateFlow<String?> = _currentPromptId.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 搜索关键字
    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    // 操作结果提示
    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    private var currentTenantId: String? = null

    init {
        // 在 init 中使用 viewModelScope.launch 调用挂起函数
        viewModelScope.launch {
            // 修改：先获取当前的租户ID，再加载提示词
            loadCurrentTenantIdInitial()
            // 然后再监听租户变化
            observeTenantChanges()
        }
    }

    /**
     * 加载当前租户ID（初始值）
     * 使用 firstOrNull() 立即获取缓存的租户ID
     */
    private suspend fun loadCurrentTenantIdInitial() {
        // 直接从缓存获取租户ID
        val cachedTenantId = dataStoreManager.getTenantId().firstOrNull()
        if (cachedTenantId != null) {
            // 从缓存获取完整的租户信息
            val cachedTenant = dataStoreManager.getCurrentTenant().firstOrNull()
            cachedTenant?.let {
                currentTenantId = it.id
                // 加载提示词ID
                loadCurrentPromptId()
                // 加载提示词列表
                loadPromptList()
            }
        }
    }

    /**
     * 监听租户变化
     * 租户变化时重新加载数据
     */
    private suspend fun observeTenantChanges() {
        dataStoreManager.getCurrentTenant().collect { tenant ->
            tenant?.let {
                val newTenantId = it.id
                // 只有租户ID发生变化时才重新加载
                if (currentTenantId != newTenantId) {
                    currentTenantId = newTenantId
                    // 租户变化时重新加载提示词列表
                    loadPromptList()
                    // 重新加载提示词ID
                    loadCurrentPromptId()
                }
            }
        }
    }

    /**
     * 加载当前使用的提示词ID（修改为 suspend 函数，直接调用）
     */
    private suspend fun loadCurrentPromptId() {
        val userId = dataStoreManager.getUser().firstOrNull()?.id ?: return
        val tenantId = currentTenantId ?: return
        val key = "prompt_id_${userId}_${tenantId}"
        val promptId = dataStoreManager.getString(key).firstOrNull()
        _currentPromptId.value = promptId
        Log.d("PromptManageVM", "加载当前提示词ID: $promptId, tenantId: $tenantId, userId: $userId")
    }

    /**
     * 加载提示词列表
     */
    fun loadPromptList() {
        viewModelScope.launch {
            val tenantId = currentTenantId ?: return@launch
            val keyword = _searchKeyword.value

            _isLoading.value = true

            try {
                val result = promptRepository.getPromptList(tenantId, keyword)
                if (result.isSuccess) {
                    _promptList.value = result.getOrNull() ?: emptyList()
                    Log.d("PromptManageVM", "加载提示词列表成功: ${_promptList.value.size} 条")
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "加载提示词列表失败"
                    Log.e("PromptManageVM", "加载提示词列表失败: $errorMsg")
                    _operationMessage.value = errorMsg
                }
            } catch (e: Exception) {
                Log.e("PromptManageVM", "加载提示词列表异常", e)
                _operationMessage.value = e.message ?: "加载提示词列表失败"
            } finally {
                _isLoading.value = false
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
                loadPromptList()
            }
        }
    }

    /**
     * 使用提示词（设置为当前使用的提示词）
     * 修改：保存成功后，更新 currentPromptId
     */
    fun usePrompt(prompt: Prompt) {
        viewModelScope.launch {
            try {
                val userId = dataStoreManager.getUser().firstOrNull()?.id
                val tenantId = currentTenantId

                if (userId == null || tenantId == null) {
                    _operationMessage.value = "用户或租户信息缺失"
                    return@launch
                }

                // 保存提示词ID到缓存
                val key = "prompt_id_${userId}_${tenantId}"
                dataStoreManager.saveString(key, prompt.id)

                // 更新当前提示词ID - 直接赋值
                _currentPromptId.value = prompt.id
                Log.d("PromptManageVM", "使用提示词成功: ${prompt.id}, 已更新 currentPromptId")

                _operationMessage.value = "使用提示词成功"

                // 延迟2秒后清除消息
                kotlinx.coroutines.delay(2000)
                _operationMessage.value = null
            } catch (e: Exception) {
                Log.e("PromptManageVM", "使用提示词异常", e)
                _operationMessage.value = e.message ?: "使用提示词失败"
            }
        }
    }

    /**
     * 更新提示词
     */
    fun updatePrompt(promptId: String, newPrompt: String) {
        viewModelScope.launch {
            try {
                val tenantId = currentTenantId ?: run {
                    _operationMessage.value = "租户信息缺失"
                    return@launch
                }

                val result = promptRepository.updatePrompt(promptId, newPrompt, tenantId)
                if (result.isSuccess && (result.getOrNull() ?: 0) > 0) {
                    _operationMessage.value = "更新提示词成功"
                    Log.d("PromptManageVM", "更新提示词成功: $promptId")
                    // 重新加载列表
                    loadPromptList()
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "更新提示词失败"
                    _operationMessage.value = errorMsg
                    Log.e("PromptManageVM", "更新提示词失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("PromptManageVM", "更新提示词异常", e)
                _operationMessage.value = e.message ?: "更新提示词失败"
            } finally {
                // 延迟2秒后清除消息
                kotlinx.coroutines.delay(2000)
                _operationMessage.value = null
            }
        }
    }

    /**
     * 删除提示词
     */
    fun deletePrompt(promptId: String) {
        viewModelScope.launch {
            try {
                val result = promptRepository.deletePrompt(promptId)
                if (result.isSuccess && (result.getOrNull() ?: 0) > 0) {
                    _operationMessage.value = "删除提示词成功"
                    Log.d("PromptManageVM", "删除提示词成功: $promptId")

                    // 如果删除的是当前使用的提示词，清空缓存中的提示词ID
                    if (_currentPromptId.value == promptId) {
                        val userId = dataStoreManager.getUser().firstOrNull()?.id
                        val tenantId = currentTenantId
                        if (userId != null && tenantId != null) {
                            val key = "prompt_id_${userId}_${tenantId}"
                            dataStoreManager.saveString(key, "")
                            _currentPromptId.value = null
                            Log.d("PromptManageVM", "清空缓存的提示词ID")
                        }
                    }

                    // 重新加载列表
                    loadPromptList()
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "删除提示词失败"
                    _operationMessage.value = errorMsg
                    Log.e("PromptManageVM", "删除提示词失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("PromptManageVM", "删除提示词异常", e)
                _operationMessage.value = e.message ?: "删除提示词失败"
            } finally {
                // 延迟2秒后清除消息
                kotlinx.coroutines.delay(2000)
                _operationMessage.value = null
            }
        }
    }

    /**
     * 重置操作消息
     */
    fun resetOperationMessage() {
        _operationMessage.value = null
    }
}