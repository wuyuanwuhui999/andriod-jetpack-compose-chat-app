package com.player.chat.repository

import android.util.Log
import com.player.chat.model.ApiResponse
import com.player.chat.model.Tenant
import com.player.chat.model.TenantUser
import com.player.chat.network.ApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TenantRepository @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * 获取租户下的用户列表
     * @param tenantId 租户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return Result<List<TenantUser>>
     */
    suspend fun getTenantUserList(
        tenantId: String,
        pageNum: Int,
        pageSize: Int
    ): Result<List<TenantUser>> {
        return try {
            val response = apiService.getTenantUserList(tenantId, pageNum, pageSize)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    Result.success(body.data ?: emptyList())
                } else {
                    Result.failure(Exception(body?.message ?: "获取租户用户列表失败"))
                }
            } else {
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("TenantRepository", "获取租户用户列表异常", e)
            Result.failure(e)
        }
    }

    /**
     * 获取用户加入的租户列表
     * @return Result<List<Tenant>>
     */
    suspend fun getUserTenantList(): Result<List<Tenant>> {
        return try {
            val response = apiService.getUserTenantList()

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    Result.success(body.data ?: emptyList())
                } else {
                    Result.failure(Exception(body?.message ?: "获取租户列表失败"))
                }
            } else {
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("TenantRepository", "获取租户列表异常", e)
            Result.failure(e)
        }
    }

    /**
     * 根据租户ID获取租户信息
     * 注意：如果后端没有单独提供此接口，可以使用 getUserTenantList 再过滤
     * @param tenantId 租户ID
     * @return Result<Tenant?>
     */
    suspend fun getTenantById(tenantId: String): Result<Tenant?> {
        return try {
            val result = getUserTenantList()
            if (result.isSuccess) {
                val tenant = result.getOrNull()?.find { it.id == tenantId }
                Result.success(tenant)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("获取租户信息失败"))
            }
        } catch (e: Exception) {
            Log.e("TenantRepository", "获取租户信息异常", e)
            Result.failure(e)
        }
    }

    /**
     * 添加租户用户（预留接口）
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @param roleType 角色类型
     * @return Result<TenantUser>
     */
    suspend fun addTenantUser(
        tenantId: String,
        userId: String,
        roleType: Int
    ): Result<TenantUser> {
        // TODO: 当后端提供添加租户用户接口时实现
        return Result.failure(UnsupportedOperationException("接口暂未实现"))
    }

    /**
     * 移除租户用户（预留接口）
     * @param tenantUserId 租户用户关联ID
     * @return Result<Boolean>
     */
    suspend fun removeTenantUser(tenantUserId: String): Result<Boolean> {
        // TODO: 当后端提供移除租户用户接口时实现
        return Result.failure(UnsupportedOperationException("接口暂未实现"))
    }

    /**
     * 更新租户用户角色（预留接口）
     * @param tenantUserId 租户用户关联ID
     * @param roleType 新角色类型
     * @return Result<TenantUser>
     */
    suspend fun updateTenantUserRole(
        tenantUserId: String,
        roleType: Int
    ): Result<TenantUser> {
        // TODO: 当后端提供更新租户用户角色接口时实现
        return Result.failure(UnsupportedOperationException("接口暂未实现"))
    }
}