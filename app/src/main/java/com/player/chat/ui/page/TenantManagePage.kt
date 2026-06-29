// ui/page/TenantManagePage.kt
package com.player.chat.ui.page

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.player.chat.R
import com.player.chat.config.Config
import com.player.chat.model.SearchUser
import com.player.chat.model.TenantUser
import com.player.chat.navigation.Screens
import com.player.chat.ui.components.CustomAlertDialog
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.TenantManageViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantManagePage(
    navController: NavHostController,
    viewModel: TenantManageViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val tenantUserList by viewModel.tenantUserList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMoreData by viewModel.hasMoreData.collectAsState()
    val currentTenant by viewModel.currentTenant.collectAsState()
    val showEndTip by viewModel.showEndTip.collectAsState()

    // 搜索相关状态
    val searchKeyword by viewModel.searchKeyword.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val addSuccessMessage by viewModel.addSuccessMessage.collectAsState()

    // 获取当前用户在当前租户下的角色 - 直接从 currentTenant.role 获取
    val currentUserRole = currentTenant?.role ?: 0

    // 删除确认对话框状态
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<TenantUser?>(null) }

    // 操作结果提示
    var operationMessage by remember { mutableStateOf<String?>(null) }
    var isOperationSuccess by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // 监听滚动到底部
    LaunchedEffect(listState.layoutInfo) {
        val layoutInfo = listState.layoutInfo
        if (layoutInfo.visibleItemsInfo.isNotEmpty() && searchKeyword.isBlank()) {
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.last().index
            val totalItemsCount = layoutInfo.totalItemsCount

            if (lastVisibleItemIndex >= totalItemsCount - 1 &&
                !isLoading && !isLoadingMore && hasMoreData && tenantUserList.isNotEmpty()
            ) {
                Log.d("TenantManage", "滚动到底部，加载更多")
                viewModel.loadMoreTenantUserList()
            }
        }
    }

    // 显示操作结果提示
    LaunchedEffect(operationMessage) {
        if (operationMessage != null) {
            delay(2000)
            operationMessage = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "租户管理",
                        color = Color.Black,
                        fontSize = Dimens.normalFontSize,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.Black,
                            modifier = Modifier.size(Dimens.middleIconSize)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screens.AddTenantUser.route)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加租户用户",
                            tint = Color.Black,
                            modifier = Modifier.size(Dimens.middleIconSize)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.PageBackground)
                .padding(paddingValues)
        ) {
            // 搜索框 - 胶囊型
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.middleGap)
                    .padding(top = Dimens.middleGap),
                shape = RoundedCornerShape(Dimens.inputHeight / 2),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.inputHeight)
                        .padding(horizontal = Dimens.middleGap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = Color.Gray,
                        modifier = Modifier.size(Dimens.smallIconSize)
                    )

                    Spacer(modifier = Modifier.width(Dimens.smallGap))

                    BasicTextField(
                        value = searchKeyword,
                        onValueChange = { viewModel.updateSearchKeyword(it) },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(
                            color = Color.Black,
                            fontSize = Dimens.normalFontSize
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchKeyword.isEmpty()) {
                                    Text(
                                        text = "请输入用户工号或名称",
                                        color = Color.Gray,
                                        fontSize = Dimens.normalFontSize
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (searchKeyword.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.clearSearchResults() },
                            modifier = Modifier.size(Dimens.smallIconSize)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "清除",
                                tint = Color.Gray,
                                modifier = Modifier.size(Dimens.smallIconSize)
                            )
                        }
                    }
                }
            }

            // 内容区域 - 与搜索框保持 middleGap 间距
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = Dimens.middleGap)
                    .padding(top = Dimens.middleGap)
                    .padding(bottom = Dimens.middleGap),
                shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                when {
                    // 显示搜索结果
                    searchKeyword.isNotBlank() -> {
                        SearchResultsSection(
                            isLoading = isSearching,
                            searchResults = searchResults,
                            onUserClick = { user ->
                                // 只有不在当前租户内的用户才能添加
                                if (user.checked == 0) {
                                    viewModel.addUserToTenant(user)
                                }
                            }
                        )
                    }
                    // 显示租户用户列表
                    else -> {
                        // 租户用户列表区域 - 使用 wrapContentHeight 让高度自适应
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            when {
                                isLoading && tenantUserList.isEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .padding(Dimens.middleGap),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(40.dp),
                                                color = Color.Primary,
                                                strokeWidth = 3.dp
                                            )
                                            Spacer(modifier = Modifier.height(Dimens.middleGap))
                                            Text(
                                                text = "加载中...",
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                                tenantUserList.isEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .padding(Dimens.middleGap),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.icon_user),
                                                contentDescription = "暂无用户",
                                                modifier = Modifier.size(60.dp),
                                                tint = Color.Gray.copy(alpha = 0.5f)
                                            )
                                            Spacer(modifier = Modifier.height(Dimens.middleGap))
                                            Text(
                                                text = "暂无租户用户",
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    // LazyColumn 使用 wrapContentHeight 让高度根据内容自适应
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(),
                                        verticalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {
                                        items(
                                            items = tenantUserList,
                                            key = { it.id }
                                        ) { tenantUser ->
                                            // 判断是否为自己（当前登录用户）
                                            // 使用 currentUser.id 与 tenantUser.userId 比较
                                            val currentUser by viewModel.dataStoreManager.getUser().collectAsState(initial = null)
                                            val isSelf = currentUser?.id == tenantUser.userId

                                            SwipeToDeleteUserItem(
                                                tenantUser = tenantUser,
                                                currentUserRole = currentUserRole,
                                                isSelf = isSelf,
                                                onDelete = {
                                                    selectedUser = tenantUser
                                                    showDeleteDialog = true
                                                },
                                                onAddAdmin = {
                                                    // 设为管理员
                                                    viewModel.addAdmin(
                                                        tenantUser = tenantUser,
                                                        onSuccess = { msg ->
                                                            operationMessage = msg
                                                            isOperationSuccess = true
                                                        },
                                                        onError = { errorMsg ->
                                                            operationMessage = errorMsg
                                                            isOperationSuccess = false
                                                        }
                                                    )
                                                },
                                                onCancelAdmin = {
                                                    // 取消管理员
                                                    viewModel.cancelAdmin(
                                                        tenantUser = tenantUser,
                                                        onSuccess = { msg ->
                                                            operationMessage = msg
                                                            isOperationSuccess = true
                                                        },
                                                        onError = { errorMsg ->
                                                            operationMessage = errorMsg
                                                            isOperationSuccess = false
                                                        }
                                                    )
                                                }
                                            )

                                            // 分隔线
                                            if (tenantUserList.indexOf(tenantUser) < tenantUserList.size - 1) {
                                                Divider(
                                                    color = Color.Gray.copy(alpha = 0.2f),
                                                    thickness = Dimens.borderSize,
                                                    modifier = Modifier.padding(horizontal = Dimens.middleGap)
                                                )
                                            }
                                        }

                                        // 加载更多指示器
                                        if (isLoadingMore) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 16.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        color = Color.Primary,
                                                        strokeWidth = 2.dp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 底部提示 - 已经最后一页
            if (showEndTip) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 20.dp)
                        .widthIn(max = 200.dp),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color.Gray.copy(alpha = 0.9f),
                    contentColor = Color.White,
                    action = null
                ) {
                    Text(
                        text = "没有更多数据了",
                        color = Color.White,
                        fontSize = Dimens.normalFontSize
                    )
                }
            }

            // 添加成功提示
            if (addSuccessMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 20.dp)
                        .widthIn(max = 250.dp),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = if (addSuccessMessage?.contains("成功") == true) Color.Primary else Color.Red,
                    contentColor = Color.White,
                    action = null
                ) {
                    Text(
                        text = addSuccessMessage ?: "",
                        color = Color.White,
                        fontSize = Dimens.normalFontSize
                    )
                }
            }

            // 操作结果提示
            if (operationMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 20.dp)
                        .widthIn(max = 250.dp),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = if (isOperationSuccess) Color.Primary else Color.Red,
                    contentColor = Color.White,
                    action = null
                ) {
                    Text(
                        text = operationMessage ?: "",
                        color = Color.White,
                        fontSize = Dimens.normalFontSize
                    )
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog && selectedUser != null) {
        CustomAlertDialog(
            title = "删除用户",
            onConfirm = {
                viewModel.deleteTenantUser(selectedUser!!)
                showDeleteDialog = false
                selectedUser = null
            },
            onDismiss = {
                showDeleteDialog = false
                selectedUser = null
            }
        ) {
            Text("确定要删除用户 ${selectedUser?.username} 吗？")
        }
    }

    // 首次加载或租户变更时刷新数据
    LaunchedEffect(currentTenant?.id) {
        if (currentTenant != null) {
            viewModel.refreshTenantUserList()
        }
    }
}

