package com.player.chat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.local.DataStoreManager
import com.player.chat.model.Company
import com.player.chat.repository.CompanyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 公司选择页面 ViewModel
 */
@HiltViewModel
class CompanyViewModel @Inject constructor(
    private val companyRepository: CompanyRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _companyList = MutableStateFlow<List<Company>>(emptyList())
    val companyList: StateFlow<List<Company>> = _companyList.asStateFlow()

    private val _selectedCompany = MutableStateFlow<Company?>(null)
    val selectedCompany: StateFlow<Company?> = _selectedCompany.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 标记是否从登录页进入（用于控制返回按钮显示）
    private val _isFromLogin = MutableStateFlow(false)
    val isFromLogin: StateFlow<Boolean> = _isFromLogin.asStateFlow()

    /**
     * 加载公司列表
     * 流程：
     * 1. 获取当前用户的 userId
     * 2. 从缓存获取已保存的 companyId
     * 3. 调用接口获取公司列表
     * 4. 根据缓存的 companyId 自动选中对应的公司
     */
    fun loadCompanyList() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // 1. 获取当前用户
                val currentUser = dataStoreManager.getUser().firstOrNull()
                if (currentUser == null) {
                    Log.e("CompanyViewModel", "用户未登录")
                    _isLoading.value = false
                    return@launch
                }
                
                // 2. 从缓存获取保存的 companyId（拼接了 userId）
                val savedCompanyKey = "${PreferenceKeys.COMPANY_ID}_${currentUser.id}"
                val cachedCompanyId = dataStoreManager.getString(savedCompanyKey).firstOrNull()
                
                // 3. 调用接口获取公司列表
                val result = companyRepository.getCompanyList()
                
                if (result.isSuccess) {
                    val companies = result.getOrNull() ?: emptyList()
                    _companyList.value = companies
                    
                    // 4. 根据缓存的 companyId 自动选中对应的公司
                    if (!cachedCompanyId.isNullOrBlank()) {
                        val matchedCompany = companies.find { it.id == cachedCompanyId }
                        if (matchedCompany != null) {
                            _selectedCompany.value = matchedCompany
                            Log.d("CompanyViewModel", "自动选中公司: ${matchedCompany.name}")
                        } else {
                            Log.d("CompanyViewModel", "缓存的 companyId 未找到对应公司")
                        }
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("CompanyViewModel", "获取公司列表失败: ${error?.message}")
                }
            } catch (e: Exception) {
                Log.e("CompanyViewModel", "加载公司列表异常", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 选择公司
     */
    fun selectCompany(company: Company) {
        _selectedCompany.value = company
    }

    /**
     * 确认选择
     * 保存公司信息到缓存，并更新当前租户信息
     * @return 是否保存成功
     */
    suspend fun confirmSelection(): Boolean {
        val selectedCompany = _selectedCompany.value ?: return false
        
        return try {
            // 1. 获取当前用户
            val currentUser = dataStoreManager.getUser().firstOrNull()
            if (currentUser == null) {
                Log.e("CompanyViewModel", "用户未登录，无法保存公司信息")
                return false
            }
            
            // 2. 保存 companyId 到缓存（拼接 userId）
            val companyKey = "${PreferenceKeys.COMPANY_ID}_${currentUser.id}"
            dataStoreManager.saveString(companyKey, selectedCompany.id)
            
            // 3. 保存完整的公司信息
            dataStoreManager.saveCurrentCompany(selectedCompany)
            
            Log.d("CompanyViewModel", "公司信息保存成功: ${selectedCompany.name}")
            true
        } catch (e: Exception) {
            Log.e("CompanyViewModel", "保存公司信息失败", e)
            false
        }
    }
    
    /**
     * 设置是否从登录页进入
     */
    fun setFromLogin(isFromLogin: Boolean) {
        _isFromLogin.value = isFromLogin
    }
    
    /**
     * 缓存 Key 常量
     */
    private object PreferenceKeys {
        const val COMPANY_ID = "company_id"
    }
}