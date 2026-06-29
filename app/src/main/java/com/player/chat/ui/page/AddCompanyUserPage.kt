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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.player.chat.R
import com.player.chat.config.Config
import com.player.chat.model.Department
import com.player.chat.model.Position
import com.player.chat.model.SearchUser
import com.player.chat.ui.components.CustomAlertDialog
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.AddUserViewModel

/**
 * 添加用户页面
 * 搜索用户并添加到当前公司
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCompanyUserPage(
    navController: NavHostController,
    viewModel: AddUserViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMoreData by viewModel.hasMoreData.collectAsState()
    val searchKeyword by viewModel.searchKeyword.collectAsState()
    val currentCompany by viewModel.currentCompany.collectAsState()
    val isSuperAdmin by viewModel.isSuperAdmin.collectAsState()
    val showEndTip by viewModel.showEndTip.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    // 添加用户对话框状态
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<SearchUser?>(null) }

    val listState = rememberLazyListState()

    // 监听滚动到底部
    LaunchedEffect(listState.layoutInfo) {
        val layoutInfo = listState.layoutInfo
        if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.last().index
            val totalItemsCount = layoutInfo.totalItemsCount

            if (lastVisibleItemIndex >= totalItemsCount - 1 &&
                !isLoading && !isLoadingMore && hasMoreData && searchResults.isNotEmpty()
            ) {
                Log.d("AddUser", "滚动到底部，加载更多")
                viewModel.loadMoreSearchResults()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "添加用户",
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

            // 搜索结果列表卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                        searchKeyword.isEmpty() -> {
                            // 空状态 - 提示输入搜索关键字
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
                                    .fillMaxSize()
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
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                items(
                                    items = searchResults,
                                    key = { it.id }
                                ) { user ->
                                    SearchUserItem(
                                        user = user,
                                        isSuperAdmin = isSuperAdmin,
                                        onAddClick = {
                                            // 如果用户已添加，不执行任何操作
                                            if (user.checked == 1) return@SearchUserItem
                                            // 如果是超级管理员，弹出对话框选择角色
                                            if (isSuperAdmin) {
                                                selectedUser = user
                                                showAddDialog = true
                                            } else {
                                                // 普通管理员直接添加（role=0 普通用户）
                                                viewModel.addUserDirectly(user.id)
                                            }
                                        }
                                    )

                                    // 分隔线
                                    if (searchResults.indexOf(user) < searchResults.size - 1) {
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
                                if (!hasMoreData && searchResults.isNotEmpty()) {
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

    // 添加用户对话框
    if (showAddDialog && selectedUser != null) {
        AddUserDialog(
            user = selectedUser!!,
            isSuperAdmin = isSuperAdmin,
            companyId = currentCompany?.id ?: "",
            onConfirm = { role, positionId ->
                viewModel.addUserWithRole(selectedUser!!.id, role, positionId)
                showAddDialog = false
                selectedUser = null
            },
            onDismiss = {
                showAddDialog = false
                selectedUser = null
            }
        )
    }

    // 首次加载时刷新数据
    LaunchedEffect(Unit) {
        viewModel.loadCompanyInfo()
    }
}

/**
 * 搜索用户条目组件
 */
