package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 修改文档权限请求体
 * 接口：PUT /service/chat/updateDocPermission
 * 说明：docId 不再拼接在 URL 路径上，与 permission 一起放在请求体中提交
 *
 * @param docId 文档ID
 * @param permission 文档权限：private-私密 / tenant-租户内公开 / company-公司内公开
 */
data class UpdateDocPermissionRequest(
    @SerializedName("docId") val docId: String,
    @SerializedName("permission") val permission: String
)
