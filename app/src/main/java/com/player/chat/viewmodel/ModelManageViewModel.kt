package com.player.chat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.local.DataStoreManager
import com.player.chat.model.AddModelRequest
import com.player.chat.model.ChatModel
import com.player.chat.model.UpdateModelRequest
import com.player.chat.repository.ModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 模型管理 ViewModel
 */
@HiltViewModel
class ModelManageViewModel @Inject constructor(
    private val modelRepository: ModelRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    // 模型列表
    private val _modelList = MutableStateFlow<List<ChatModel>>(emptyList())
    val modelList: StateFlow<List<ChatModel>> = _modelList.asStateFlow()

    // 当前使用的模型ID
    private val _currentModelId = MutableStateFlow<String?>(null)
    val currentModelId: StateFlow<String?> = _currentModelId.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 搜索关键字
    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    // 操作结果提示
    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    private var currentCompanyId: String? = null

    init {
        viewModelScope.launch {
            loadCurrentCompanyId()
        }
    }

    /**
     * 加载当前公司ID
     */
    /**
     * 加载当前公司ID
     * 修改：确保在 companyId 正确获取后，再加载模型ID和模型列表
     */
    private suspend fun loadCurrentCompanyId() {
        // 1. 获取当前用户
        val currentUser = dataStoreManager.getUser().firstOrNull()
        if (currentUser == null) {
            Log.e("ModelManageVM", "用户未登录，无法加载公司ID")
            return
        }

        // 2. 获取公司ID
        val companyKey = "company_id_${currentUser.id}"
        currentCompanyId = dataStoreManager.getString(companyKey).firstOrNull()

        if (currentCompanyId.isNullOrBlank()) {
            Log.e("ModelManageVM", "公司ID为空，无法加载模型数据")
            return
        }

        Log.d("ModelManageVM", "加载公司ID成功: $currentCompanyId")

        // 3. 在 companyId 确认后，按顺序加载数据
        loadCurrentModelId()
        loadModelList()
    }

    /**
     * 加载当前使用的模型ID
     */
    private suspend fun loadCurrentModelId() {
        val companyId = currentCompanyId ?: return
        val key = "current_model_id_${companyId}"
        val modelId = dataStoreManager.getString(key).firstOrNull()
        _currentModelId.value = modelId
        Log.d("ModelManageVM", "加载当前模型ID: $modelId")
    }

    /**
     * 保存当前使用的模型ID到缓存
     */
    private suspend fun saveCurrentModelId(modelId: String) {
        val companyId = currentCompanyId ?: return
        val key = "current_model_id_${companyId}"
        dataStoreManager.saveString(key, modelId)
        _currentModelId.value = modelId
        Log.d("ModelManageVM", "保存当前模型ID: $modelId")
    }

    /**
     * 加载模型列表
     * 修改：加载完成后，如果 currentModelId 为空，尝试从缓存重新加载
     */
    fun loadModelList() {
        viewModelScope.launch {
            val companyId = currentCompanyId
            if (companyId.isNullOrBlank()) {
                Log.e("ModelManageVM", "公司ID为空，无法加载模型列表")
                _operationMessage.value = "未找到公司信息"
                return@launch
            }

            val keyword = _searchKeyword.value

            _isLoading.value = true

            try {
                val result = modelRepository.getModelList(companyId, keyword.takeIf { it.isNotBlank() })
                if (result.isSuccess) {
                    _modelList.value = result.getOrNull() ?: emptyList()
                    Log.d("ModelManageVM", "加载模型列表成功: ${_modelList.value.size} 条")

                    // 如果 currentModelId 为空，尝试从缓存重新加载
                    if (_currentModelId.value.isNullOrBlank() && _modelList.value.isNotEmpty()) {
                        loadCurrentModelId()
                        // 如果加载后仍然为空，默认选中第一个
                        if (_currentModelId.value.isNullOrBlank()) {
                            _currentModelId.value = _modelList.value.firstOrNull()?.id
                            Log.d("ModelManageVM", "默认选中第一个模型: ${_currentModelId.value}")
                        }
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "加载模型列表失败"
                    Log.e("ModelManageVM", "加载模型列表失败: $errorMsg")
                    _operationMessage.value = errorMsg
                }
            } catch (e: Exception) {
                Log.e("ModelManageVM", "加载模型列表异常", e)
                _operationMessage.value = e.message ?: "加载模型列表失败"
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
                loadModelList()
            }
        }
    }

    /**
     * 使用模型（设置为当前使用的模型）
     */
    fun useModel(model: ChatModel) {
        viewModelScope.launch {
            try {
                saveCurrentModelId(model.id)
                _operationMessage.value = "使用模型成功"
                Log.d("ModelManageVM", "使用模型成功: ${model.modelName}")

                // 延迟2秒后清除消息
                kotlinx.coroutines.delay(2000)
                _operationMessage.value = null
            } catch (e: Exception) {
                Log.e("ModelManageVM", "使用模型异常", e)
                _operationMessage.value = e.message ?: "使用模型失败"
            }
        }
    }

    /**
     * 删除模型
     */
    fun deleteModel(model: ChatModel, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // 检查是否为当前使用中的模型
                if (_currentModelId.value == model.id) {
                    _operationMessage.value = "当前使用中的模型不能删除"
                    Log.w("ModelManageVM", "当前使用中的模型不能删除: ${model.modelName}")
                    kotlinx.coroutines.delay(2000)
                    _operationMessage.value = null
                    return@launch
                }

                val result = modelRepository.deleteModel(model.id)
                if (result.isSuccess && (result.getOrNull() ?: 0) > 0) {
                    _operationMessage.value = "删除模型成功"
                    Log.d("ModelManageVM", "删除模型成功: ${model.modelName}")
                    // 重新加载列表
                    loadModelList()
                    onSuccess()
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "删除模型失败"
                    _operationMessage.value = errorMsg
                    Log.e("ModelManageVM", "删除模型失败: $errorMsg")
                }
            } catch (e: Exception) {
                Log.e("ModelManageVM", "删除模型异常", e)
                _operationMessage.value = e.message ?: "删除模型失败"
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

    /**
     * 获取当前公司ID
     */
    fun getCompanyId(): String? = currentCompanyId

    // 在 ModelManageViewModel 中添加以下方法

    /**
     * 添加模型
     */
    suspend fun addModel(request: AddModelRequest): Result<Int> {
        return modelRepository.addModel(request)
    }

    /**
     * 更新模型
     */
    suspend fun updateModel(request: UpdateModelRequest): Result<Int> {
        return modelRepository.updateModel(request)
    }
}