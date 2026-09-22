package com.player.chat

import com.player.chat.network.ApiService
import com.player.chat.repository.ChatRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.atomic.AtomicReference

/**
 * uploadDoc 请求的"真实执行"验证（不联网）
 *
 * 用 OkHttp 拦截器把请求截获下来喂一个假响应，因此走的是真实的 Retrofit 参数拼装 + OkHttp multipart 序列化，
 * 能直接看到后端将会收到哪些表单字段与值。
 * 反射契约测试（ApiDocumentContractTest）只能证明注解形状，这里进一步证明 ChatRepository -> ApiService
 * 真正拼出来的线上内容（含新增的 companyId）。
 */
class UploadDocMultipartWireTest {

    /** 造一个用假响应的 ChatRepository，同时把真实请求与请求体记录下来 */
    private fun buildRepository(
        capturedRequest: AtomicReference<Request?>,
        capturedBody: AtomicReference<RequestBody?>
    ): ChatRepository {
        val interceptor = Interceptor { chain ->
            val request = chain.request()
            capturedRequest.set(request)
            capturedBody.set(request.body)
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(
                    """{"data":1,"token":null,"status":"SUCCESS","msg":"上传成功","total":null}"""
                        .toResponseBody("application/json".toMediaType())
                )
                .build()
        }
        val api = Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(OkHttpClient.Builder().addInterceptor(interceptor).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        return ChatRepository(api)
    }

    /** multipart 请求体 -> { 字段名: 字段值 } */
    private fun formFields(body: RequestBody): Map<String, String> {
        val multipart = body as MultipartBody
        return multipart.parts.associate { part ->
            val disposition = part.headers?.get("Content-Disposition").orEmpty()
            val name = Regex("name=\"([^\"]+)\"").find(disposition)?.groupValues?.get(1).orEmpty()
            val buffer = Buffer()
            part.body.writeTo(buffer)
            name to buffer.readUtf8()
        }
    }

    @Test
    fun uploadDocument_wireContainsCompanyIdAndAllOtherFields() {
        val capturedRequest = AtomicReference<Request?>(null)
        val capturedBody = AtomicReference<RequestBody?>(null)
        val repository = buildRepository(capturedRequest, capturedBody)

        val file = File.createTempFile("upload_wire_", ".txt").apply { writeText("hello-doc") }
        try {
            val result = runBlocking {
                repository.uploadDocument(
                    tenantId = "t1",
                    companyId = "c1",
                    directoryId = "d1",
                    file = file,
                    splitMethod = "fixed",
                    chunkSize = 800,
                    permission = "tenant"
                )
            }
            assertTrue("假响应为上传成功，Repository 应返回成功: $result", result.isSuccess)

            // 1) 请求行：POST + 路径无占位符、无查询参数（参数都在表单里）
            val request = capturedRequest.get()!!
            assertEquals("POST", request.method)
            assertEquals("/service/chat/uploadDoc", request.url.encodedPath)
            assertEquals("URL 不应带查询参数", null, request.url.query)

            // 2) multipart 表单内容
            val body = capturedBody.get()!!
            assertTrue("uploadDoc 应为 multipart 请求体，实际 ${body.javaClass}", body is MultipartBody)
            val fields = formFields(body)
            assertEquals(
                "表单字段集合应与后端约定一致",
                setOf("tenantId", "companyId", "directoryId", "splitMethod", "chunkSize", "permission", "file"),
                fields.keys
            )
            assertEquals("tenantId 的值不对", "t1", fields["tenantId"])
            assertEquals("companyId 的值没有正确带上去", "c1", fields["companyId"])
            assertEquals("directoryId 的值不对", "d1", fields["directoryId"])
            assertEquals("splitMethod 的值不对", "fixed", fields["splitMethod"])
            assertEquals("chunkSize 的值不对", "800", fields["chunkSize"])
            assertEquals("permission 的值不对", "tenant", fields["permission"])
            assertEquals("文件内容不对", "hello-doc", fields["file"])

            // 3) 文件 part 仍带文件名
            val disposition = (body as MultipartBody).parts
                .first { it.headers?.get("Content-Disposition").orEmpty().contains("name=\"file\"") }
                .headers?.get("Content-Disposition").orEmpty()
            assertTrue("文件 part 应带 filename: $disposition", disposition.contains("filename=\"${file.name}\""))
        } finally {
            file.delete()
        }
    }

    /** 默认值路径：只给必填参数时，表单里仍应有默认的 splitMethod/chunkSize/permission */
    @Test
    fun uploadDocument_usesDefaultsForOptionalFields() {
        val capturedRequest = AtomicReference<Request?>(null)
        val capturedBody = AtomicReference<RequestBody?>(null)
        val repository = buildRepository(capturedRequest, capturedBody)

        val file = File.createTempFile("upload_wire_default_", ".txt").apply { writeText("x") }
        try {
            runBlocking {
                repository.uploadDocument(
                    tenantId = "t1",
                    companyId = "c1",
                    directoryId = "d1",
                    file = file
                )
            }
            val fields = formFields(capturedBody.get()!!)
            assertEquals("recursive", fields["splitMethod"])
            assertEquals("1000", fields["chunkSize"])
            assertEquals("private", fields["permission"])
        } finally {
            file.delete()
        }
    }
}
