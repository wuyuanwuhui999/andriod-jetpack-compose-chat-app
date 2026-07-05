// viewmodel/AddPromptViewModel.kt
package com.player.chat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.local.DataStoreManager
import com.player.chat.repository.PromptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 添加提示词 ViewModel
 */
@HiltViewModel
class AddPromptViewModel @Inject constructor(
    private val promptRepository: PromptRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    /**
     * 添加提示词
     * @param prompt 提示词内容
     * @return Result<Boolean> 是否添加成功
     */
    suspend fun addPrompt(prompt: String): Result<Boolean> {
        return try {
            // 获取当前租户ID
            val tenant = dataStoreManager.getCurrentTenant().firstOrNull()
            val tenantId = tenant?.id

            if (tenantId.isNullOrBlank()) {
                return Result.failure(Exception("未找到租户信息"))
            }

            val result = promptRepository.insertPrompt(prompt, tenantId)

            if (result.isSuccess && (result.getOrNull() ?: 0) > 0) {
                Log.d("AddPromptVM", "添加提示词成功")
                Result.success(true)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "添加提示词失败"
                Log.e("AddPromptVM", "添加提示词失败: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("AddPromptVM", "添加提示词异常", e)
            Result.failure(e)
        }
    }
}