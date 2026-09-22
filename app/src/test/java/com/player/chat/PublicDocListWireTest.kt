package com.player.chat

import com.player.chat.network.ApiService
import com.player.chat.repository.ChatRepository
import com.player.chat.utils.PublicDocumentUtils
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.atomic.AtomicReference

/**
 * getPublicDocList（公共文档页签）请求的"真实执行"验证（不联网）
 *
 * 用假响应拦截器把请求截获：走的是真实的 Retrofit 参数拼装 + Gson 解析，
 * 因此能证明 URL 上确实带了 tenantId/companyId 查询参数、以及返回的文档带 directoryName 并能按目录名分组。
 */
class PublicDocListWireTest {

    /** 返回体：3 篇文档，分布在两个公共目录里 */
    private val publicDocsJson = """
        {
          "data": [
            {"id":"p1","name":"制度.pdf","ext":"pdf","userId":"u1","createTime":"2026-01-01 00:00:00",
             "updateTime":"2026-01-01 00:00:00","directoryId":"dir-1","directoryName":"公共目录A","permission":"company"},
            {"id":"p2","name":"流程.pdf","ext":"pdf","userId":"u1","createTime":"2026-01-01 00:00:00",
             "updateTime":"2026-01-01 00:00:00","directoryId":"dir-1","directoryName":"公共目录A","permission":"company"},
            {"id":"p3","name":"手册.pdf","ext":"pdf","userId":"u2","createTime":"2026-01-01 00:00:00",
             "updateTime":"2026-01-01 00:00:00","directoryId":"dir-2","directoryName":"公共目录B","permission":"tenant"}
          ],
          "token": null, "status": "SUCCESS", "msg": "查询成功", "total": null
        }
    """.trimIndent()

    private fun buildRepository(
        capturedRequest: AtomicReference<Request?>,
        responseJson: String
    ): ChatRepository {
        val interceptor = Interceptor { chain ->
            val request = chain.request()
            capturedRequest.set(request)
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseJson.toResponseBody("application/json".toMediaType()))
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

    @Test
    fun getPublicDocList_sendsQueryParamsAndGroupsByDirectoryName() {
        val captured = AtomicReference<Request?>(null)
        val repository = buildRepository(captured, publicDocsJson)

        val result = runBlocking { repository.getPublicDocList(tenantId = "tenant-1", companyId = "company-1") }

        // 1) 真实请求：GET /service/chat/getPublicDocList?tenantId=...&companyId=...
        val request = captured.get()!!
        assertEquals("GET", request.method)
        assertEquals("/service/chat/getPublicDocList", request.url.encodedPath)
        assertEquals("tenantId 查询参数不对", "tenant-1", request.url.queryParameter("tenantId"))
        assertEquals("companyId 查询参数不对", "company-1", request.url.queryParameter("companyId"))

        // 2) 解析：全部文档一次返回，且带 directoryName
        assertTrue("应解析成功: $result", result.isSuccess)
        val documents = result.getOrNull()!!
        assertEquals("应返回全部公共文档", 3, documents.size)
        assertEquals("公共目录A", documents[0].directoryName)
        assertEquals("公共目录B", documents[2].directoryName)

        // 3) 分组：按 directoryName 分组，展开时不再请求接口（分组数据已全部在本地）
        val groups = PublicDocumentUtils.groupByDirectoryName(documents)
        assertEquals(listOf("公共目录A", "公共目录B"), groups.keys.toList())
        assertEquals(listOf("p1", "p2"), groups.getValue("公共目录A").map { it.id })
        assertEquals(listOf("p3"), groups.getValue("公共目录B").map { it.id })
    }

    /** 业务失败（status != SUCCESS）时把后端 msg 带回上层，便于 Toast 提示 */
    @Test
    fun getPublicDocList_businessFailureCarriesBackendMsg() {
        val captured = AtomicReference<Request?>(null)
        val repository = buildRepository(
            captured,
            """{"data":null,"token":null,"status":"FAIL","msg":"无权限查询公共文档","total":null}"""
        )

        val result = runBlocking { repository.getPublicDocList(tenantId = "t1", companyId = "c1") }

        assertTrue("业务失败应返回 failure", result.isFailure)
        assertEquals("无权限查询公共文档", result.exceptionOrNull()?.message)
    }
}
