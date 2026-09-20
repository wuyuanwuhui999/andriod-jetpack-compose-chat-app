package com.player.chat

import com.player.chat.model.DocumentUploadConfig
import com.player.chat.model.UploadDocumentRequest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 文档上传设置（section: 权限 permission / 分割方式 splitMethod）单元测试
 * 覆盖下拉选项与后端的取值映射、默认值，防止 UI 与接口约定漂移
 */
class DocumentUploadConfigTest {

    /** 权限下拉项的 value-label 映射与顺序必须与需求一致 */
    @Test
    fun permissionOptions_matchSpec() {
        val actual = DocumentUploadConfig.PERMISSION_OPTIONS.map { it.value to it.label }
        assertEquals(
            listOf(
                "private" to "私密",
                "tenant" to "租户内公开",
                "company" to "公司内公开"
            ),
            actual
        )
    }

    /** 分割模式下拉项的 value-label 映射与顺序必须与需求一致 */
    @Test
    fun splitMethodOptions_matchSpec() {
        val actual = DocumentUploadConfig.SPLIT_METHOD_OPTIONS.map { it.value to it.label }
        assertEquals(
            listOf(
                "recursive" to "递归字符分割（推荐）",
                "paragraph" to "按段落分割",
                "sentence" to "按句子分割",
                "fixed" to "固定长度分割"
            ),
            actual
        )
    }

    /** 已上传文档的权限选项（修改权限对话框）与上传权限保持一致 */
    @Test
    fun docPermissionOptions_matchSpec() {
        val actual = DocumentUploadConfig.DOC_PERMISSION_OPTIONS.map { it.value to it.label }
        assertEquals(
            listOf(
                "private" to "私密",
                "tenant" to "租户内公开",
                "company" to "公司内公开"
            ),
            actual
        )
    }

    /** 默认值：私密 + 递归字符分割 + 分割大小 1000 */
    @Test
    fun defaults_matchSpec() {
        assertEquals("private", DocumentUploadConfig.DEFAULT_PERMISSION)
        assertEquals("recursive", DocumentUploadConfig.DEFAULT_SPLIT_METHOD)
        assertEquals(1000, DocumentUploadConfig.DEFAULT_CHUNK_SIZE)
    }

    /** value -> label 反查，未知值返回空串 */
    @Test
    fun labelLookup() {
        assertEquals("租户内公开", DocumentUploadConfig.permissionLabel("tenant"))
        assertEquals("固定长度分割", DocumentUploadConfig.splitMethodLabel("fixed"))
        assertEquals("", DocumentUploadConfig.permissionLabel("unknown"))
        assertEquals("", DocumentUploadConfig.splitMethodLabel(null))
    }

    /** 上传参数对象未显式赋值时应落到默认值 */
    @Test
    fun uploadRequest_appliesDefaults() {
        val request = UploadDocumentRequest(tenantId = "t1", directoryId = "d1")
        assertEquals("t1", request.tenantId)
        assertEquals("d1", request.directoryId)
        assertEquals(DocumentUploadConfig.DEFAULT_SPLIT_METHOD, request.splitMethod)
        assertEquals(DocumentUploadConfig.DEFAULT_CHUNK_SIZE, request.chunkSize)
        assertEquals(DocumentUploadConfig.DEFAULT_PERMISSION, request.permission)
    }
}
