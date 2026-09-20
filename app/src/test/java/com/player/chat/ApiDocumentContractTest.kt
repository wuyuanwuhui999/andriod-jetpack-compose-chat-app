package com.player.chat

import com.google.gson.annotations.SerializedName
import com.player.chat.model.UpdateDocPermissionRequest
import com.player.chat.network.ApiService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import java.lang.reflect.Method

/**
 * ApiService 接口契约测试（反射读取 Retrofit 注解，真实验证 URL 与参数位置）
 * 重点：updateDocPermission 的 docId 必须放在请求体，URL 上不能残留路径占位符
 */
class ApiDocumentContractTest {

    private fun apiMethod(name: String): Method =
        ApiService::class.java.declaredMethods.first { it.name == name && !it.isSynthetic }

    private fun paramAnnotations(m: Method) = m.parameters.flatMap { it.annotations.toList() }

    /** updateDocPermission：URL 无路径参数，docId 与 permission 都通过 @Body 提交 */
    @Test
    fun updateDocPermission_docIdIsInBody_notInUrl() {
        val m = apiMethod("updateDocPermission")

        val put = m.annotations.filterIsInstance<PUT>().single()
        assertEquals("/service/chat/updateDocPermission", put.value)
        assertFalse("URL 不应再有路径占位符: ${put.value}", put.value.contains("{"))

        val anns = paramAnnotations(m)
        assertEquals("应且仅应有一个 @Body", 1, anns.count { it is Body })
        assertTrue("@Body 参数类型应为 UpdateDocPermissionRequest",
            m.parameterTypes.contains(UpdateDocPermissionRequest::class.java))
        assertFalse("不应再有 @Path 参数", anns.any { it is Path })
    }

    /** 请求体的线上字段名必须包含 docId 与 permission */
    @Test
    fun updateDocPermissionRequest_wireFields() {
        val wireNames = UpdateDocPermissionRequest::class.java.declaredFields
            .mapNotNull { it.getAnnotation(SerializedName::class.java)?.value }
            .toSet()
        assertTrue("请求体缺少 docId 字段: $wireNames", wireNames.contains("docId"))
        assertTrue("请求体缺少 permission 字段: $wireNames", wireNames.contains("permission"))
    }

    /** uploadDoc：tenantId/directoryId 等参数保持放在 body（multipart 表单），URL 无路径占位符 */
    @Test
    fun uploadDocument_paramsStayInMultipartBody() {
        val m = apiMethod("uploadDocument")
        val post = m.annotations.filterIsInstance<POST>().single()
        assertEquals("/service/chat/uploadDoc", post.value)
        assertFalse("URL 不应有路径占位符: ${post.value}", post.value.contains("{"))
        assertTrue("uploadDoc 应为 multipart", m.annotations.any { it is Multipart })

        val anns = paramAnnotations(m)
        assertTrue("tenantId 应通过表单字段提交", anns.any { it is Part && it.value == "tenantId" })
        assertTrue("directoryId 应通过表单字段提交", anns.any { it is Part && it.value == "directoryId" })
        assertTrue("splitMethod 应通过表单字段提交", anns.any { it is Part && it.value == "splitMethod" })
        assertTrue("chunkSize 应通过表单字段提交", anns.any { it is Part && it.value == "chunkSize" })
        assertTrue("permission 应通过表单字段提交", anns.any { it is Part && it.value == "permission" })
        assertFalse("uploadDoc 不应有 @Path 参数", anns.any { it is Path })
    }

    /** deleteDoc：本次未改动，docId 仍在 URL 路径上且为 DELETE */
    @Test
    fun deleteDocument_keepsDocIdInPath() {
        val m = apiMethod("deleteDocument")
        assertEquals("/service/chat/deleteDoc/{docId}", m.annotations.filterIsInstance<DELETE>().single().value)
        assertTrue("docId 仍应为路径参数",
            paramAnnotations(m).any { it is Path && it.value == "docId" })
    }
}
