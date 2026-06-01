package com.player.chat.repository

import android.util.Log
import com.player.chat.model.Company
import com.player.chat.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 公司相关数据仓库
 */
@Singleton
class CompanyRepository @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * 获取公司列表
     * @return Result<List<Company>>
     */
    suspend fun getCompanyList(): Result<List<Company>> {
        return try {
            val response = apiService.getCompanyList()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val companies = body.data ?: emptyList()
                    Log.d("CompanyRepository", "获取公司列表成功，共 ${companies.size} 条")
                    Result.success(companies)
                } else {
                    val errorMsg = body?.message ?: "获取公司列表失败"
                    Log.e("CompanyRepository", "获取公司列表失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CompanyRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CompanyRepository", "获取公司列表异常", e)
            Result.failure(e)
        }
    }
}