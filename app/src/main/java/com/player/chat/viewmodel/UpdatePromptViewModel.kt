package com.player.chat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.local.DataStoreManager
import com.player.chat.model.UpdatePromptRequest
import com.player.chat.repository.PromptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 更新提示词 ViewModel
 */
@HiltViewModel
class UpdatePromptViewModel @Inject constructor(
    private val promptRepository: PromptRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _promptText = MutableStateFlow("")
    val promptText: StateFlow<String> = _promptText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * 加载提示词
     * @param promptId 提示词ID
     */
    fun loadPrompt(promptId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tenantId = dataStoreManager.getCurrentTenant().firstOrNull()?.id ?: return@launch
                val result = promptRepository.getPromptList(tenantId)
                if (result.isSuccess) {
                    val prompt = result.getOrNull()?.find { it.id == promptId }
                    _promptText.value = prompt?.prompt ?: ""
                }
            } catch (e: Exception) {
                Log.e("UpdatePromptVM", "加载提示词失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 更新提示词文本
     */
    fun updatePromptText(text: String) {
        _promptText.value = text
    }

    /**
     * 更新提示词
     * @param promptId 提示词ID
     * @param prompt 新的提示词内容
     */
    suspend fun updatePrompt(promptId: String, prompt: String): Result<Int> {
        return try {
            val tenantId = dataStoreManager.getCurrentTenant().firstOrNull()?.id
            if (tenantId.isNullOrBlank()) {
                return Result.failure(Exception("未找到租户信息"))
            }

            val request = UpdatePromptRequest(
                id = promptId,
                tenantId = tenantId,
                prompt = prompt
            )

            promptRepository.updatePrompt(promptId, prompt, tenantId)
        } catch (e: Exception) {
            Log.e("UpdatePromptVM", "更新提示词异常", e)
            Result.failure(e)
        }
    }
}