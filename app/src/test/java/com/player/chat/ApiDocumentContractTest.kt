package com.player.chat

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.player.chat.model.UpdateDocPermissionRequest
import com.player.chat.model.UploadDocumentRequest
import com.player.chat.network.ApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
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

    /** 实际序列化出的请求体 JSON 形状：{"docId":..., "permission":...} */
    @Test
    fun updateDocPermissionRequest_jsonShape() {
        val json = Gson().toJson(UpdateDocPermissionRequest(docId = "doc-1", permission = "tenant"))
        val obj = JsonParser.parseString(json).asJsonObject
        assertEquals("请求体字段应只有 docId 与 permission: $json",
            setOf("docId", "permission"), obj.keySet())
        assertEquals("doc-1", obj.get("docId").asString)
        assertEquals("tenant", obj.get("permission").asString)
    }

    /** uploadDoc：tenantId/companyId/directoryId 等参数保持放在 body（multipart 表单），URL 无路径占位符 */
    @Test
    fun uploadDocument_paramsStayInMultipartBody() {
        val m = apiMethod("uploadDocument")
        val post = m.annotations.filterIsInstance<POST>().single()
        assertEquals("/service/chat/uploadDoc", post.value)
        assertFalse("URL 不应有路径占位符: ${post.value}", post.value.contains("{"))
        assertTrue("uploadDoc 应为 multipart", m.annotations.any { it is Multipart })

        // 具名表单字段集合必须与后端约定完全一致（含新增的 companyId）
        val namedParts = m.parameters
            .mapNotNull { it.getAnnotation(Part::class.java)?.value?.takeIf { v -> v.isNotBlank() } }
        assertEquals(
            "uploadDoc 表单字段集合应与约定一致",
            setOf("tenantId", "companyId", "directoryId", "splitMethod", "chunkSize", "permission"),
            namedParts.toSet()
        )

        // 具名字段都应是文本 RequestBody
        val namedTypes = m.parameters
            .filter { it.getAnnotation(Part::class.java)?.value?.isNotBlank() == true }
            .map { it.type }
        assertTrue("具名字段应为 RequestBody，实际: $namedTypes", namedTypes.all { it == RequestBody::class.java })

        // 文件 part：只有一个且不带名字（注意 suspend 函数末尾还有一个无注解的 Continuation 参数，不能算进去）
        val fileParts = m.parameters.filter { p ->
            val part = p.getAnnotation(Part::class.java)
            part != null && part.value.isBlank()
        }
        assertEquals("应且仅应有一个文件 part", 1, fileParts.size)
        assertEquals("文件 part 类型应为 MultipartBody.Part",
            MultipartBody.Part::class.java, fileParts.single().type)

        assertFalse("uploadDoc 不应有 @Path 参数", paramAnnotations(m).any { it is Path })
    }

    /** uploadDoc 请求体的线上字段名：含新增 companyId，且集合与后端约定一致 */
    @Test
    fun uploadDocumentRequest_wireFields() {
        val wireNames = UploadDocumentRequest::class.java.declaredFields
            .mapNotNull { it.getAnnotation(SerializedName::class.java)?.value }
            .toSet()
        assertEquals(
            "uploadDoc 请求体字段应为约定集合: $wireNames",
            setOf("tenantId", "companyId", "directoryId", "splitMethod", "chunkSize", "permission"),
            wireNames
        )
    }

    /** deleteDoc：本次未改动，docId 仍在 URL 路径上且为 DELETE */
    @Test
    fun deleteDocument_keepsDocIdInPath() {
        val m = apiMethod("deleteDocument")
        assertEquals("/service/chat/deleteDoc/{docId}", m.annotations.filterIsInstance<DELETE>().single().value)
        assertTrue("docId 仍应为路径参数",
            paramAnnotations(m).any { it is Path && it.value == "docId" })
    }

    /** getPublicDocList：GET + tenantId/companyId 作为 URL 查询参数，无路径占位符、无 body */
    @Test
    fun getPublicDocList_usesQueryParams() {
        val m = apiMethod("getPublicDocList")
        val get = m.annotations.filterIsInstance<GET>().single()
        assertEquals("/service/chat/getPublicDocList", get.value)
        assertFalse("URL 不应有路径占位符: ${get.value}", get.value.contains("{"))
        assertFalse("URL 不应把参数拼死在里面: ${get.value}", get.value.contains("?"))

        val anns = paramAnnotations(m)
        assertTrue("tenantId 应为查询参数", anns.any { it is Query && it.value == "tenantId" })
        assertTrue("companyId 应为查询参数", anns.any { it is Query && it.value == "companyId" })
        assertFalse("不应有 @Path 参数", anns.any { it is Path })
        assertFalse("不应有 @Body 参数", anns.any { it is Body })
        assertEquals("查询参数应只有 tenantId 与 companyId", 2,
            anns.filterIsInstance<Query>().size)
    }
}
