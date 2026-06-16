// chat/model/Company.kt
package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 公司数据模型
 * 对应接口 /service/company/getCompanyList 返回的公司信息
 */
data class Company(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("status")
    val status: Int,

    @SerializedName("createDate")
    val createDate: String,

    @SerializedName("updateDate")
    val updateDate: String,

    @SerializedName("createdBy")
    val createdBy: String,

    @SerializedName("updatedBy")
    val updatedBy: String,

    /**
     * 用户在当前公司的角色
     * 0: 普通用户
     * 1: 普通管理员
     * 2: 超级管理员
     */
    @SerializedName("role")
    val role: Int = 0
)