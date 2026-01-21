package com.player.chat.repository

import com.player.chat.model.ChatModel
import com.player.chat.network.ApiService
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getModelList(): Result<List<ChatModel>> {
        return try {
            val response = apiService.getModelList()
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "获取模型列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}