package com.player.chat.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.player.chat.model.*
import com.player.chat.network.WebSocketManager
import com.player.chat.network.WebSocketMessageHandler
import com.player.chat.chat.repository.UserRepository
import com.player.chat.repository.ChatRepository
import com.player.chat.local.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID
import javax.inject.Inject
import com.player.chat.repository.TenantRepository
import com.player.chat.utils.CommonUtils.formatRelativeTime

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val dataStoreManager: DataStoreManager,
    private val tenantRepository: TenantRepository  // 添加 TenantRepository
) : ViewModel() {
    private val _tenantList = MutableStateFlow<List<Tenant>>(emptyList())
    val tenantList: StateFlow<List<Tenant>> = _tenantList.asStateFlow()

    private val _showTenantDialog = MutableStateFlow(false)
    val showTenantDialog: StateFlow<Boolean> = _showTenantDialog.asStateFlow()

    // 模型相关
    private val _modelList = MutableStateFlow<List<ChatModel>>(emptyList())
    val modelList: StateFlow<List<ChatModel>> = _modelList.asStateFlow()

    private val _selectedModel = MutableStateFlow<ChatModel?>(null)
    val selectedModel: StateFlow<ChatModel?> = _selectedModel.asStateFlow()

    // 聊天相关
    private val _chatList = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatList: StateFlow<List<ChatMessage>> = _chatList.asStateFlow()

    private val _currentTenant = MutableStateFlow<Tenant?>(null)
    val currentTenant: StateFlow<Tenant?> = _currentTenant.asStateFlow()

    private val _chatId = MutableStateFlow<String>("")
    val chatId: StateFlow<String> = _chatId.asStateFlow()

    private val _thinkMode = MutableStateFlow(false)
    val thinkMode: StateFlow<Boolean> = _thinkMode.asStateFlow()

    private val _language = MutableStateFlow("zh")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    // UI状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _showModelDialog = MutableStateFlow(false)
    val showModelDialog: StateFlow<Boolean> = _showModelDialog.asStateFlow()

    private val _showMenuDialog = MutableStateFlow(false)
    val showMenuDialog: StateFlow<Boolean> = _showMenuDialog.asStateFlow()

    private val _showUploadDialog = MutableStateFlow(false)
    val showUploadDialog: StateFlow<Boolean> = _showUploadDialog.asStateFlow()

    private val _directoryList = MutableStateFlow<List<Directory>>(emptyList())
    val directoryList: StateFlow<List<Directory>> = _directoryList.asStateFlow()

    private val _selectedDirectory = MutableStateFlow<Directory?>(null)
    val selectedDirectory: StateFlow<Directory?> = _selectedDirectory.asStateFlow()

    private val _showCreateDirectoryDialog = MutableStateFlow(false)
    val showCreateDirectoryDialog: StateFlow<Boolean> = _showCreateDirectoryDialog.asStateFlow()

    private val _isDirectoryLoading = MutableStateFlow(false)
    val isDirectoryLoading: StateFlow<Boolean> = _isDirectoryLoading.asStateFlow()


    // WebSocket
    private var webSocketManager: WebSocketManager? = null
    private var currentThinkContent = ""
    private var currentResponseContent = ""

    private val _isTenantListLoaded = MutableStateFlow(false)
    val isTenantListLoaded: StateFlow<Boolean> = _isTenantListLoaded.asStateFlow()

    private val _showMyDocumentsDialog = MutableStateFlow(false)
    val showMyDocumentsDialog: StateFlow<Boolean> = _showMyDocumentsDialog.asStateFlow()

    private val _expandedDirectories = MutableStateFlow<Set<String>>(emptySet())
    val expandedDirectories: StateFlow<Set<String>> = _expandedDirectories.asStateFlow()

    private val _directoryDocuments = MutableStateFlow<Map<String, List<Document>>>(emptyMap())
    val directoryDocuments: StateFlow<Map<String, List<Document>>> = _directoryDocuments.asStateFlow()

    private val _isDocumentsLoading = MutableStateFlow(false)
    val isDocumentsLoading: StateFlow<Boolean> = _isDocumentsLoading.asStateFlow()

    private val _chatHistoryList = MutableStateFlow<List<ChatHistory>>(emptyList())
    val chatHistoryList: StateFlow<List<ChatHistory>> = _chatHistoryList.asStateFlow()

    private val _groupedChatHistory = MutableStateFlow<Map<String, List<ChatHistory>>>(emptyMap())
    val groupedChatHistory: StateFlow<Map<String, List<ChatHistory>>> = _groupedChatHistory.asStateFlow()

    private val _isChatHistoryLoading = MutableStateFlow(false)
    val isChatHistoryLoading: StateFlow<Boolean> = _isChatHistoryLoading.asStateFlow()

    private val _hasMoreChatHistory = MutableStateFlow(true)
    val hasMoreChatHistory: StateFlow<Boolean> = _hasMoreChatHistory.asStateFlow()

    private val _currentChatHistoryPage = MutableStateFlow(1)
    val currentChatHistoryPage: StateFlow<Int> = _currentChatHistoryPage.asStateFlow()

    private val _showChatHistoryDialog = MutableStateFlow(false)
    val showChatHistoryDialog: StateFlow<Boolean> = _showChatHistoryDialog.asStateFlow()

    private var pageSize = 20

    init {
        loadTenantInfo()
        loadModelList()
        loadSettings()
        addGreetingMessage()
    }

    private fun loadTenantInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _isTenantListLoaded.value = false

            // 1. 尝试从缓存获取租户ID
            val cachedTenantId = dataStoreManager.getTenantId().firstOrNull()

            // 2. 使用 TenantRepository 获取租户列表
            val result = tenantRepository.getUserTenantList()

            if (result.isSuccess) {
                val tenantList = result.getOrNull() ?: emptyList()
                _tenantList.value = tenantList
                _isTenantListLoaded.value = true

                // 3. 选择租户
                val selectedTenant = if (tenantList.isNotEmpty()) {
                    cachedTenantId?.let { cachedId ->
                        tenantList.find { it.id == cachedId }
                    } ?: tenantList.first()
                } else {
                    DefaultTenant.PERSONAL_SPACE
                }

                _currentTenant.value = selectedTenant

                // 保存当前租户到缓存
                selectedTenant.let {
                    if (it != DefaultTenant.PERSONAL_SPACE) {
                        dataStoreManager.saveTenantId(it.id)
                    }
                    dataStoreManager.saveCurrentTenant(it)
                }
            } else {
                // 获取失败，使用默认租户
                _currentTenant.value = DefaultTenant.PERSONAL_SPACE
                dataStoreManager.saveCurrentTenant(DefaultTenant.PERSONAL_SPACE)
                _tenantList.value = emptyList()
                _isTenantListLoaded.value = true
            }

            _isLoading.value = false
        }
    }


    private fun loadModelList() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = chatRepository.getModelList()
            if (result.isSuccess) {
                _modelList.value = result.getOrNull() ?: emptyList()
                if (_modelList.value.isNotEmpty()) {
                    _selectedModel.value = _modelList.value.first()
                }
            }
            _isLoading.value = false
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // 加载思考模式
            dataStoreManager.getThinkMode().collect { enabled ->
                _thinkMode.value = enabled
            }

            // 加载语言设置
            dataStoreManager.getLanguage().collect { lang ->
                _language.value = lang
            }

            // 加载聊天ID
            dataStoreManager.getChatId().collect { chatId ->
                chatId?.let {
                    _chatId.value = it
                }
            }
        }
    }

    private fun addGreetingMessage() {
        viewModelScope.launch {
            val greeting = when (_language.value) {
                "en" -> "Hello, I am Xiao Wu, your intelligent assistant. How can I help you today?"
                else -> "你好，我是智能助手小吴同学，请问有什么可以帮助您？"
            }

            val greetingMessage = ChatMessage(
                position = PositionEnum.LEFT,
                responseContent = greeting
            )

            _chatList.value = listOf(greetingMessage)
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            if (message.isBlank()) return@launch

            // 添加用户消息到列表
            val userMessage = ChatMessage(
                position = PositionEnum.RIGHT,
                responseContent = message
            )

            _chatList.value = _chatList.value + userMessage
            _isSending.value = true

            // 每次都重新连接WebSocket（如果已有连接会自动关闭）
            connectWebSocket()

            // 生成或使用现有chatId
            val currentChatId = if (_chatId.value.isBlank()) {
                val newChatId = UUID.randomUUID().toString().replace("-", "")
                _chatId.value = newChatId
                dataStoreManager.saveChatId(newChatId)
                newChatId
            } else {
                _chatId.value
            }

            // 准备请求数据
            val token = dataStoreManager.getToken().firstOrNull() ?: ""
            val tenantId = _currentTenant.value?.id ?: ""
            val modelId = _selectedModel.value?.id ?: ""

            val request = WebSocketManager.ChatRequest(
                modelId = modelId,
                token = token,
                chatId = currentChatId,
                tenantId = tenantId,
                prompt = message,
                showThink = _thinkMode.value,
                language = _language.value
            )

            // 发送消息
            webSocketManager?.sendMessage(request)

            // 添加AI回复占位
            val aiMessage = ChatMessage(
                position = PositionEnum.LEFT,
                responseContent = ""
            )
            _chatList.value = _chatList.value + aiMessage
        }
    }

    private fun connectWebSocket() {
        viewModelScope.launch {
            _isConnecting.value = true

            // 确保旧的连接已关闭
            webSocketManager?.closeWebSocket()
            webSocketManager = null

            val token = dataStoreManager.getToken().firstOrNull() ?: ""
            val currentChatId = _chatId.value

            webSocketManager = WebSocketManager()
            webSocketManager?.connectWebSocket(
                token = token,
                chatId = currentChatId,
                onConnected = {
                    _isConnecting.value = false
                },
                onClosed = {
                    _isConnecting.value = false
                },
                onMessageReceived = { message ->
                    handleWebSocketMessage(message)
                }
            )
        }
    }

    private fun handleWebSocketMessage(message: String) {
        viewModelScope.launch {
            val parsed = WebSocketMessageHandler.parseMessage(message)

            // 更新当前消息
            if (parsed.thinkContent != null) {
                currentThinkContent += parsed.thinkContent
            }
            currentResponseContent += parsed.responseContent

            // 获取最后一条AI消息（应该是占位消息）
            val currentList = _chatList.value.toMutableList()
            if (currentList.isNotEmpty() && currentList.last().position == PositionEnum.LEFT) {
                val lastIndex = currentList.lastIndex
                val updatedMessage = ChatMessage(
                    position = PositionEnum.LEFT,
                    thinkContent = if (currentThinkContent.isNotBlank()) currentThinkContent else null,
                    responseContent = currentResponseContent
                )
                currentList[lastIndex] = updatedMessage
                _chatList.value = currentList
            }

            // 如果完成，重置当前内容并关闭WebSocket
            if (parsed.isCompleted) {
                currentThinkContent = ""
                currentResponseContent = ""
                _isSending.value = false

                // 延迟一小段时间后关闭WebSocket连接，避免立即关闭导致消息不完整
                kotlinx.coroutines.delay(100)
                webSocketManager?.closeWebSocket()
                webSocketManager = null
                Log.d("ChatViewModel", "消息完成，WebSocket连接已关闭")
            }
        }
    }

    fun toggleThinkMode() {
        viewModelScope.launch {
            val newValue = !_thinkMode.value
            _thinkMode.value = newValue
            dataStoreManager.saveThinkMode(newValue)
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val newLanguage = if (_language.value == "zh") "en" else "zh"
            _language.value = newLanguage
            dataStoreManager.saveLanguage(newLanguage)
        }
    }

    fun startNewChat() {
        viewModelScope.launch {
            // 清除聊天记录
            _chatList.value = emptyList()

            // 生成新的chatId
            val newChatId = UUID.randomUUID().toString().replace("-", "")
            _chatId.value = newChatId
            dataStoreManager.saveChatId(newChatId)

            // 关闭现有WebSocket连接
            webSocketManager?.closeWebSocket()
            webSocketManager = null

            // 重新添加问候语
            addGreetingMessage()
        }
    }

    fun selectModel(model: ChatModel) {
        _selectedModel.value = model
        _showModelDialog.value = false
    }

    fun toggleModelDialog() {
        _showModelDialog.value = !_showModelDialog.value
    }

    fun toggleMenuDialog() {
        _showMenuDialog.value = !_showMenuDialog.value
    }

    fun toggleTenantDialog() {
        _showTenantDialog.value = !_showTenantDialog.value
    }

    fun toggleUploadDialog() {
        _showUploadDialog.value = !_showUploadDialog.value
        if (_showUploadDialog.value) {
            loadDirectories()
        } else {
            _selectedDirectory.value = null
        }
    }

    fun showUploadDialog() {
        _showUploadDialog.value = true
        _showMenuDialog.value = false
        loadDirectories()
    }

    fun hideUploadDialog() {
        _showUploadDialog.value = false
        _selectedDirectory.value = null
    }

    fun selectDirectory(directory: Directory) {
        _selectedDirectory.value = directory
    }

    fun showCreateDirectoryDialog() {
        _showCreateDirectoryDialog.value = true
    }

    fun hideCreateDirectoryDialog() {
        _showCreateDirectoryDialog.value = false
    }

    private fun loadDirectories() {
        viewModelScope.launch {
            _isDirectoryLoading.value = true
            try {
                val tenantId = _currentTenant.value?.id ?: ""
                if (tenantId.isNotBlank()) {
                    val result = chatRepository.getDirectoryList(tenantId)
                    if (result.isSuccess) {
                        _directoryList.value = result.getOrNull() ?: emptyList()
                    } else {
                        // 可以在这里处理错误，例如显示Toast
                        Log.e("ChatViewModel", "加载目录失败: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "加载目录异常", e)
            } finally {
                _isDirectoryLoading.value = false
            }
        }
    }

    fun createDirectory(name: String) {
        viewModelScope.launch {
            val tenantId = _currentTenant.value?.id ?: ""
            if (tenantId.isNotBlank() && name.isNotBlank()) {
                val result = chatRepository.createDirectory(name, tenantId)
                if (result.isSuccess) {
                    val newDirectory = result.getOrNull()
                    newDirectory?.let {
                        // 添加到列表最上面并选中
                        _directoryList.value = listOf(it) + _directoryList.value
                        _selectedDirectory.value = it

                        // 可以在这里显示成功提示
                        Log.d("ChatViewModel", "创建目录成功: ${it.directory}")
                    }
                } else {
                    Log.e("ChatViewModel", "创建目录失败: ${result.exceptionOrNull()?.message}")
                    // 可以在这里显示错误提示
                }
            }
        }
    }

    // 修改文件上传方法
    fun uploadDocument(context: Context, uri: Uri, directory: Directory) {
        // 用 context.contentResolver 打开 InputStream 或转成 File
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            // 注意：你可能需要将 InputStream 写入临时文件，因为 Retrofit 需要 File
            val tempFile = createTempFileFromUri(context, uri)
            if (tempFile != null) {
                viewModelScope.launch {
                    chatRepository.uploadDocument(
                        tenantId = directory.tenantId,
                        directoryId = directory.id ?: "",
                        file = tempFile
                    ).onSuccess { result ->
                        // 处理成功
                    }.onFailure { error ->
                        // 处理失败
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Upload", "Failed to open URI", e)
        }
    }

    // 辅助方法：将 Uri 转为临时 File
    private fun createTempFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload_", ".tmp", context.cacheDir)
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("Upload", "Failed to create temp file", e)
            null
        }
    }


    // 修改文件选择方法
    fun openFilePicker(context: Context) {
        viewModelScope.launch {
            selectedDirectory.value?.let { directory ->
                // 这里应该使用 ActivityResultLauncher，简化处理
                // 在实际项目中，你需要注册一个 ActivityResultLauncher
                // 这里只是示例，实际需要从文件选择器的回调中获取 URI
                // val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                //     type = "*/*"
                //     putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                //         "text/markdown",
                //         "text/plain",
                //         "application/msword",
                //         "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                //         "application/pdf"
                //     ))
                //     addCategory(Intent.CATEGORY_OPENABLE)
                // }
                // filePickerLauncher.launch(intent)

                // 简化处理，假设文件选择成功并调用上传
                // 实际项目中需要从 ActivityResult 回调中获取 URI
            }
        }
    }

    // 添加上传文件的方法（从URI）
    fun uploadDocument(context: Context,uri: Uri) {
        viewModelScope.launch {
            selectedDirectory.value?.let { directory ->
                uploadDocument(context, uri, directory)
            }
        }
    }

    // 辅助扩展函数
    private fun String.toRequestBody(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    // 注意：删除了有问题的 getDocListByDirId 方法，因为 ChatRepository 中已经有这个方法了

    // 添加切换我的文档对话框方法
    fun toggleMyDocumentsDialog() {
        _showMyDocumentsDialog.value = !_showMyDocumentsDialog.value
        if (_showMyDocumentsDialog.value) {
            loadDirectories() // 打开对话框时加载目录
        } else {
            // 关闭对话框时清空展开状态和文档数据
            _expandedDirectories.value = emptySet()
            _directoryDocuments.value = emptyMap()
        }
    }


    // 切换目录展开状态并加载文档
    fun toggleDirectoryExpanded(directory: Directory) {
        viewModelScope.launch {
            val directoryId = directory.id ?: return@launch
            val currentExpanded = _expandedDirectories.value.toMutableSet()

            if (currentExpanded.contains(directoryId)) {
                // 如果已经展开，则收起
                currentExpanded.remove(directoryId)
                _expandedDirectories.value = currentExpanded
            } else {
                // 如果未展开，则展开并加载文档
                _isDocumentsLoading.value = true
                try {
                    val tenantId = _currentTenant.value?.id ?: ""
                    if (tenantId.isNotBlank()) {
                        // 使用 chatRepository.getDocListByDirId 而不是已删除的方法
                        val result = chatRepository.getDocListByDirId(tenantId, directoryId)
                        if (result.isSuccess) {
                            val documents: List<Document> = (result.getOrNull() ?: emptyList()) as List<Document>
                            // 更新文档映射
                            val currentMap = _directoryDocuments.value.toMutableMap()
                            currentMap[directoryId] = documents
                            _directoryDocuments.value = currentMap

                            // 添加到展开集合
                            currentExpanded.add(directoryId)
                            _expandedDirectories.value = currentExpanded
                        } else {
                            // 可以在这里处理错误
                            Log.e("ChatViewModel", "加载文档失败: ${result.exceptionOrNull()?.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "加载文档异常", e)
                } finally {
                    _isDocumentsLoading.value = false
                }
            }
        }
    }

    // 删除文档
    fun deleteDocument(docId: String, directoryId: String) {
        viewModelScope.launch {
            try {
                val result = chatRepository.deleteDocument(docId)
                if (result.isSuccess && result.getOrNull() ?: 0 > 0) {
                    // 删除成功，更新本地数据
                    val currentMap = _directoryDocuments.value.toMutableMap()
                    val documents = currentMap[directoryId]?.toMutableList() ?: mutableListOf()
                    documents.removeAll { it.id == docId }
                    currentMap[directoryId] = documents
                    _directoryDocuments.value = currentMap

                    // 可以在这里显示成功提示
                    Log.d("ChatViewModel", "删除文档成功: $docId")
                } else {
                    // 可以在这里显示错误提示
                    Log.e("ChatViewModel", "删除文档失败")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "删除文档异常", e)
            }
        }
    }

    // 加载会话记录
    fun loadChatHistory() {
        viewModelScope.launch {
            _isChatHistoryLoading.value = true
            _currentChatHistoryPage.value = 1

            try {
                // 获取当前租户ID
                val tenantId = _currentTenant.value?.id ?: ""

                val result = chatRepository.getChatHistory(tenantId, pageSize, 1)
                if (result.isSuccess) {
                    val historyList = result.getOrNull() ?: emptyList()
                    _chatHistoryList.value = historyList
                    _hasMoreChatHistory.value = historyList.size >= pageSize
                    updateGroupedChatHistory(historyList)
                } else {
                    // 可以显示错误提示
                    Log.e("ChatViewModel", "加载会话记录失败: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "加载会话记录异常", e)
            } finally {
                _isChatHistoryLoading.value = false
            }
        }
    }

    // 加载更多会话记录（带tenantId）
    fun loadMoreChatHistory() {
        viewModelScope.launch {
            if (_isChatHistoryLoading.value || !_hasMoreChatHistory.value) return@launch

            _isChatHistoryLoading.value = true
            val nextPage = _currentChatHistoryPage.value + 1

            try {
                // 获取当前租户ID
                val tenantId = _currentTenant.value?.id ?: ""

                val result = chatRepository.getChatHistory(tenantId, pageSize, nextPage)
                if (result.isSuccess) {
                    val newHistoryList = result.getOrNull() ?: emptyList()
                    val currentList = _chatHistoryList.value.toMutableList()
                    currentList.addAll(newHistoryList)

                    _chatHistoryList.value = currentList
                    _currentChatHistoryPage.value = nextPage
                    _hasMoreChatHistory.value = newHistoryList.size >= pageSize
                    updateGroupedChatHistory(currentList)
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "加载更多会话记录异常", e)
            } finally {
                _isChatHistoryLoading.value = false
            }
        }
    }

    // 更新分组数据
    private fun updateGroupedChatHistory(historyList: List<ChatHistory>) {
        val grouped = mutableMapOf<String, MutableList<ChatHistory>>()

        historyList.forEach { history ->
            // 使用formatRelativeTime获取相对时间分组
            val timeGroup = formatRelativeTime(history.createTime)

            if (!grouped.containsKey(timeGroup)) {
                grouped[timeGroup] = mutableListOf()
            }
            grouped[timeGroup]?.add(history)
        }

        _groupedChatHistory.value = grouped
    }

    // 加载会话记录到聊天
    fun loadChatHistoryToChat(history: ChatHistory) {
        viewModelScope.launch {
            // 清空当前聊天
            _chatList.value = emptyList()

            // 更新chatId
            _chatId.value = history.chatId
            dataStoreManager.saveChatId(history.chatId)

            // 创建用户消息
            val userMessage = ChatMessage(
                position = PositionEnum.RIGHT,
                responseContent = history.prompt
            )

            // 创建AI回复消息
            val aiMessage = ChatMessage(
                position = PositionEnum.LEFT,
                thinkContent = history.thinkContent,
                responseContent = history.responseContent ?: history.content
            )

            // 添加到聊天列表
            _chatList.value = listOf(userMessage, aiMessage)

            // 更新当前租户和模型（如果有的话）
            // 这里可以根据需要从history中提取相关信息
        }
    }

    // 重置会话记录状态
    private fun resetChatHistory() {
        _chatHistoryList.value = emptyList()
        _groupedChatHistory.value = emptyMap()
        _currentChatHistoryPage.value = 1
        _hasMoreChatHistory.value = true
    }

    fun selectTenant(tenant: Tenant) {
        viewModelScope.launch {
            _currentTenant.value = tenant
            dataStoreManager.saveTenantId(tenant.id)
            dataStoreManager.saveCurrentTenant(tenant)
            _showTenantDialog.value = false

            // 切换租户后，清空当前聊天
            startNewChat()

            // 重置会话记录（因为租户变了，之前的会话记录不再适用）
            resetChatHistory()
        }
    }

    // 在toggleChatHistoryDialog中添加租户检查
    fun toggleChatHistoryDialog() {
        if (_showChatHistoryDialog.value) {
            _showChatHistoryDialog.value = false
            resetChatHistory()
        } else {
            // 检查是否有当前租户
            if (_currentTenant.value == null) {
                // 可以显示提示信息
                Log.w("ChatViewModel", "没有选择租户，无法加载会话记录")
                return
            }
            _showChatHistoryDialog.value = true
            loadChatHistory()
        }
    }


    override fun onCleared() {
        super.onCleared()
        webSocketManager?.closeWebSocket()
        webSocketManager = null
    }
}