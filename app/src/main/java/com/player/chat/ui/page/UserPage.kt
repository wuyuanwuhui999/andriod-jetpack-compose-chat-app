// UserPage.kt
package com.player.chat.ui.page

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.navigation.Screens
import com.player.chat.ui.components.*
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.MainViewModel
import com.player.chat.viewmodel.UserViewModel
import com.player.chat.viewmodel.UpdateResult
import com.player.chat.ui.theme.Color
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 用户信息页面
 * 支持查看和编辑用户信息，包括头像上传、昵称、电话、邮箱、性别、出生日期、地区、个性签名等
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPage(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    // 从 MainViewModel 获取当前用户信息
    val currentUser by mainViewModel.currentUser.collectAsState()
    val currentTenant by userViewModel.currentTenant.collectAsState()
    val tenantList by userViewModel.tenantList.collectAsState()
    val showTenantDialog by userViewModel.showTenantDialog.collectAsState()
    val showLogoutDialog by userViewModel.showLogoutDialog.collectAsState()
    val updateResult by userViewModel.updateResult.collectAsState()

    val context = LocalContext.current

    // 编辑对话框状态
    var showEditDialog by remember { mutableStateOf(false) }
    var editField by remember { mutableStateOf<EditField?>(null) }
    var editValue by remember { mutableStateOf("") }

    // 日期选择器状态
    var showDatePicker by remember { mutableStateOf(false) }

    // 头像选择器 - 从相册选择
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val tempFile = createTempFileFromUri(context, it)
            tempFile?.let { file ->
                userViewModel.updateAvatar(file)
            }
        }
    }

    // 相机拍照
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val tempFile = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { outputStream ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            userViewModel.updateAvatar(tempFile)
        }
    }

    // 头像选择对话框
    var showAvatarOptions by remember { mutableStateOf(false) }

    // 监听更新结果，显示提示
    LaunchedEffect(updateResult) {
        updateResult?.let { result ->
            // 可以在这里添加 Toast 或 Snackbar 提示
            when (result) {
                is UpdateResult.Success -> {
                    // 显示成功提示
                    android.widget.Toast.makeText(context, result.message, android.widget.Toast.LENGTH_SHORT).show()
                }
                is UpdateResult.Error -> {
                    // 显示错误提示
                    android.widget.Toast.makeText(context, result.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            kotlinx.coroutines.delay(2000)
            userViewModel.resetUpdateResult()
        }
    }

    // 性别选项
    val genderOptions = listOf("男", "女")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentTenant?.name ?: "私人空间",
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
                            tint = Color.Black,
                            modifier = Modifier.size(Dimens.middleIconSize)
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
                .background(Color.PageBackground),
            contentPadding = PaddingValues(Dimens.middleGap),
            verticalArrangement = Arrangement.spacedBy(Dimens.middleGap)
        ) {
            // 用户信息卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.middleGap),
                        verticalArrangement = Arrangement.spacedBy(0.dp)  // 分隔线自带间距，不需要额外间距
                    ) {
                        // 头像 - 可点击
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAvatarOptions = true }
                                .padding(vertical = Dimens.middleGap),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "头像",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                fontSize = Dimens.normalFontSize
                            )
                            Avatar(
                                avatarUrl = currentUser?.avatar,
                                size = AvatarSize.BIG
                            )
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // 昵称 - 可点击编辑（必填）
                        EditableInfoRow(
                            label = "昵称",
                            value = currentUser?.username ?: "",
                            isRequired = true,
                            onClick = {
                                editField = EditField.NICKNAME
                                editValue = currentUser?.username ?: ""
                                showEditDialog = true
                            }
                        )

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // 电话 - 可点击编辑
                        EditableInfoRow(
                            label = "电话",
                            value = currentUser?.telephone ?: "",
                            isRequired = false,
                            onClick = {
                                editField = EditField.PHONE
                                editValue = currentUser?.telephone ?: ""
                                showEditDialog = true
                            }
                        )

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // 邮箱 - 可点击编辑（必填）
                        EditableInfoRow(
                            label = "邮箱",
                            value = currentUser?.email ?: "",
                            isRequired = true,
                            onClick = {
                                editField = EditField.EMAIL
                                editValue = currentUser?.email ?: ""
                                showEditDialog = true
                            }
                        )

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // 性别 - 单选按钮
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.middleGap),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "性别",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                fontSize = Dimens.normalFontSize
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Dimens.middleGap)
                            ) {
                                genderOptions.forEach { gender ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            val newSex = if (gender == "男") 0 else 1
                                            currentUser?.let { user ->
                                                val updatedUser = user.copy(sex = newSex)
                                                userViewModel.updateUserInfo(updatedUser)
                                            }
                                        }
                                    ) {
                                        RadioButton(
                                            selected = when (currentUser?.sex) {
                                                0 -> gender == "男"
                                                1 -> gender == "女"
                                                else -> false
                                            },
                                            onClick = {
                                                val newSex = if (gender == "男") 0 else 1
                                                currentUser?.let { user ->
                                                    val updatedUser = user.copy(sex = newSex)
                                                    userViewModel.updateUserInfo(updatedUser)
                                                }
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color.Primary
                                            )
                                        )
                                        Text(
                                            text = gender,
                                            color = Color.Black,
                                            fontSize = Dimens.normalFontSize,
                                            modifier = Modifier.padding(start = Dimens.smallGap)
                                        )
                                    }
                                }
                            }
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // 出生日期 - 可点击
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }
                                .padding(vertical = Dimens.middleGap),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "出生日期",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                fontSize = Dimens.normalFontSize
                            )
                            Text(
                                text = currentUser?.birthday?.takeIf { it.isNotBlank() } ?: "未设置",
                                color = if (currentUser?.birthday.isNullOrBlank()) Color.Gray else Color.Black,
                                fontSize = Dimens.normalFontSize
                            )
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // 地区 - 可点击编辑
                        EditableInfoRow(
                            label = "地区",
                            value = currentUser?.region ?: "",
                            isRequired = false,
                            onClick = {
                                editField = EditField.REGION
                                editValue = currentUser?.region ?: ""
                                showEditDialog = true
                            }
                        )

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // 个性签名 - 可点击编辑
                        EditableInfoRow(
                            label = "个性签名",
                            value = currentUser?.sign ?: "",
                            isRequired = false,
                            onClick = {
                                editField = EditField.SIGN
                                editValue = currentUser?.sign ?: ""
                                showEditDialog = true
                            }
                        )
                    }
                }
            }

            // 退出登录按钮
            item {
                Button(
                    onClick = { userViewModel.showLogoutDialog() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.btnHeight),
                    shape = RoundedCornerShape(Dimens.btnHeight / 2),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "退出登录",
                        fontSize = Dimens.normalFontSize
                    )
                }
            }
        }
    }

    // 编辑对话框
    if (showEditDialog && editField != null) {
        InputDialog(
            title = when (editField) {
                EditField.NICKNAME -> "编辑昵称"
                EditField.PHONE -> "编辑电话"
                EditField.EMAIL -> "编辑邮箱"
                EditField.REGION -> "编辑地区"
                EditField.SIGN -> "编辑个性签名"
                else -> ""
            },
            initialValue = editValue,
            hint = when (editField) {
                EditField.NICKNAME -> "请输入昵称"
                EditField.PHONE -> "请输入电话号码"
                EditField.EMAIL -> "请输入邮箱地址"
                EditField.REGION -> "请输入地区"
                EditField.SIGN -> "请输入个性签名"
                else -> ""
            },
            isRequired = editField == EditField.NICKNAME || editField == EditField.EMAIL,
            keyboardType = when (editField) {
                EditField.PHONE -> KeyboardType.Phone
                EditField.EMAIL -> KeyboardType.Email
                else -> KeyboardType.Text
            },
            validator = when (editField) {
                EditField.PHONE -> { value ->
                    value.isBlank() || android.util.Patterns.PHONE.matcher(value).matches()
                }
                EditField.EMAIL -> { value ->
                    value.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()
                }
                else -> null
            },
            onConfirm = { newValue ->
                val updatedUser = when (editField) {
                    EditField.NICKNAME -> currentUser?.copy(username = newValue)
                    EditField.PHONE -> currentUser?.copy(telephone = newValue)
                    EditField.EMAIL -> currentUser?.copy(email = newValue)
                    EditField.REGION -> currentUser?.copy(region = newValue)
                    EditField.SIGN -> currentUser?.copy(sign = newValue)
                    else -> null
                }
                updatedUser?.let {
                    userViewModel.updateUserInfo(it)
                }
                showEditDialog = false
                editField = null
            },
            onDismiss = {
                showEditDialog = false
                editField = null
            }
        )
    }

    // 日期选择器
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                currentUser?.let { user ->
                    val updatedUser = user.copy(birthday = formattedDate)
                    userViewModel.updateUserInfo(updatedUser)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            initialDate = currentUser?.birthday
        )
    }

    // 头像选择对话框
    if (showAvatarOptions) {
        AlertDialog(
            onDismissRequest = { showAvatarOptions = false },
            title = { Text("选择头像") },
            text = { Text("请选择图片来源") },
            confirmButton = {
                TextButton(onClick = {
                    avatarPickerLauncher.launch("image/*")
                    showAvatarOptions = false
                }) {
                    Text("相册")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    cameraLauncher.launch(null)
                    showAvatarOptions = false
                }) {
                    Text("相机")
                }
            }
        )
    }

    // 租户选择对话框
    if (showTenantDialog) {
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
            selectedValue = currentTenant?.id ?: "",
            onOptionSelected = { value, _ ->
                val selectedTenant = tenantList.find { it.id == value }
                selectedTenant?.let {
                    userViewModel.selectTenant(it)
                }
            },
            onDismiss = { userViewModel.hideTenantDialog() }
        )
    }

    // 退出登录确认对话框
    if (showLogoutDialog) {
        CustomAlertDialog(
            title = "退出登录",
            onConfirm = {
                userViewModel.logout()
                // 清空返回栈并跳转到登录页
                navController.navigate(Screens.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
                userViewModel.hideLogoutDialog()
            },
            onDismiss = {
                userViewModel.hideLogoutDialog()
            }
        ) {
            Text("确定要退出登录吗？")
        }
    }
}

/**
 * 可编辑信息行组件
 * @param label 标签文字
 * @param value 当前值
 * @param isRequired 是否必填
 * @param onClick 点击回调
 */
@Composable
fun EditableInfoRow(
    label: String,
    value: String,
    isRequired: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = Dimens.middleGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧：标签 + 必填星号
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontSize = Dimens.normalFontSize
            )
            if (isRequired) {
                Text(
                    text = " *",
                    color = Color.Red,
                    fontSize = Dimens.normalFontSize
                )
            }
        }

        // 右侧：值（未设置时显示灰色提示）
        Text(
            text = value.ifEmpty { "未设置" },
            color = if (value.isEmpty()) Color.Gray else Color.Black,
            fontSize = Dimens.normalFontSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
            textAlign = TextAlign.End
        )
    }
}

/**
 * 编辑字段枚举
 */
enum class EditField {
    NICKNAME, PHONE, EMAIL, REGION, SIGN
}

/**
 * 从URI创建临时文件
 * @param context 上下文
 * @param uri 文件URI
 * @return 临时文件，失败返回null
 */
private fun createTempFileFromUri(context: android.content.Context, uri: Uri): File? {
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
        e.printStackTrace()
        null
    }
}