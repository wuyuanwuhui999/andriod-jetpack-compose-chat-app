package com.player.chat.repository

import android.util.Log
import com.player.chat.model.InsertPromptRequest
import com.player.chat.model.Prompt
import com.player.chat.model.UpdatePromptRequest
import com.player.chat.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 提示词相关数据仓库
 */
@Singleton
class PromptRepository @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * 获取提示词列表
     * @param tenantId 租户ID
     * @param keyword 搜索关键字（可选）
     * @return Result<List<Prompt>>
     */
    suspend fun getPromptList(tenantId: String, keyword: String? = null): Result<List<Prompt>> {
        return try {
            val response = apiService.getPromptList(tenantId, keyword)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val prompts = body.data ?: emptyList()
                    Log.d("PromptRepository", "获取提示词列表成功，共 ${prompts.size} 条")
                    Result.success(prompts)
                } else {
                    val errorMsg = body?.message ?: "获取提示词列表失败"
                    Log.e("PromptRepository", "获取提示词列表失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PromptRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PromptRepository", "获取提示词列表异常", e)
            Result.failure(e)
        }
    }

    /**
     * 新增提示词
     * @param prompt 提示词内容
     * @param tenantId 租户ID
     * @return Result<Int> 影响行数
     */
    suspend fun insertPrompt(prompt: String, tenantId: String): Result<Int> {
        return try {
            val request = InsertPromptRequest(prompt, tenantId)
            val response = apiService.insertPrompt(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val data = body.data ?: 0
                    Log.d("PromptRepository", "新增提示词成功，影响行数: $data")
                    Result.success(data)
                } else {
                    val errorMsg = body?.message ?: "新增提示词失败"
                    Log.e("PromptRepository", "新增提示词失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PromptRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PromptRepository", "新增提示词异常", e)
            Result.failure(e)
        }
    }

    /**
     * 更新提示词
     * @param id 提示词ID
     * @param prompt 新的提示词内容
     * @param tenantId 租户ID
     * @return Result<Int> 影响行数
     */
    suspend fun updatePrompt(id: String, prompt: String, tenantId: String): Result<Int> {
        return try {
            val request = UpdatePromptRequest(id, tenantId, "", prompt)
            val response = apiService.updatePrompt(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val data = body.data ?: 0
                    Log.d("PromptRepository", "更新提示词成功，影响行数: $data")
                    Result.success(data)
                } else {
                    val errorMsg = body?.message ?: "更新提示词失败"
                    Log.e("PromptRepository", "更新提示词失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PromptRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PromptRepository", "更新提示词异常", e)
            Result.failure(e)
        }
    }

    /**
     * 删除提示词
     * @param promptId 提示词ID
     * @return Result<Int> 影响行数
     */
    suspend fun deletePrompt(promptId: String): Result<Int> {
        return try {
            val response = apiService.deletePrompt(promptId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val data = body.data ?: 0
                    Log.d("PromptRepository", "删除提示词成功，影响行数: $data")
                    Result.success(data)
                } else {
                    val errorMsg = body?.message ?: "删除提示词失败"
                    Log.e("PromptRepository", "删除提示词失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PromptRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PromptRepository", "删除提示词异常", e)
            Result.failure(e)
        }
    }
}