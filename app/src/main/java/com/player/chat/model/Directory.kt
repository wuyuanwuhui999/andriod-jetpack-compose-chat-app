package com.player.chat.model

import com.google.gson.annotations.SerializedName

data class Directory(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("userId")
    val userId: String? = null,

    @SerializedName("directory")
    val directory: String,

    @SerializedName("tenantId")
    val tenantId: String,

    @SerializedName("createTime")
    val createTime: String? = null,

    @SerializedName("updateTime")
    val updateTime: String? = null
)

data class DirectoryListResponse(
    @SerializedName("data")
    val data: List<Directory>? = null,

    @SerializedName("status")
    val status: String,

    @SerializedName("msg")
    val message: String?
)

data class CreateDirectoryRequest(
    val directory: String,
    val tenantId: String
)

data class UploadDocResponse(
    @SerializedName("data")
    val data: String? = null,

    @SerializedName("status")
    val status: String,

    @SerializedName("msg")
    val message: String?
)