/**
 * 搜索结果区域组件 - 高度自适应
 */
@Composable
fun SearchResultsSection(
    isLoading: Boolean,
    searchResults: List<SearchUser>,
    onUserClick: (SearchUser) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(Dimens.middleGap),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = Color.Primary,
                        strokeWidth = 3.dp
                    )
                }
            }
            searchResults.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(Dimens.middleGap),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_user),
                            contentDescription = "未找到用户",
                            modifier = Modifier.size(60.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(Dimens.middleGap))
                        Text(
                            text = "未找到相关用户",
                            color = Color.Gray
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(searchResults) { index, user ->
                        SearchUserItem(
                            user = user,
                            onClick = { onUserClick(user) }
                        )

                        // 分隔线（最后一条不显示）
                        if (index < searchResults.size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimens.middleGap)
                            ) {
                                Divider(
                                    color = Color.Gray.copy(alpha = 0.2f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 搜索结果用户条目组件 - 使用圆角
 * 已添加的用户显示✅图标，未添加的直接点击添加
 */
@Composable
fun SearchUserItem(
    user: SearchUser,
    onClick: () -> Unit
) {
    val isInTenant = user.checked == 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isInTenant) { onClick() },
        shape = RoundedCornerShape(Dimens.moduleBorderRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isInTenant) Color.Primary.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.middleGap),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 用户信息
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像
                Box(
                    modifier = Modifier
                        .size(Dimens.middleAvatar)
                        .clip(RoundedCornerShape(Dimens.middleAvatar / 2))
                        .background(Color.Gray.copy(alpha = 0.1f))
                ) {
                    if (!user.avatar.isNullOrBlank()) {
                        AsyncImage(
                            model = Config.BASE_URL + user.avatar,
                            contentDescription = "用户头像",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.default_avater),
                            error = painterResource(R.drawable.default_avater)
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.icon_user),
                            contentDescription = "默认头像",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Dimens.smallGap),
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Dimens.middleGap))

                // 用户名和邮箱
                Column {
                    Text(
                        text = user.username,
                        color = if (isInTenant) Color.Primary else Color.Black,
                        fontSize = Dimens.normalFontSize,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = user.email,
                        color = Color.Gray,
                        fontSize = Dimens.normalFontSize
                    )
                }
            }

            // 右侧图标：已在租户内显示✅图标，否则不显示按钮（点击卡片添加）
            if (isInTenant) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已添加",
                    tint = Color.Primary,
                    modifier = Modifier.size(Dimens.middleIconSize)
                )
            }
        }
    }
}

