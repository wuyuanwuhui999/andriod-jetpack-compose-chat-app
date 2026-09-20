package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 修改文档权限请求体
 * 接口：PUT /service/chat/updateDocPermission/{docId}
 *
 * @param permission 文档权限：private-私密 / tenant-租户内公开 / company-公司内公开
 */
data class UpdateDocPermissionRequest(
    @SerializedName("permission") val permission: String
)
