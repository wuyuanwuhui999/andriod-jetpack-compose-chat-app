package com.player.chat.repository

import android.util.Log
import com.player.chat.model.AddCompanyUserRequest
import com.player.chat.model.Company
import com.player.chat.model.Department
import com.player.chat.model.Position
import com.player.chat.model.SearchUser
import com.player.chat.model.User
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

     /**
     * 获取公司下的用户列表
     * @param companyId 公司ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键字（可选）
     * @return Result<List<User>>
     */
    suspend fun getCompanyUsers(
        companyId: String,
        pageNum: Int,
        pageSize: Int,
        keyword: String? = null
    ): Result<List<User>> {
        return try {
            val response = apiService.getCompanyUsers(companyId, pageSize, pageNum, keyword)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val users = body.data ?: emptyList()
                    Log.d("CompanyRepository", "获取公司用户成功，共 ${users.size} 条")
                    Result.success(users)
                } else {
                    val errorMsg = body?.message ?: "获取公司用户失败"
                    Log.e("CompanyRepository", "获取公司用户失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CompanyRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CompanyRepository", "获取公司用户异常", e)
            Result.failure(e)
        }
    }

    /**
     * 删除公司用户
     * @param userId 用户ID
     * @param companyId 公司ID
     * @return Result<Int> 影响行数
     */
    suspend fun removeUser(userId: String, companyId: String): Result<Int> {
        return try {
            val response = apiService.removeUser(userId, companyId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val data = body.data ?: 0
                    Log.d("CompanyRepository", "删除用户成功，影响行数: $data")
                    Result.success(data)
                } else {
                    val errorMsg = body?.message ?: "删除用户失败"
                    Log.e("CompanyRepository", "删除用户失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CompanyRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CompanyRepository", "删除用户异常", e)
            Result.failure(e)
        }
    }

    /**
     * 搜索用户（带公司过滤）
     * @param companyId 公司ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return Result<List<SearchUser>>
     */
    /**
     * 搜索用户（带公司过滤）
     * @param companyId 公司ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键字（可选）
     * @return Result<List<SearchUser>>
     */
    suspend fun searchUsersWithCompany(
        companyId: String,
        pageNum: Int,
        pageSize: Int,
        keyword: String? = null
    ): Result<List<SearchUser>> {
        return try {
            val response = apiService.searchUsersWithCompany(companyId, pageNum, pageSize, keyword)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val users = body.data ?: emptyList()
                    Log.d("CompanyRepository", "搜索用户成功，共 ${users.size} 条")
                    Result.success(users)
                } else {
                    val errorMsg = body?.message ?: "搜索用户失败"
                    Log.e("CompanyRepository", "搜索用户失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CompanyRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CompanyRepository", "搜索用户异常", e)
            Result.failure(e)
        }
    }

    /**
     * 获取部门列表
     * @param companyId 公司ID
     * @return Result<List<Department>>
     */
    suspend fun getDepartments(companyId: String): Result<List<Department>> {
        return try {
            val response = apiService.getDepartments(companyId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val departments = body.data ?: emptyList()
                    Log.d("CompanyRepository", "获取部门列表成功，共 ${departments.size} 条")
                    Result.success(departments)
                } else {
                    val errorMsg = body?.message ?: "获取部门列表失败"
                    Log.e("CompanyRepository", "获取部门列表失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CompanyRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CompanyRepository", "获取部门列表异常", e)
            Result.failure(e)
        }
    }

    /**
     * 获取职位列表
     * @param departmentId 部门ID
     * @return Result<List<Position>>
     */
    suspend fun getPositions(departmentId: String): Result<List<Position>> {
        return try {
            val response = apiService.getPositions(departmentId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val positions = body.data ?: emptyList()
                    Log.d("CompanyRepository", "获取职位列表成功，共 ${positions.size} 条")
                    Result.success(positions)
                } else {
                    val errorMsg = body?.message ?: "获取职位列表失败"
                    Log.e("CompanyRepository", "获取职位列表失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CompanyRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CompanyRepository", "获取职位列表异常", e)
            Result.failure(e)
        }
    }

    /**
     * 添加公司用户
     * @param request 添加用户请求
     * @return Result<Int> 影响行数
     */
    suspend fun addCompanyUser(request: AddCompanyUserRequest): Result<Int> {
        return try {
            val response = apiService.addCompanyUser(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "SUCCESS") {
                    val data = body.data ?: 0
                    Log.d("CompanyRepository", "添加用户成功，影响行数: $data")
                    Result.success(data)
                } else {
                    val errorMsg = body?.message ?: "添加用户失败"
                    Log.e("CompanyRepository", "添加用户失败: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CompanyRepository", "网络请求失败: ${response.code()}, $errorBody")
                Result.failure(Exception("网络请求失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("CompanyRepository", "添加用户异常", e)
            Result.failure(e)
        }
    }
}