@Composable
fun SearchUserItem(
    user: SearchUser,
    isSuperAdmin: Boolean,
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
                    text = "工号: ${user.id.takeLast(8)}",
                    color = Color.Gray,
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

/**
 * 添加用户对话框
 * 选择角色和职位
 */
@Composable
fun AddUserDialog(
    user: SearchUser,
    isSuperAdmin: Boolean,
    companyId: String,
    onConfirm: (role: Int, positionId: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel: AddUserViewModel = hiltViewModel()

    var selectedRole by remember { mutableStateOf(0) } // 0: 普通用户, 1: 管理员
    var selectedDepartment by remember { mutableStateOf<Department?>(null) }
    var selectedPosition by remember { mutableStateOf<Position?>(null) }

    val departments by viewModel.departments.collectAsState()
    val positions by viewModel.positions.collectAsState()
    val isLoadingDepartments by viewModel.isLoadingDepartments.collectAsState()
    val isLoadingPositions by viewModel.isLoadingPositions.collectAsState()

    // 加载部门列表
    LaunchedEffect(companyId) {
        if (companyId.isNotBlank()) {
            viewModel.loadDepartments(companyId)
        }
    }

    // 当部门变化时加载职位列表
    LaunchedEffect(selectedDepartment) {
        selectedDepartment?.let {
            viewModel.loadPositions(it.id)
        }
    }

    // 重置职位选择当部门变化
    LaunchedEffect(selectedDepartment) {
        selectedPosition = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.9f)
                .wrapContentHeight()
                .align(Alignment.Center)
                .clip(RoundedCornerShape(Dimens.moduleBorderRadius))
                .clickable(enabled = false) { }, // 阻止点击透传
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.middleGap)
            ) {
                // 标题
                Text(
                    text = "添加用户",
                    color = Color.Black,
                    fontSize = Dimens.bigFontSize,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = Dimens.middleGap)
                )

                // 用户信息
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = Dimens.middleGap)
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

                    Column {
                        Text(
                            text = user.username,
                            color = Color.Black,
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

                Divider(
                    color = Color.Gray.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = Dimens.smallGap)
                )

                // 角色选择（仅超级管理员显示）
                if (isSuperAdmin) {
                    Column(
                        modifier = Modifier.padding(vertical = Dimens.smallGap)
                    ) {
                        Text(
                            text = "角色",
                            color = Color.Black,
                            fontSize = Dimens.normalFontSize,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = Dimens.smallGap)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.middleGap)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    selectedRole = 0
                                }
                            ) {
                                RadioButton(
                                    selected = selectedRole == 0,
                                    onClick = { selectedRole = 0 },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color.Primary
                                    )
                                )
                                Text(
                                    text = "普通用户",
                                    color = Color.Black,
                                    fontSize = Dimens.normalFontSize
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    selectedRole = 1
                                }
                            ) {
                                RadioButton(
                                    selected = selectedRole == 1,
                                    onClick = { selectedRole = 1 },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color.Primary
                                    )
                                )
                                Text(
                                    text = "管理员",
                                    color = Color.Black,
                                    fontSize = Dimens.normalFontSize
                                )
                            }
                        }
                    }

                    Divider(
                        color = Color.Gray.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = Dimens.smallGap)
                    )
                }

                // 部门选择
                Column(
                    modifier = Modifier.padding(vertical = Dimens.smallGap)
                ) {
                    Text(
                        text = "部门",
                        color = Color.Black,
                        fontSize = Dimens.normalFontSize,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = Dimens.smallGap)
                    )

                    if (isLoadingDepartments) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.inputHeight),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.Primary
                            )
                        }
                    } else if (departments.isEmpty()) {
                        Text(
                            text = "暂无部门",
                            color = Color.Gray,
                            fontSize = Dimens.normalFontSize,
                            modifier = Modifier.padding(vertical = Dimens.smallGap)
                        )
                    } else {
                        // 部门下拉选择
                        var expanded by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimens.inputHeight),
                                shape = RoundedCornerShape(Dimens.inputHeight / 2),
                                color = Color.PageBackground
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = Dimens.middleGap),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = selectedDepartment?.departmentName ?: "请选择部门",
                                        color = if (selectedDepartment == null) Color.Gray else Color.Black,
                                        fontSize = Dimens.normalFontSize
                                    )
                                    Icon(
                                        painter = painterResource(
                                            if (expanded) R.drawable.icon_down else R.drawable.icon_arrow
                                        ),
                                        contentDescription = if (expanded) "收起" else "展开",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(Dimens.smallIconSize)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                            ) {
                                departments.forEach { department ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = department.departmentName,
                                                color = Color.Black,
                                                fontSize = Dimens.normalFontSize
                                            )
                                        },
                                        onClick = {
                                            selectedDepartment = department
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(
                    color = Color.Gray.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = Dimens.smallGap)
                )

                // 职位选择
                Column(
                    modifier = Modifier.padding(vertical = Dimens.smallGap)
                ) {
                    Text(
                        text = "职位",
                        color = Color.Black,
                        fontSize = Dimens.normalFontSize,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = Dimens.smallGap)
                    )

                    if (selectedDepartment == null) {
                        Text(
                            text = "请先选择部门",
                            color = Color.Gray,
                            fontSize = Dimens.normalFontSize,
                            modifier = Modifier.padding(vertical = Dimens.smallGap)
                        )
                    } else if (isLoadingPositions) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.inputHeight),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.Primary
                            )
                        }
                    } else if (positions.isEmpty()) {
                        Text(
                            text = "该部门暂无职位",
                            color = Color.Gray,
                            fontSize = Dimens.normalFontSize,
                            modifier = Modifier.padding(vertical = Dimens.smallGap)
                        )
                    } else {
                        // 职位下拉选择
                        var expanded by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimens.inputHeight),
                                shape = RoundedCornerShape(Dimens.inputHeight / 2),
                                color = Color.PageBackground
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = Dimens.middleGap),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = selectedPosition?.positionName ?: "请选择职位",
                                        color = if (selectedPosition == null) Color.Gray else Color.Black,
                                        fontSize = Dimens.normalFontSize
                                    )
                                    Icon(
                                        painter = painterResource(
                                            if (expanded) R.drawable.icon_down else R.drawable.icon_arrow
                                        ),
                                        contentDescription = if (expanded) "收起" else "展开",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(Dimens.smallIconSize)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                            ) {
                                positions.forEach { position ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = position.positionName,
                                                color = Color.Black,
                                                fontSize = Dimens.normalFontSize
                                            )
                                        },
                                        onClick = {
                                            selectedPosition = position
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.middleGap))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.middleGap)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.btnHeight / 2)
                    ) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            onConfirm(selectedRole, selectedPosition?.id)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.btnHeight / 2),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Primary
                        )
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}