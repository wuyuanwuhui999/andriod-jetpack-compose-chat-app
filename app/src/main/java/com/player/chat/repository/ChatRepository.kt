package com.player.chat.repository

import com.player.chat.model.ChatHistory
import com.player.chat.model.ChatModel
import com.player.chat.model.Directory
import com.player.chat.model.Document
import com.player.chat.network.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
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

    // 获取目录列表
    suspend fun getDirectoryList(tenantId: String): Result<List<Directory>> {
        return try {
            val response = apiService.getDirectoryList(tenantId)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "获取目录列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 创建目录
    suspend fun createDirectory(directory: String, tenantId: String): Result<Directory> {
        return try {
            val response = apiService.createDirectory(
                directory = directory.toRequestBody("text/plain".toMediaTypeOrNull()),
                tenantId = tenantId.toRequestBody("text/plain".toMediaTypeOrNull())
            )
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                Result.success(response.body()?.data ?: Directory(
                    directory = directory,
                    tenantId = tenantId
                )
                )
            } else {
                Result.failure(Exception(response.body()?.message ?: "创建目录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 上传文档
    suspend fun uploadDocument(
        tenantId: String,
        directoryId: String,
        file: File
    ): Result<String> {
        return try {
            // 创建Multipart请求
            val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = apiService.uploadDocument(tenantId, directoryId, filePart)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                Result.success(response.body()?.data ?: "上传成功")
            } else {
                Result.failure(Exception(response.body()?.message ?: "上传失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } as Result<String>
    }

    suspend fun getDocListByDirId(tenantId: String, directoryId: String): Result<List<Document>> {
        return try {
            val response = apiService.getDocListByDirId(tenantId, directoryId)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "获取文档列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(docId: String): Result<Int> {
        return try {
            val response = apiService.deleteDocument(docId)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                val deletedCount = response.body()?.data ?: 0
                if (deletedCount > 0) {
                    Result.success(deletedCount)
                } else {
                    Result.failure(Exception("删除失败"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "删除文档失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatHistory(tenantId: String, pageSize: Int, pageNum: Int): Result<List<ChatHistory>> {
        return try {
            val response = apiService.getChatHistory(tenantId, pageSize, pageNum)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "获取会话记录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



}