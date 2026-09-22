package com.player.chat

import com.player.chat.model.ApiResponse
import com.player.chat.utils.ApiResultUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * 文档操作接口（修改权限 / 删除文档）返回解析的行为测试
 * 需求约定：data > 0 表示成功，成功与失败都提示后端 msg
 */
class ApiResultUtilsTest {

    /** 构造一个 HTTP 200 的响应体 */
    private fun ok(data: Int?, status: String, msg: String?) =
        Response.success(ApiResponse(data = data, token = null, status = status, msg = msg, total = null))

    /** data > 0 且 status=SUCCESS：成功，并提示后端 msg */
    @Test
    fun dataGreaterThanZero_isSuccess_withBackendMsg() {
        val result = ApiResultUtils.docOperationResult(ok(1, "SUCCESS", "修改成功"), "兜底成功", "兜底失败")
        assertTrue(result.isSuccess)
        assertEquals("修改成功", result.getOrNull())
    }

    /** data = 0：即使 status=SUCCESS 也视为失败，提示后端 msg */
    @Test
    fun dataZero_isFailure_withBackendMsg() {
        val result = ApiResultUtils.docOperationResult(ok(0, "SUCCESS", "无操作权限"), "兜底成功", "兜底失败")
        assertTrue(result.isFailure)
        assertEquals("无操作权限", result.exceptionOrNull()?.message)
    }

    /** status != SUCCESS：失败，提示后端 msg */
    @Test
    fun nonSuccessStatus_isFailure_withBackendMsg() {
        val result = ApiResultUtils.docOperationResult(ok(null, "FAIL", "文档不存在"), "兜底成功", "兜底失败")
        assertTrue(result.isFailure)
        assertEquals("文档不存在", result.exceptionOrNull()?.message)
    }

    /** HTTP 非 2xx：失败，回退到兜底提示 */
    @Test
    fun httpError_isFailure_withFallbackMsg() {
        val error = Response.error<ApiResponse<Int>>(500, "".toResponseBody("application/json".toMediaType()))
        val result = ApiResultUtils.docOperationResult(error, "兜底成功", "删除文档失败")
        assertTrue(result.isFailure)
        assertEquals("删除文档失败", result.exceptionOrNull()?.message)
    }

    /** 成功但后端未返回 msg：回退到兜底成功提示 */
    @Test
    fun successWithoutMsg_usesSuccessFallback() {
        val result = ApiResultUtils.docOperationResult(ok(2, "SUCCESS", null), "删除成功", "兜底失败")
        assertTrue(result.isSuccess)
        assertEquals("删除成功", result.getOrNull())
    }
}
