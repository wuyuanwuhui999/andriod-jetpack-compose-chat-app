// chat/ui/page/UserManagePage.kt
package com.player.chat.ui.page

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import com.player.chat.model.User
import com.player.chat.navigation.Screens
import com.player.chat.ui.components.CustomAlertDialog
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.UserManageViewModel

/**
 * 用户管理页面
 * 显示公司下的所有用户，支持搜索、分页加载、删除用户、设置/取消管理员
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagePage(
    navController: NavHostController,
    viewModel: UserManageViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val userList by viewModel.userList.collectAsState()
    val currentCompany by viewModel.currentCompany.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMoreData by viewModel.hasMoreData.collectAsState()
    val showEndTip by viewModel.showEndTip.collectAsState()
    val searchKeyword by viewModel.searchKeyword.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    // 删除确认对话框状态
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    // 当前登录用户信息（从 Company 中获取 role）
    val currentUserRole = currentCompany?.role ?: 0
    // 是否为超级管理员 (role = 2)
    val isSuperAdmin = currentUserRole == 2
    // 是否为管理员 (role > 0)
    val isAdmin = currentUserRole > 0

    val listState = rememberLazyListState()

    // 监听滚动到底部
    LaunchedEffect(listState.layoutInfo) {
        val layoutInfo = listState.layoutInfo
        if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.last().index
            val totalItemsCount = layoutInfo.totalItemsCount

            if (lastVisibleItemIndex >= totalItemsCount - 1 &&
                !isLoading && !isLoadingMore && hasMoreData && userList.isNotEmpty()
            ) {
                Log.d("UserManage", "滚动到底部，加载更多")
                viewModel.loadMoreUserList()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "用户管理",
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
                    // 加号图标 - 跳转到添加用户页面
                    IconButton(
                        onClick = {
                            navController.navigate(Screens.AddUser.route)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加用户",
                            tint = Color.Black,
                            modifier = Modifier.size(Dimens.middleIconSize)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
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
                                        text = "请输入用户姓名或工号",
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
                            onClick = { viewModel.updateSearchKeyword("") },
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

            Spacer(modifier = Modifier.height(Dimens.middleGap))

            // 用户列表卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.middleGap),
                shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    when {
                        isLoading && userList.isEmpty() -> {
                            // 加载中状态
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
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
                                        color = Color.Gray,
                                        fontSize = Dimens.normalFontSize
                                    )
                                }
                            }
                        }
                        userList.isEmpty() -> {
                            // 空状态
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
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
                                        text = if (searchKeyword.isNotBlank()) "未找到相关用户" else "暂无用户",
                                        color = Color.Gray,
                                        fontSize = Dimens.normalFontSize
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                items(
                                    items = userList,
                                    key = { it.id }
                                ) { user ->
                                    SwipeToDeleteUserItem(
                                        user = user,
                                        isSuperAdmin = isSuperAdmin,
                                        isAdmin = isAdmin,
                                        onDelete = {
                                            selectedUser = user
                                            showDeleteDialog = true
                                        },
                                        onSetAdmin = {
                                            viewModel.setAdmin(user)
                                        },
                                        onCancelAdmin = {
                                            viewModel.cancelAdmin(user)
                                        }
                                    )

                                    // 分隔线
                                    if (userList.indexOf(user) < userList.size - 1) {
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

                                // 已加载全部提示
                                if (!hasMoreData && userList.isNotEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "已加载全部用户",
                                                color = Color.Gray,
                                                fontSize = Dimens.normalFontSize
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                modifier = Modifier
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
    }

    // 操作结果提示
    if (operationMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .widthIn(max = 250.dp),
                shape = RoundedCornerShape(20.dp),
                containerColor = if (operationMessage?.contains("成功") == true) Color.Primary else Color.Red,
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

    // 删除确认对话框
    if (showDeleteDialog && selectedUser != null) {
        CustomAlertDialog(
            title = "删除用户",
            onConfirm = {
                viewModel.removeUser(selectedUser!!)
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

    // 首次加载时刷新数据
    LaunchedEffect(Unit) {
        viewModel.refreshUserList()
    }
}

/**
 * 可滑动删除的用户条目组件
 * 支持向左滑动显示操作按钮（设为管理员/取消管理员/删除）
 */
@Composable
fun SwipeToDeleteUserItem(
    user: User,
    isSuperAdmin: Boolean,
    isAdmin: Boolean,
    onDelete: () -> Unit,
    onSetAdmin: () -> Unit,
    onCancelAdmin: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var showActions by remember { mutableStateOf(false) }

    // 判断用户是否为管理员 (role == "1" 表示管理员)
    val isUserAdmin = user.role == "1"

    // 计算操作按钮数量
    val actionCount = when {
        // 超级管理员：显示 设为管理员/取消管理员 + 删除
        isSuperAdmin -> 2
        // 普通管理员：只显示删除
        isAdmin -> 1
        else -> 0
    }

    // 每个按钮宽度
    val buttonWidth = 80.dp
    val totalWidth = actionCount * buttonWidth.value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.middleAvatar + Dimens.middleGap * 2)
            .background(Color.White)
    ) {
        // 操作按钮（右侧）
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(buttonWidth * actionCount),
            horizontalArrangement = Arrangement.End
        ) {
            // 超级管理员：显示设为管理员/取消管理员按钮
            if (isSuperAdmin) {
                if (isUserAdmin) {
                    // 取消管理员按钮
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(buttonWidth)
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
                    // 设为管理员按钮
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(buttonWidth)
                            .clickable {
                                onSetAdmin()
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

            // 删除按钮（所有管理员都可显示）
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(buttonWidth)
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

        // 用户信息内容（可滑动）
        Row(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = offsetX.dp)
                .clip(RoundedCornerShape(Dimens.moduleBorderRadius))
                .background(Color.White)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // 滑动结束后判断显示/隐藏操作按钮
                            val threshold = if (actionCount > 0) buttonWidth.value * 0.3f else 0f
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

            // 用户姓名和工号
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.smallGap)
                ) {
                    Text(
                        text = user.username,
                        color = Color.Black,
                        fontSize = Dimens.normalFontSize,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 管理员标签
                    if (isUserAdmin) {
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
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "工号: ${user.id.takeLast(8)}",
                    color = Color.Gray,
                    fontSize = Dimens.normalFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(Dimens.middleGap))
        }
    }
}