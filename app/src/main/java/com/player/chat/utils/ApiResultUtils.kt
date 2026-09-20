package com.player.chat.utils

import com.player.chat.model.ApiResponse
import retrofit2.Response

/**
 * 接口返回统一解析工具
 */
object ApiResultUtils {

    /**
     * 解析文档操作类接口（修改权限、删除文档）的返回结果
     * 规则：HTTP 成功 && status == "SUCCESS" && data > 0 才算成功
     *
     * @param response 接口响应
     * @param successFallback 成功但后端未返回 msg 时的兜底提示
     * @param failFallback 失败且后端未返回 msg 时的兜底提示
     * @return Result<String>：成功时携带后端 msg（用于 Toast），失败时 exception.message 同样是后端 msg
     */
    fun docOperationResult(
        response: Response<ApiResponse<Int>>,
        successFallback: String,
        failFallback: String
    ): Result<String> {
        val body = response.body()
        return if (response.isSuccessful && body?.status == "SUCCESS" && (body.data ?: 0) > 0) {
            Result.success(body.message ?: successFallback)
        } else {
            Result.failure(Exception(body?.message ?: failFallback))
        }
    }
}
