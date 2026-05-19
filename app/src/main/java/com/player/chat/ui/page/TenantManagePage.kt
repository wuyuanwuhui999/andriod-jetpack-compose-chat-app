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
import com.player.chat.model.TenantUser
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

    // 搜索关键词
    var searchKeyword by remember { mutableStateOf("") }

    // 删除确认对话框状态
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<TenantUser?>(null) }

    val listState = rememberLazyListState()

    // 过滤后的用户列表（根据搜索关键词过滤）
    val filteredUserList = remember(tenantUserList, searchKeyword) {
        if (searchKeyword.isBlank()) {
            tenantUserList
        } else {
            tenantUserList.filter {
                it.username.contains(searchKeyword, ignoreCase = true) ||
                        it.email.contains(searchKeyword, ignoreCase = true)
            }
        }
    }

    // 监听滚动到底部
    LaunchedEffect(listState.layoutInfo) {
        val layoutInfo = listState.layoutInfo
        if (layoutInfo.visibleItemsInfo.isNotEmpty() && searchKeyword.isBlank()) {
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.last().index
            val totalItemsCount = layoutInfo.totalItemsCount

            if (lastVisibleItemIndex >= totalItemsCount - 1 &&
                !isLoading && !isLoadingMore && hasMoreData && tenantUserList.isNotEmpty()) {
                Log.d("TenantManage", "滚动到底部，加载更多")
                viewModel.loadMoreTenantUserList()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "租户管理",
                            color = Color.Black,
                            fontSize = Dimens.normalFontSize,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.PageBackground)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 搜索框 - 胶囊型
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.middleGap, vertical = Dimens.middleGap),
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
                            onValueChange = { searchKeyword = it },
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
                                            text = "搜索用户...",
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
                                onClick = { searchKeyword = "" },
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

                // 用户列表区域
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.middleGap)
                ) {
                    when {
                        isLoading && tenantUserList.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
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
                        filteredUserList.isEmpty() && searchKeyword.isNotBlank() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
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
                        filteredUserList.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
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
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                items(
                                    items = filteredUserList,
                                    key = { it.id }
                                ) { tenantUser ->
                                    SwipeToDeleteUserItem(
                                        tenantUser = tenantUser,
                                        onDelete = {
                                            selectedUser = tenantUser
                                            showDeleteDialog = true
                                        }
                                    )

                                    // 分隔线
                                    if (filteredUserList.indexOf(tenantUser) < filteredUserList.size - 1) {
                                        Divider(
                                            color = Color.Gray.copy(alpha = 0.2f),
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(horizontal = Dimens.middleGap)
                                        )
                                    }
                                }

                                // 加载更多指示器
                                if (isLoadingMore && searchKeyword.isBlank()) {
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

            // 底部提示 - 已经最后一页（放在 Box 最外层，使用 align）
            if (showEndTip) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
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
 * 可滑动删除的用户条目组件
 */
@Composable
fun SwipeToDeleteUserItem(
    tenantUser: TenantUser,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val deleteButtonWidth = 80.dp
    var showDeleteButton by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.middleAvatar + Dimens.middleGap * 2)
            .background(Color.White)
    ) {
        // 删除按钮（右侧）
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(deleteButtonWidth)
                .background(Color.Red)
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "删除",
                color = Color.White,
                fontSize = Dimens.normalFontSize,
                fontWeight = FontWeight.Medium
            )
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
                            // 滑动结束后判断显示/隐藏删除按钮
                            showDeleteButton = offsetX <= -deleteButtonWidth.value / 2
                            offsetX = if (showDeleteButton) -deleteButtonWidth.value else 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            // 使用 onHorizontalDrag 替代 onDrag
                            change.consume()
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(-deleteButtonWidth.value, 0f)
                        }
                    )
                }
                .clickable {
                    // 如果删除按钮显示中，点击隐藏
                    if (showDeleteButton) {
                        showDeleteButton = false
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

            // 用户名和邮箱信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = tenantUser.username,
                    color = Color.Black,
                    fontSize = Dimens.normalFontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = tenantUser.email,
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