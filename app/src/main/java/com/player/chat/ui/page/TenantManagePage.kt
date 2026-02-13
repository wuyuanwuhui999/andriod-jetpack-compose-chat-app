package com.player.chat.ui.page

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.player.chat.R
import com.player.chat.config.Config
import com.player.chat.model.TenantUser
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.TenantManageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantManagePage(
    navController: NavHostController,
    viewModel: TenantManageViewModel = hiltViewModel()
) {
    val tenantUserList by viewModel.tenantUserList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMoreData by viewModel.hasMoreData.collectAsState()
    val currentTenant by viewModel.currentTenant.collectAsState()
    val showEndTip by viewModel.showEndTip.collectAsState()

    val listState = rememberLazyListState()

    // 监听滚动到底部
    LaunchedEffect(listState.layoutInfo) {
        val layoutInfo = listState.layoutInfo
        if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.last().index
            val totalItemsCount = layoutInfo.totalItemsCount

            // 当滚动到最后一项时，加载更多
            if (lastVisibleItemIndex >= totalItemsCount - 1 &&
                !isLoading && !isLoadingMore && hasMoreData && tenantUserList.isNotEmpty()) {
                Log.d("TenantManage", "滚动到底部，加载更多")
                viewModel.loadMoreTenantUserList()
            } else if (!hasMoreData && lastVisibleItemIndex >= totalItemsCount - 1 && tenantUserList.isNotEmpty()) {
                // 已经最后一页，显示提示
                viewModel.loadMoreTenantUserList() // 这个方法会触发显示结束提示
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
                            text = "租户管理 - ${currentTenant?.name ?: ""}",
                            color = Color.Black,
                            fontSize = Dimens.fontSizeNormal,
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
                actions = {
                    // 右侧加号按钮，预留点击方法
                    IconButton(
                        onClick = {
                            // TODO: 添加租户用户功能，后面自己实现
                            Log.d("TenantManage", "点击添加租户用户")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.PrimaryColor
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.pageBackgroundColor)
                .padding(paddingValues)
        ) {
            if (isLoading) {
                // 初始加载
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
                            color = Color.PrimaryColor,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(Dimens.pagePadding))
                        Text(
                            text = "加载中...",
                            color = Color.Gray
                        )
                    }
                }
            } else if (tenantUserList.isEmpty()) {
                // 空状态
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
                        Spacer(modifier = Modifier.height(Dimens.pagePadding))
                        Text(
                            text = "暂无租户用户",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // 用户列表
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(0.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(
                        items = tenantUserList,
                        key = { it.id }
                    ) { tenantUser ->
                        TenantUserItem(tenantUser = tenantUser)

                        // 分隔线，最后一项不显示分隔线
                        if (tenantUserList.indexOf(tenantUser) < tenantUserList.size - 1) {
                            Divider(
                                color = Color.Gray.copy(alpha = 0.2f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = Dimens.pagePadding)
                            )
                        }
                    }

                    // 加载更多指示器
                    item {
                        if (isLoadingMore) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.PrimaryColor,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }

            // 底部提示 - 已经最后一页
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
                        fontSize = Dimens.fontSizeNormal
                    )
                }
            }
        }
    }

    // 首次加载或租户变更时刷新数据
    LaunchedEffect(currentTenant?.id) {
        if (currentTenant != null) {
            viewModel.refreshTenantUserList()
        }
    }
}

@Composable
fun TenantUserItem(
    tenantUser: TenantUser
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = Dimens.pagePadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 圆形用户头像
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.1f))
        ) {
            if (!tenantUser.avatar.isNullOrBlank()) {
                // 有头像时显示头像
                AsyncImage(
                    model = Config.BASE_URL + tenantUser.avatar,
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.default_avater),
                    error = painterResource(R.drawable.default_avater)
                )
            } else {
                // 默认头像
                Icon(
                    painter = painterResource(R.drawable.icon_user),
                    contentDescription = "默认头像",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.width(Dimens.pagePadding))

        // 用户信息
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // 用户昵称
            Text(
                text = tenantUser.username.ifEmpty { "未设置昵称" },
                color = Color.Black,
                fontSize = Dimens.fontSizeNormal,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 用户工号 - 显示灰色
            Text(
                text = "工号: ${tenantUser.id.takeLast(8)}", // 取ID后8位作为工号展示
                color = Color.Gray,
                fontSize = Dimens.fontSizeNormal
            )
        }

        // 角色标识（可选）
        Surface(
            modifier = Modifier
                .wrapContentSize(),
            shape = RoundedCornerShape(4.dp),
            color = when (tenantUser.roleType) {
                1 -> Color.PrimaryColor.copy(alpha = 0.1f) // 管理员
                2 -> Color.SecondaryColor.copy(alpha = 0.1f) // 普通成员
                else -> Color.Gray.copy(alpha = 0.1f)
            }
        ) {
            Text(
                text = when (tenantUser.roleType) {
                    1 -> "管理员"
                    2 -> "成员"
                    else -> "访客"
                },
                color = when (tenantUser.roleType) {
                    1 -> Color.PrimaryColor
                    2 -> Color.SecondaryColor
                    else -> Color.Gray
                },
                fontSize = Dimens.fontSizeNormal,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}