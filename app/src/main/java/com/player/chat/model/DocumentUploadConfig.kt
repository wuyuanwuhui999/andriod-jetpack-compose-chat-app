package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 下拉选项通用模型
 * @param value 提交给后端的字段值（如 splitMethod、permission 的取值）
 * @param label 界面展示文案
 */
data class DropdownOption(
    @SerializedName("value") val value: String,
    @SerializedName("label") val label: String
)

/**
 * 文档上传设置相关常量
 * 统一维护 permission、splitMethod 的取值与默认值，避免各层硬编码字符串
 */
object DocumentUploadConfig {

    // ---------------- 文档权限 permission ----------------
    /** 私密 */
    const val PERMISSION_PRIVATE = "private"
    /** 租户内公开 */
    const val PERMISSION_TENANT = "tenant"
    /** 公司内公开 */
    const val PERMISSION_COMPANY = "company"

    // ---------------- 分割方式 splitMethod ----------------
    /** 递归字符分割（推荐） */
    const val SPLIT_METHOD_RECURSIVE = "recursive"
    /** 按段落分割 */
    const val SPLIT_METHOD_PARAGRAPH = "paragraph"
    /** 按句子分割 */
    const val SPLIT_METHOD_SENTENCE = "sentence"
    /** 固定长度分割 */
    const val SPLIT_METHOD_FIXED = "fixed"

    /** 固定长度分割时，分割大小的默认值 */
    const val DEFAULT_CHUNK_SIZE = 1000

    /** 权限默认值：私密 */
    const val DEFAULT_PERMISSION = PERMISSION_PRIVATE

    /** 分割方式默认值：递归字符分割 */
    const val DEFAULT_SPLIT_METHOD = SPLIT_METHOD_RECURSIVE

    /** 权限下拉选项：private-私密，tenant-租户内公开，company-公司内公开 */
    val PERMISSION_OPTIONS: List<DropdownOption> = listOf(
        DropdownOption(value = PERMISSION_PRIVATE, label = "私密"),
        DropdownOption(value = PERMISSION_TENANT, label = "租户内公开"),
        DropdownOption(value = PERMISSION_COMPANY, label = "公司内公开")
    )

    /**
     * 已上传文档的权限选项（"修改权限"对话框使用）
     * 与上传时的权限取值保持一致，便于统一维护
     */
    val DOC_PERMISSION_OPTIONS: List<DropdownOption> = PERMISSION_OPTIONS

    /** 分割模式下拉选项 */
    val SPLIT_METHOD_OPTIONS: List<DropdownOption> = listOf(
        DropdownOption(value = SPLIT_METHOD_RECURSIVE, label = "递归字符分割（推荐）"),
        DropdownOption(value = SPLIT_METHOD_PARAGRAPH, label = "按段落分割"),
        DropdownOption(value = SPLIT_METHOD_SENTENCE, label = "按句子分割"),
        DropdownOption(value = SPLIT_METHOD_FIXED, label = "固定长度分割")
    )

    /**
     * 根据权限 value 获取展示文案
     * @param value 权限值，如 private
     * @return 展示文案，未匹配时返回空串
     */
    fun permissionLabel(value: String?): String =
        PERMISSION_OPTIONS.firstOrNull { it.value == value }?.label ?: ""

    /**
     * 根据分割方式 value 获取展示文案
     * @param value 分割方式值，如 recursive
     * @return 展示文案，未匹配时返回空串
     */
    fun splitMethodLabel(value: String?): String =
        SPLIT_METHOD_OPTIONS.firstOrNull { it.value == value }?.label ?: ""
}

/**
 * 上传文档设置参数
 * 说明：tenantId、directoryId 由 URL 路径改为请求体（multipart 表单）字段，
 * 与 splitMethod、chunkSize、permission 一起提交给 uploadDoc 接口
 *
 * @param tenantId 租户ID
 * @param directoryId 目录ID
 * @param splitMethod 分割方式：recursive / paragraph / sentence / fixed
 * @param chunkSize 分割大小，仅当 splitMethod = fixed 时生效
 * @param permission 文档权限：private / tenant / company
 */
data class UploadDocumentRequest(
    @SerializedName("tenantId") val tenantId: String,
    @SerializedName("directoryId") val directoryId: String,
    @SerializedName("splitMethod") val splitMethod: String = DocumentUploadConfig.DEFAULT_SPLIT_METHOD,
    @SerializedName("chunkSize") val chunkSize: Int = DocumentUploadConfig.DEFAULT_CHUNK_SIZE,
    @SerializedName("permission") val permission: String = DocumentUploadConfig.DEFAULT_PERMISSION
)
