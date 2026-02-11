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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.R
import com.player.chat.ui.components.Avatar
import com.player.chat.ui.components.AvatarSize
import com.player.chat.ui.theme.Dimens
import com.player.chat.ui.theme.Color as AppColor
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
                        color = AppColor.Black
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = AppColor.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColor.White,
                    titleContentColor = AppColor.Black,
                    navigationIconContentColor = AppColor.Black
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColor.pageBackgroundColor),
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
                                color = AppColor.Black,
                                fontWeight = FontWeight.Medium
                            )
                            Avatar(
                                avatarUrl = currentUser?.avatar,
                                size = AvatarSize.MIDDLE
                            )
                        }

                        Divider(color = AppColor.Gray.copy(alpha = 0.2f))

                        // 昵称
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "昵称",
                                color = AppColor.Black,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currentUser?.username ?: "",
                                color = AppColor.Gray
                            )
                        }

                        Divider(color = AppColor.Gray.copy(alpha = 0.2f))

                        // 邮箱
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "邮箱",
                                color = AppColor.Black,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currentUser?.email ?: "",
                                color = AppColor.Gray
                            )
                        }

                        Divider(color = AppColor.Gray.copy(alpha = 0.2f))

                        // 性别
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "性别",
                                color = AppColor.Black,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = when (currentUser?.sex) {
                                    0 -> "男"
                                    1 -> "女"
                                    else -> "未知"
                                },
                                color = AppColor.Gray
                            )
                        }

                        Divider(color = AppColor.Gray.copy(alpha = 0.2f))

                        // 地区
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "地区",
                                color = AppColor.Black,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currentUser?.region ?: "未设置",
                                color = AppColor.Gray
                            )
                        }

                        Divider(color = AppColor.Gray.copy(alpha = 0.2f))

                        // 个性签名
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "个性签名",
                                color = AppColor.Black,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currentUser?.sign ?: "无时无刻不想你",
                                color = AppColor.Gray,
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
                        border = BorderStroke(Dimens.borderSize, AppColor.disableTextColor)

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
                            // TODO: 租户管理
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.bigBorderRadius),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(Dimens.borderSize, AppColor.disableTextColor)

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
                        border = BorderStroke(Dimens.borderSize, AppColor.disableTextColor)

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

    // 租户选择对话框
    if (showTenantDialog) {
        AlertDialog(
            onDismissRequest = { userViewModel.hideTenantDialog() },
            title = { Text("选择租户") },
            text = {
                Column {
                    tenantList.forEach { tenant ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = tenant.name,
                                    color = if (tenant.id == currentTenant?.id)
                                        AppColor.PrimaryColor
                                    else
                                        AppColor.Black
                                )
                            },
                            modifier = Modifier.clickable {
                                userViewModel.selectTenant(tenant)
                            }
                        )
                        Divider()
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { userViewModel.hideTenantDialog() }
                ) {
                    Text("取消")
                }
            }
        )
    }

    // 退出登录确认对话框
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { userViewModel.hideLogoutDialog() },
            title = { Text("退出登录") },
            text = { Text("确定要退出登录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userViewModel.logout()
                        // 退出登录后清空返回栈并跳转到登录页
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true } // 清空所有页面
                        }
                        userViewModel.hideLogoutDialog()
                    }
                ) {
                    Text("确定", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { userViewModel.hideLogoutDialog() }) {
                    Text("取消")
                }
            }
        )
    }
}