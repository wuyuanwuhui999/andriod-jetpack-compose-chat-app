package com.player.chat.repository

import com.player.chat.model.ChatHistory
import com.player.chat.model.ChatModel
import com.player.chat.model.Directory
import com.player.chat.model.Document
import com.player.chat.model.DocumentUploadConfig
import com.player.chat.model.Prompt
import com.player.chat.model.UpdateDocPermissionRequest
import com.player.chat.model.UpdatePromptRequest
import com.player.chat.network.ApiService
import com.player.chat.utils.ApiResultUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val apiService: ApiService
) {
    // 修改：获取模型列表，添加 companyId 参数
    suspend fun getModelList(companyId: String): Result<List<ChatModel>> {
        return try {
            val response = apiService.getModelList(companyId)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.msg ?: "获取模型列表失败"))
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
                Result.failure(Exception(response.body()?.msg ?: "获取目录列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 创建目录
    /**
     * 创建目录
     * @param directory 目录名称
     * @param tenantId 租户ID
     * @return Result<Directory> 创建成功的目录对象
     */
    suspend fun createDirectory(directory: String, tenantId: String): Result<Directory> {
        return try {
            val response = apiService.createDirectory(
                directory = directory.toRequestBody("text/plain".toMediaTypeOrNull()),
                tenantId = tenantId.toRequestBody("text/plain".toMediaTypeOrNull())
            )
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    // 后端返回data为null时，构造一个本地Directory对象
                    Result.success(Directory(directory = directory, tenantId = tenantId))
                }
            } else {
                Result.failure(Exception(response.body()?.msg ?: "创建目录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 上传文档
     * 说明：tenantId、directoryId 不再拼接在 URL 上，改为和分割参数、权限一起放到请求体（multipart 表单）中
     *
     * @param tenantId 租户ID
     * @param directoryId 目录ID
     * @param file 文件
     * @param splitMethod 分割方式：recursive / paragraph / sentence / fixed，默认 recursive
     * @param chunkSize 分割大小，仅当 splitMethod = fixed 时生效，默认 1000
     * @param permission 文档权限：private / tenant / company，默认 private
     * @return Result<Int> 上传成功的数量
     */
    suspend fun uploadDocument(
        tenantId: String,
        directoryId: String,
        file: File,
        splitMethod: String = DocumentUploadConfig.DEFAULT_SPLIT_METHOD,
        chunkSize: Int = DocumentUploadConfig.DEFAULT_CHUNK_SIZE,
        permission: String = DocumentUploadConfig.DEFAULT_PERMISSION
    ): Result<Int> {
        return try {
            val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val textType = "text/plain".toMediaTypeOrNull()

            val response = apiService.uploadDocument(
                tenantId = tenantId.toRequestBody(textType),
                directoryId = directoryId.toRequestBody(textType),
                splitMethod = splitMethod.toRequestBody(textType),
                chunkSize = chunkSize.toString().toRequestBody(textType),
                permission = permission.toRequestBody(textType),
                file = filePart
            )
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                val data = response.body()?.data ?: 0
                if (data > 0) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("上传失败"))
                }
            } else {
                Result.failure(Exception(response.body()?.msg ?: "上传失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDocListByDirId(tenantId: String, directoryId: String): Result<List<Document>> {
        return try {
            val response = apiService.getDocListByDirId(tenantId, directoryId)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.msg ?: "获取文档列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除文档
     * 说明：调用 DELETE /service/chat/deleteDoc/{docId}，data > 0 视为成功
     *
     * @param docId 文档ID
     * @return Result<String> 成功时携带后端 msg（用于提示语），失败时 exception.message 为后端 msg
     */
    suspend fun deleteDocument(docId: String): Result<String> {
        return try {
            ApiResultUtils.docOperationResult(
                response = apiService.deleteDocument(docId),
                successFallback = "删除成功",
                failFallback = "删除文档失败"
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 修改文档权限
     * 说明：调用 PUT /service/chat/updateDocPermission，docId 与 permission 都放在请求体中，data > 0 视为成功
     *
     * @param docId 文档ID（随 permission 一起放入请求体）
     * @param permission 文档权限：private-私密 / tenant-租户内公开 / company-公司内公开
     * @return Result<String> 成功时携带后端 msg（用于提示语），失败时 exception.message 为后端 msg
     */
    suspend fun updateDocPermission(docId: String, permission: String): Result<String> {
        return try {
            ApiResultUtils.docOperationResult(
                response = apiService.updateDocPermission(
                    UpdateDocPermissionRequest(docId = docId, permission = permission)
                ),
                successFallback = "修改权限成功",
                failFallback = "修改文档权限失败"
            )
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
                Result.failure(Exception(response.body()?.msg ?: "获取会话记录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取提示词
     * @param tenantId 租户ID
     * @param promptId 提示词ID，可空，为空时后端返回默认或最新创建的提示词
     */
    suspend fun getPrompt(tenantId: String, promptId: String? = null): Result<Prompt> {
        return try {
            val response = apiService.getPrompt(tenantId, promptId)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                val prompt = response.body()?.data
                if (prompt != null) {
                    Result.success(prompt)
                } else {
                    Result.failure(Exception("提示词不存在"))
                }
            } else {
                Result.failure(Exception(response.body()?.msg ?: "获取提示词失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 更新提示词
     * @param request 更新提示词请求
     */
    suspend fun updatePrompt(request: UpdatePromptRequest): Result<Boolean> {
        return try {
            val response = apiService.updatePrompt(request)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "更新提示词失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取提示词列表（支持分页和关键字搜索）
     * @param tenantId 租户ID
     * @param keyword 搜索关键字
     * @param pageSize 每页数量
     * @param pageNum 页码
     */
    suspend fun getPromptList(
        tenantId: String,
        keyword: String? = null,
        pageSize: Int = 20,
        pageNum: Int = 1
    ): Result<List<Prompt>> {
        return try {
            val response = apiService.getPromptList(tenantId, keyword, pageSize, pageNum)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.msg ?: "获取提示词列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除提示词（带tenantId）
     * @param promptId 提示词ID
     * @param tenantId 租户ID
     */
    suspend fun deletePromptWithTenant(promptId: String, tenantId: String): Result<Int> {
        return try {
            val response = apiService.deletePromptWithTenant(promptId, tenantId)
            if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                Result.success(response.body()?.data ?: 0)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "删除提示词失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}