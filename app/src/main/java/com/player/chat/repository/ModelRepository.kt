package com.player.chat.repository

import android.util.Log
import com.player.chat.model.AddModelRequest
import com.player.chat.model.ChatModel
import com.player.chat.model.UpdateModelRequest
import com.player.chat.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 模型管理数据仓库
 */
@Singleton
class ModelRepository @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * 获取模型列表
     * @param companyId 公司ID
     * @param keyword 搜索关键字（可选）
     * @return Result<List<Model>>
     */
    suspend fun getModelList(companyId: String, keyword: String? = null): Result<List<ChatModel>> {
        return try {
            val response = apiService.getModelList(companyId, keyword)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val models = body.data ?: emptyList()
                    Log.d("ModelRepository", "获取模型列表成功，共 ${models.size} 条")
                    Result.success(models)
                } else {
                    val errorMsg = body?.message ?: "获取模型列表失败"
                    Log.e("ModelRepository", "获取模型列表失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ModelRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ModelRepository", "获取模型列表异常", e)
            Result.failure(e)
        }
    }

    /**
     * 添加模型
     * @param request 添加模型请求
     * @return Result<Int> 影响行数
     */
    suspend fun addModel(request: AddModelRequest): Result<Int> {
        return try {
            val response = apiService.addModel(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val data = body.data ?: 0
                    Log.d("ModelRepository", "添加模型成功，影响行数: $data")
                    Result.success(data)
                } else {
                    val errorMsg = body?.message ?: "添加模型失败"
                    Log.e("ModelRepository", "添加模型失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ModelRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ModelRepository", "添加模型异常", e)
            Result.failure(e)
        }
    }

    /**
     * 更新模型
     * @param request 更新模型请求
     * @return Result<Int> 影响行数
     */
    suspend fun updateModel(request: UpdateModelRequest): Result<Int> {
        return try {
            val response = apiService.updateModel(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val data = body.data ?: 0
                    Log.d("ModelRepository", "更新模型成功，影响行数: $data")
                    Result.success(data)
                } else {
                    val errorMsg = body?.message ?: "更新模型失败"
                    Log.e("ModelRepository", "更新模型失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ModelRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ModelRepository", "更新模型异常", e)
            Result.failure(e)
        }
    }

    /**
     * 删除模型
     * @param modelId 模型ID
     * @return Result<Int> 影响行数
     */
    suspend fun deleteModel(modelId: String): Result<Int> {
        return try {
            val response = apiService.deleteModel(modelId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val data = body.data ?: 0
                    Log.d("ModelRepository", "删除模型成功，影响行数: $data")
                    Result.success(data)
                } else {
                    val errorMsg = body?.message ?: "删除模型失败"
                    Log.e("ModelRepository", "删除模型失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ModelRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ModelRepository", "删除模型异常", e)
            Result.failure(e)
        }
    }
}