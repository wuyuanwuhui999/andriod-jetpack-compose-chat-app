// chat/ui/page/AddTenantUserPage.kt
package com.player.chat.ui.page

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.player.chat.R
import com.player.chat.config.Config
import com.player.chat.model.SearchUser
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.AddTenantUserViewModel

/**
 * 添加租户用户页面
 * 搜索用户并添加到当前租户
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantUserPage(
    navController: NavHostController,
    viewModel: AddTenantUserViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // 状态收集
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val hasMoreData by viewModel.hasMoreData.collectAsStateWithLifecycle()
    val searchKeyword by viewModel.searchKeyword.collectAsStateWithLifecycle()
    val currentTenant by viewModel.currentTenant.collectAsStateWithLifecycle()
    val showEndTip by viewModel.showEndTip.collectAsStateWithLifecycle()
    val operationMessage by viewModel.operationMessage.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    // 监听滚动到底部，加载更多
    LaunchedEffect(listState.layoutInfo) {
        val layoutInfo = listState.layoutInfo
        if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.last().index
            val totalItemsCount = layoutInfo.totalItemsCount

            if (lastVisibleItemIndex >= totalItemsCount - 1 &&
                !isLoading && !isLoadingMore && hasMoreData && searchResults.isNotEmpty()
            ) {
                Log.d("AddTenantUser", "滚动到底部，加载更多")
                viewModel.loadMoreSearchResults()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "${currentTenant?.name ?: "租户"} | 添加租户用户",
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
                                        text = "请输入用户姓名或工号",
                                        color = Color.secondary,
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

            // 搜索结果列表卡片 - 高度自适应（wrapContentHeight）
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()  // 改为高度自适应
                    .padding(horizontal = Dimens.middleGap)
                    .padding(bottom = Dimens.middleGap),
                shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()  // 高度自适应
                ) {
                    when {
                        searchKeyword.isEmpty() -> {
                            // 空状态 - 提示输入搜索关键字
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
                                        painter = painterResource(id = R.drawable.icon_search),
                                        contentDescription = "搜索",
                                        modifier = Modifier.size(60.dp),
                                        tint = Color.Gray.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.middleGap))
                                    Text(
                                        text = "输入关键字搜索用户",
                                        color = Color.Gray,
                                        fontSize = Dimens.normalFontSize
                                    )
                                }
                            }
                        }
                        isLoading && searchResults.isEmpty() -> {
                            // 加载中状态
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
                                        text = "搜索中...",
                                        color = Color.Gray,
                                        fontSize = Dimens.normalFontSize
                                    )
                                }
                            }
                        }
                        searchResults.isEmpty() -> {
                            // 无搜索结果
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
                                        color = Color.Gray,
                                        fontSize = Dimens.normalFontSize
                                    )
                                }
                            }
                        }
                        else -> {
                            // 搜索结果列表 - 高度自适应
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),  // 高度自适应
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                items(
                                    items = searchResults,
                                    key = { it.id }
                                ) { user ->
                                    TenantSearchUserItem(
                                        user = user,
                                        onAddClick = {
                                            // 如果用户已添加，不执行任何操作
                                            if (user.checked == 1) return@TenantSearchUserItem
                                            viewModel.addUserToTenant(user)
                                        }
                                    )

                                    // 分隔线
                                    if (searchResults.indexOf(user) < searchResults.size - 1) {
                                        Divider(
                                            color = Color.Gray,
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

            // 已加载全部提示
            if (!hasMoreData && searchResults.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.middleGap),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "已加载全部用户",
                        color = Color.secondary,
                        fontSize = Dimens.normalFontSize
                    )
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

            // 底部留白 - 让内容不贴底
            Spacer(modifier = Modifier.weight(1f))
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
                    .padding(bottom = Dimens.middleGap)
                    .widthIn(max = 200.dp),
                shape = RoundedCornerShape(Dimens.middleGap),
                containerColor = Color.Gray,
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

    // 页面加载时加载租户信息
    LaunchedEffect(Unit) {
        viewModel.loadTenantInfo()
    }
}

/**
 * 搜索用户条目组件（租户添加专用）
 * @param user 搜索用户数据
 * @param onAddClick 添加按钮点击回调
 */
@Composable
fun TenantSearchUserItem(
    user: SearchUser,
    onAddClick: () -> Unit
) {
    val isAdded = user.checked == 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.middleGap)
            .padding(horizontal = Dimens.middleGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 用户信息
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Column {
                Text(
                    text = user.username,
                    color = Color.Black,
                    fontSize = Dimens.normalFontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = user.userAccount,
                    color = Color.secondary,
                    fontSize = Dimens.normalFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(Dimens.middleGap))

        // 添加按钮
        Button(
            onClick = {
                if (!isAdded) {
                    onAddClick()
                }
            },
            enabled = !isAdded,
            modifier = Modifier
                .width(80.dp)
                .height(Dimens.inputHeight),
            shape = RoundedCornerShape(Dimens.inputHeight / 2),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAdded) Color.Gray else Color.Primary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = if (isAdded) "已添加" else "添加",
                fontSize = Dimens.normalFontSize
            )
        }
    }
}