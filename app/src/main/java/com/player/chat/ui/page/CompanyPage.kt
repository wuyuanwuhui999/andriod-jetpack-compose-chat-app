package com.player.chat.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.player.chat.model.Company
import com.player.chat.navigation.Screens
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.CompanyViewModel
import kotlinx.coroutines.launch

/**
 * 公司选择页面
 * 展示公司列表供用户选择，选择后保存公司信息并跳转到聊天页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyPage(
    navController: NavHostController,
    viewModel: CompanyViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 状态收集
    val companyList by viewModel.companyList.collectAsStateWithLifecycle()
    val selectedCompany by viewModel.selectedCompany.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isFromLogin by viewModel.isFromLogin.collectAsStateWithLifecycle()

    // 页面加载时获取公司列表
    LaunchedEffect(Unit) {
        viewModel.loadCompanyList()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "选择公司",
                        color = Color.Black,
                        fontSize = Dimens.normalFontSize,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    // 只有从用户页面进入时才显示返回按钮
                    if (!isFromLogin) {
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
                .padding(paddingValues)
                .background(Color.PageBackground)
                .padding(Dimens.middleGap)
        ) {
            // 公司列表卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                when {
                    isLoading && companyList.isEmpty() -> {
                        // 加载中状态
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Dimens.middleGap),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
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
                    companyList.isEmpty() -> {
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
                                Text(
                                    text = "暂无公司列表",
                                    color = Color.Gray,
                                    fontSize = Dimens.normalFontSize
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(Dimens.middleGap),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            items(
                                items = companyList,
                                key = { it.id }
                            ) { company ->
                                CompanyItem(
                                    company = company,
                                    isSelected = selectedCompany?.id == company.id,
                                    onSelect = { viewModel.selectCompany(company) }
                                )
                                
                                // 分隔线（最后一条不显示）
                                if (companyList.indexOf(company) < companyList.size - 1) {
                                    Divider(
                                        color = Color.Gray.copy(alpha = 0.2f),
                                        thickness = Dimens.borderSize,
                                        modifier = Modifier.padding(horizontal = 0.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 确定按钮
            Button(
                onClick = {
                    scope.launch {
                        val success = viewModel.confirmSelection()
                        if (success) {
                            // 跳转到聊天页面，清除返回栈
                            navController.navigate(Screens.Chat.route) {
                                popUpTo(Screens.Company.route) { inclusive = true }
                            }
                        }
                    }
                },
                enabled = selectedCompany != null && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.btnHeight),
                shape = RoundedCornerShape(Dimens.btnHeight / 2),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedCompany != null && !isLoading) Color.Primary else Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "确定",
                    fontSize = Dimens.normalFontSize
                )
            }
        }
    }
}

/**
 * 公司列表项组件
 */
@Composable
fun CompanyItem(
    company: Company,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = Dimens.middleGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 公司信息
        Text(
            text = company.name,
            color = Color.Black,
            fontSize = Dimens.normalFontSize,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.width(Dimens.middleGap))
        
        // 单选按钮
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) Color.Primary else Color.Gray.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}