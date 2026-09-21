// Document.kt
package com.player.chat.model

import com.google.gson.annotations.SerializedName

data class Document(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("ext")
    val ext: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("createTime")
    val createTime: String,

    @SerializedName("updateTime")
    val updateTime: String,

    @SerializedName("directoryId")
    val directoryId: String,

    @SerializedName("directoryName")
    val directoryName: String,

    // 文档权限：private-私密 / tenant-租户内公开 / company-公司内公开
    // 用于"修改权限"对话框的默认值回显
    // 注意：声明为 var，修改权限后由 DocumentListUtils 直接赋值同步本地列表。
    // 不能用数据类 copy() 同步：后端若未下发 userId/directoryId 等非空字段，
    // Gson 会把 null 写进非空类型字段，copy() 会对所有非空参数做 null 校验并抛异常。
    @SerializedName("permission")
    var permission: String? = null,

    // 本地状态，非后端字段
    var checked: Boolean = false
)