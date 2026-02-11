package com.player.chat.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.player.chat.model.Tenant
import com.player.chat.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.player.chat.model.TenantUser

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class DataStoreManager(private val context: Context) {
    private val gson = Gson()

    // Token
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.TOKEN)] = token
        }
    }

    fun getToken(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.TOKEN)]
        }

    // User Data
    suspend fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.USER_DATA)] = userJson
        }
    }

    fun getUser(): Flow<User?> = context.dataStore.data
        .map { preferences ->
            val userJson = preferences[stringPreferencesKey(PreferenceKeys.USER_DATA)]
            userJson?.let { gson.fromJson(it, User::class.java) }
        }

    // Login Status
    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(PreferenceKeys.IS_LOGGED_IN)] = isLoggedIn
        }
    }

    fun isLoggedIn(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey(PreferenceKeys.IS_LOGGED_IN)] ?: false
        }

    // Clear all data (logout)
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // 租户ID
    suspend fun saveTenantId(tenantId: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.TENANT_ID)] = tenantId
        }
    }

    fun getTenantId(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.TENANT_ID)]
        }

    // 当前租户信息
    suspend fun saveCurrentTenant(tenant: Tenant) {
        val tenantJson = gson.toJson(tenant)
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.CURRENT_TENANT)] = tenantJson
        }
    }

    fun getCurrentTenant(): Flow<Tenant?> = context.dataStore.data
        .map { preferences ->
            val tenantJson = preferences[stringPreferencesKey(PreferenceKeys.CURRENT_TENANT)]
            tenantJson?.let { gson.fromJson(it, Tenant::class.java) }
        }

    // 聊天ID
    suspend fun saveChatId(chatId: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.CHAT_ID)] = chatId
        }
    }

    fun getChatId(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.CHAT_ID)]
        }

    // 语言设置
    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.LANGUAGE)] = language
        }
    }

    fun getLanguage(): Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[stringPreferencesKey(PreferenceKeys.LANGUAGE)] ?: "zh"
        }

    // 思考模式
    suspend fun saveThinkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(PreferenceKeys.THINK_MODE)] = enabled
        }
    }

    fun getThinkMode(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey(PreferenceKeys.THINK_MODE)] ?: false
        }
}