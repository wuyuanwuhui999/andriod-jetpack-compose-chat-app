// UserPage.kt
package com.player.chat.ui.page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.navigation.Screens
import com.player.chat.ui.components.Avatar
import com.player.chat.ui.components.AvatarSize
import com.player.chat.ui.components.CustomAlertDialog
import com.player.chat.ui.components.CustomBottomOption
import com.player.chat.ui.components.OptionItem
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.MainViewModel
import com.player.chat.viewmodel.UserViewModel
import com.player.chat.ui.theme.Color
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPage(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val currentUser by mainViewModel.currentUser.collectAsState()
    val currentTenant by userViewModel.currentTenant.collectAsState()
    val tenantList by userViewModel.tenantList.collectAsState()
    val showTenantDialog by userViewModel.showTenantDialog.collectAsState()
    val showLogoutDialog by userViewModel.showLogoutDialog.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentTenant?.name ?: "私人空间",
                        color = Color.Black
                    )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.pageBackgroundColor),
            contentPadding = PaddingValues(Dimens.pagePadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.pagePadding)
        ) {
            // 用户信息卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.pagePadding),
                        verticalArrangement = Arrangement.spacedBy(Dimens.pagePadding)
                    ) {
                        // 头像
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "头像",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                            Avatar(
                                avatarUrl = currentUser?.avatar,
                                size = AvatarSize.MIDDLE
                            )
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // 昵称
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "昵称",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currentUser?.username ?: "",
                                color = Color.Gray
                            )
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // 邮箱
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "邮箱",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currentUser?.email ?: "",
                                color = Color.Gray
                            )
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // 性别
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "性别",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = when (currentUser?.sex) {
                                    0 -> "男"
                                    1 -> "女"
                                    else -> "未知"
                                },
                                color = Color.Gray
                            )
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // 地区
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "地区",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currentUser?.region ?: "未设置",
                                color = Color.Gray
                            )
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // 个性签名
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "个性签名",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currentUser?.sign ?: "无时无刻不想你",
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }
                    }
                }
            }



            // 按钮组
            item {
                Column() {
                    // 切换租户
                    OutlinedButton(
                        onClick = {
                            userViewModel.showTenantDialog()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.bigBorderRadius),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(Dimens.borderSize, Color.disableTextColor)

                    ) {
                        Text(
                            text = "切换租户",
                            fontSize = Dimens.fontSizeNormal,
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.pagePadding))

                    // 租户管理
                    OutlinedButton(
                        onClick = {
                            navController.navigate(Screens.TenantManage.route)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.bigBorderRadius),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(Dimens.borderSize, Color.disableTextColor)

                    ) {
                        Text(
                            text = "租户管理",
                            fontSize = Dimens.fontSizeNormal,
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.pagePadding))

                    // 修改密码按钮
                    OutlinedButton(
                        onClick = {
                            // 修改密码
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.bigBorderRadius),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(Dimens.borderSize, Color.disableTextColor)

                    ) {
                        Text(
                            text = "修改密码",

                            fontSize = Dimens.fontSizeNormal,
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.pagePadding))

                    // 退出登录
                    Button(
                        onClick = {
                            userViewModel.showLogoutDialog()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.bigBorderRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor =Color.PrimaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text("退出登录")
                    }
                }
            }
        }
    }

    // 租户选择对话框 - 使用 CustomBottomOption
    if (showTenantDialog) {
        // 将租户列表转换为 OptionItem 列表
        val tenantOptions = remember(tenantList) {
            tenantList.map { tenant ->
                OptionItem(
                    name = tenant.name,
                    value = tenant.id
                )
            }
        }

        CustomBottomOption(
            options = tenantOptions,
            selectedValue = currentTenant?.id ?: "", // 当前选中的租户ID
            onOptionSelected = { value, index ->
                // 根据选中的 value（租户ID）找到对应的租户对象
                val selectedTenant = tenantList.find { it.id == value }
                selectedTenant?.let {
                    userViewModel.selectTenant(it) // 调用 ViewModel 的方法切换租户
                }
            },
            onDismiss = { userViewModel.hideTenantDialog() }
        )
    }

    // 退出登录确认对话框
    if (showLogoutDialog) {
        CustomAlertDialog(
            title="退出登录",
            onConfirm = {
                userViewModel.logout()
                // 退出登录后清空返回栈并跳转到登录页
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true } // 清空所有页面
                }
                userViewModel.hideLogoutDialog()
            },
            onDismiss = {
                userViewModel.hideLogoutDialog()
            }
        ){
            Text("确定要退出登录吗？")
        }
    }
}