/**
 * 可滑动删除的用户条目组件
 * 显示用户姓名 + 用户账号，纵向排列
 * 根据当前用户角色显示不同的操作按钮
 */
@Composable
fun SwipeToDeleteUserItem(
    tenantUser: TenantUser,
    currentUserRole: Int, // 当前登录用户在当前租户下的角色：0-普通用户，1-管理员，2-超级管理员
    isSelf: Boolean = false, // 是否为自己
    onDelete: () -> Unit,
    onAddAdmin: () -> Unit,
    onCancelAdmin: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var showActions by remember { mutableStateOf(false) }
    val deleteButtonWidth = 80.dp

    // 判断用户角色
    val isUserAdmin = tenantUser.role == 1
    val isUserSuperAdmin = tenantUser.role == 2

    // 计算操作按钮数量
    // 规则：
    // 1. 不能对自己操作
    // 2. 当前用户 role=2（超级管理员）：显示 设为管理员/取消管理员 + 删除
    // 3. 当前用户 role=1（普通管理员）：只显示删除，且只能删除 role=0 的用户
    // 4. 当前用户 role=0（普通用户）：不显示任何操作按钮

    // 是否可以删除
    val canDelete = when {
        isSelf -> false // 不能删除自己
        currentUserRole == 2 -> true // 超级管理员可以删除任何人
        currentUserRole == 1 && !isUserAdmin && !isUserSuperAdmin -> true // 普通管理员只能删除普通用户
        else -> false
    }

    // 是否可以设置/取消管理员（只有超级管理员可以）
    val canManageAdmin = currentUserRole == 2 && !isSelf

    // 计算按钮数量
    val actionCount = when {
        canManageAdmin && canDelete -> 2
        canManageAdmin -> 1
        canDelete -> 1
        else -> 0
    }

    val totalWidth = actionCount * deleteButtonWidth.value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.middleAvatar + Dimens.middleGap * 2)
            .background(Color.White)
    ) {
        // 操作按钮（右侧）
        if (actionCount > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(deleteButtonWidth * actionCount),
                horizontalArrangement = Arrangement.End
            ) {
                // 超级管理员：显示设为管理员/取消管理员按钮
                if (canManageAdmin) {
                    if (isUserAdmin || isUserSuperAdmin) {
                        // 取消管理员按钮（管理员或超级管理员）
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(deleteButtonWidth)
                                .background(Color.Primary) // 橙色
                                .clickable {
                                    onCancelAdmin()
                                    offsetX = 0f
                                    showActions = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "取消管理员",
                                color = Color.White,
                                fontSize = Dimens.normalFontSize,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // 设为管理员按钮（普通用户）
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(deleteButtonWidth)
                                .background(Color.Primary) // 绿色
                                .clickable {
                                    onAddAdmin()
                                    offsetX = 0f
                                    showActions = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "设为管理员",
                                color = Color.White,
                                fontSize = Dimens.normalFontSize,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 删除按钮
                if (canDelete) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(deleteButtonWidth)
                            .background(Color.Red)
                            .clickable {
                                onDelete()
                                offsetX = 0f
                                showActions = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "删除",
                            color = Color.White,
                            fontSize = Dimens.normalFontSize,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 用户信息内容（可滑动）
        Row(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = offsetX.dp)
                .background(Color.White)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // 滑动结束后判断显示/隐藏操作按钮
                            val threshold = if (actionCount > 0) deleteButtonWidth.value * 0.3f else 0f
                            showActions = offsetX <= -threshold
                            offsetX = if (showActions) -totalWidth else 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(-totalWidth, 0f)
                        }
                    )
                }
                .clickable {
                    // 如果操作按钮显示中，点击隐藏
                    if (showActions) {
                        showActions = false
                        offsetX = 0f
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Spacer(modifier = Modifier.width(Dimens.middleGap))

            // 用户头像
            Box(
                modifier = Modifier
                    .size(Dimens.middleAvatar)
                    .clip(RoundedCornerShape(Dimens.middleAvatar / 2))
                    .background(Color.Gray.copy(alpha = 0.1f))
            ) {
                if (!tenantUser.avatar.isNullOrBlank()) {
                    AsyncImage(
                        model = Config.BASE_URL + tenantUser.avatar,
                        contentDescription = "用户头像",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.default_avater),
                        error = painterResource(R.drawable.default_avater)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.icon_user),
                        contentDescription = "默认头像",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimens.smallGap),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimens.middleGap))

            // 用户姓名和用户账号 - 纵向排列
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.smallGap)
                ) {
                    Text(
                        text = tenantUser.username,
                        color = Color.Black,
                        fontSize = Dimens.normalFontSize,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 角色标签
                    when (tenantUser.role) {
                        1 -> {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "管理员",
                                    color = Color.Primary,
                                    fontSize = Dimens.normalFontSize,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        2 -> {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Primary.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "超级管理员",
                                    color = Color.Primary,
                                    fontSize = Dimens.normalFontSize,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = tenantUser.userAccount.ifEmpty { tenantUser.userId },
                    color = Color.secondary,
                    fontSize = Dimens.normalFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(Dimens.middleGap))
        }
    }
}