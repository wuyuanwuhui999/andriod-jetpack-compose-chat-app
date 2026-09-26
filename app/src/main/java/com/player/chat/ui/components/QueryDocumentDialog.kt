package com.player.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.player.chat.R
import com.player.chat.model.Directory
import com.player.chat.model.Document
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.utils.PublicDocumentUtils
import com.player.chat.viewmodel.ChatViewModel

/** "选择文档"对话框页签下标：0=我的文档（默认激活），1=公共文档 */
private const val TAB_MY_DOCUMENTS = 0
private const val TAB_PUBLIC_DOCUMENTS = 1

/**
 * 查询文档对话框
 * 功能：标题栏为"我的文档 ｜ 公共文档"两个可切换页签（居中、默认激活我的文档）
 * 1. 我的文档：保持原逻辑——目录列表来自接口，点目录才加载该目录下的文档；
 * 2. 公共文档：点击页签时调用 getPublicDocList 一次拿回全部文档，按文档自带 directoryName 分组展示目录卡片，
 *    点击展开直接显示该目录下的文档（不再请求接口）。
 */
@Composable
fun QueryDocumentDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val directories by viewModel.directoryList.collectAsState()
    val expandedDirectories by viewModel.expandedDirectories.collectAsState()
    val directoryDocuments by viewModel.directoryDocuments.collectAsState()
    val isLoading by viewModel.isDirectoryLoading.collectAsState()
    val selectedDocIds by viewModel.selectedDocIds.collectAsState()
    val showCreateDirDialog by viewModel.showCreateDirDialog.collectAsState()
    // 公共文档页签数据
    val publicDocuments by viewModel.publicDocuments.collectAsState()
    val expandedPublicDirectories by viewModel.expandedPublicDirectories.collectAsState()
    val isPublicLoading by viewModel.isPublicDocumentsLoading.collectAsState()

    // 页签：默认激活"我的文档"（对话框每次打开都是初始状态）
    var selectedTab by remember { mutableStateOf(TAB_MY_DOCUMENTS) }

    // 左/右图标区取相同宽度（两个图标位），这样中间的页签相对整条标题栏真正居中
    val sideSlotWidth = Dimens.middleIconSize * 2 + Dimens.middleGap

    // 关键修复：使用 Box 作为根容器，确保 CreateDirectoryDialog 在最上层
    Box(modifier = Modifier.fillMaxSize()) {
        // 查询文档对话框主体
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .fillMaxHeight(fraction = 0.85f)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = Dimens.moduleBorderRadius, topEnd = Dimens.moduleBorderRadius))
            ) {
                // 标题栏：中间是"我的文档 ｜ 公共文档"页签
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.barHeight)
                        .background(Color.White),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(Dimens.middleGap))

                    // 左侧刷新：按当前页签刷新对应数据
                    Box(
                        modifier = Modifier.width(sideSlotWidth),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedTab == TAB_PUBLIC_DOCUMENTS) {
                                    viewModel.loadPublicDocuments()
                                } else {
                                    viewModel.loadDirectoriesForQuery()
                                }
                            },
                            modifier = Modifier.size(Dimens.middleIconSize)
                        ) {
                            Icon(
                                tint = Color.Secondary,
                                painter = painterResource(id = R.drawable.icon_refresh),
                                contentDescription = "刷新",
                                modifier = Modifier.size(Dimens.middleIconSize)
                            )
                        }
                    }

                    // 中间页签：我的文档 ｜ 公共文档（整块水平居中）
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QueryDocTabItem(
                            text = "我的文档",
                            selected = selectedTab == TAB_MY_DOCUMENTS,
                            onClick = { selectedTab = TAB_MY_DOCUMENTS }
                        )

                        // 页签之间的竖线
                        Box(
                            modifier = Modifier
                                .padding(horizontal = Dimens.smallGap)
                                .width(Dimens.borderSize)
                                .height(Dimens.smallIconSize)
                                .background(Color.Gray.copy(alpha = 0.6f))
                        )

                        QueryDocTabItem(
                            text = "公共文档",
                            selected = selectedTab == TAB_PUBLIC_DOCUMENTS,
                            onClick = {
                                selectedTab = TAB_PUBLIC_DOCUMENTS
                                // 点击公共文档页签时调用接口（每次点击都取最新数据）
                                viewModel.loadPublicDocuments()
                            }
                        )
                    }

                    // 右侧创建目录 + 上传（与左侧同宽，保证页签居中）
                    Row(
                        modifier = Modifier.width(sideSlotWidth),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.showCreateDirDialog() },
                            modifier = Modifier.size(Dimens.middleIconSize)
                        ) {
                            Icon(
                                tint = Color.Secondary,
                                painter = painterResource(id = R.drawable.icon_create_directory),
                                contentDescription = "创建目录",
                                modifier = Modifier.size(Dimens.middleIconSize)
                            )
                        }

                        Spacer(modifier = Modifier.width(Dimens.middleGap))

                        IconButton(
                            onClick = {
                                viewModel.hideQueryDocumentDialog()
                                viewModel.showUploadDocumentDialog()
                            },
                            modifier = Modifier.size(Dimens.middleIconSize)
                        ) {
                            Icon(
                                tint = Color.Secondary,
                                painter = painterResource(id = R.drawable.icon_upload),
                                contentDescription = "上传文档",
                                modifier = Modifier.size(Dimens.middleIconSize)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(Dimens.middleGap))
                }

                // 分隔线
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.borderSize)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )

                // 内容区
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.PageBackground)
                        .padding(Dimens.middleGap)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        if (selectedTab == TAB_PUBLIC_DOCUMENTS) {
                            // 公共文档：全部文档已在本地，按 directoryName 分组；展开只是本地状态切换
                            val publicGroups = remember(publicDocuments) {
                                PublicDocumentUtils.groupByDirectoryName(publicDocuments)
                            }
                            when {
                                isPublicLoading && publicGroups.isEmpty() -> {
                                    LoadingHint()
                                }

                                publicGroups.isEmpty() -> {
                                    EmptyHint(text = "暂无公共文档")
                                }

                                else -> {
                                    LazyColumn {
                                        items(publicGroups.entries.toList()) { entry ->
                                            // 目录名取自文档的 directoryName 字段，展开后直接显示分组内的文档
                                            DocumentDirectoryItem(
                                                title = entry.key,
                                                isExpanded = expandedPublicDirectories.contains(entry.key),
                                                documents = entry.value,
                                                selectedDocIds = selectedDocIds,
                                                onDirectoryClick = { viewModel.togglePublicDirectoryExpanded(entry.key) },
                                                onDocumentToggle = { docId -> viewModel.toggleDocSelection(docId) }
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // 我的文档：保持原有逻辑（目录来自接口，点目录才加载文档）
                            if (isLoading && directories.isEmpty()) {
                                LoadingHint()
                            } else if (directories.isEmpty()) {
                                EmptyHint(text = "暂无目录")
                            } else {
                                LazyColumn {
                                    items(directories) { directory ->
                                        DirectoryWithDocumentsItem(
                                            directory = directory,
                                            isExpanded = expandedDirectories.contains(directory.id),
                                            documents = directoryDocuments[directory.id] ?: emptyList(),
                                            selectedDocIds = selectedDocIds,
                                            onDirectoryClick = { viewModel.toggleDirectoryExpanded(directory) },
                                            onDocumentToggle = { docId -> viewModel.toggleDocSelection(docId) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 底部确定按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(Dimens.middleGap),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.middleGap)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.btnHeight / 2),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Secondary
                        )
                    ) {
                        Text("取消", fontSize = Dimens.normalFontSize)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                        },
                        enabled = selectedDocIds.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.btnHeight / 2),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedDocIds.isNotEmpty()) Color.Primary else Color.Gray,
                            contentColor = Color.White
                        )
                    ) {
                        Text("确定", fontSize = Dimens.normalFontSize)
                    }
                }
            }
        }

        // 关键修复：创建目录对话框放在最外层 Box 内部，确保它显示在查询文档对话框之上
        if (showCreateDirDialog) {
            CreateDirectoryDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.hideCreateDirDialog() }
            )
        }
    }
}

/**
 * 标题栏页签文本
 *
 * @param text 页签文案
 * @param selected 是否激活：激活用高亮色（Color.Primary），未激活用黑色
 * @param onClick 点击回调
 */
@Composable
private fun QueryDocTabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = if (selected) Color.Primary else Color.Black,
        fontSize = Dimens.normalFontSize,
        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
        maxLines = 1,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = Dimens.smallGap, vertical = Dimens.smallGap)
    )
}

/** 内容区加载提示（我的文档/公共文档共用） */
@Composable
private fun LoadingHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.middleGap),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(Dimens.bigIconSize),
            color = Color.Primary,
            strokeWidth = Dimens.strokeWidth
        )
    }
}

/** 内容区空数据提示（我的文档/公共文档共用） */
@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.middleGap),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Gray,
            fontSize = Dimens.normalFontSize
        )
    }
}

/**
 * 目录及文档列表项（我的文档：目录对象来自接口）
 */
@Composable
fun DirectoryWithDocumentsItem(
    directory: Directory,
    isExpanded: Boolean,
    documents: List<Document>,
    selectedDocIds: Set<String>,
    onDirectoryClick: () -> Unit,
    onDocumentToggle: (String) -> Unit
) {
    // 目录名来自 Directory；公共文档页签直接用 Document.directoryName，两者共用同一个条目组件
    DocumentDirectoryItem(
        title = directory.directory,
        isExpanded = isExpanded,
        documents = documents,
        selectedDocIds = selectedDocIds,
        onDirectoryClick = onDirectoryClick,
        onDocumentToggle = onDocumentToggle
    )
}

/**
 * 目录及文档列表项（通用：目录标题 + 展开后的文档列表）
 * 我的文档的目录标题来自 Directory.directory，公共文档的目录标题来自 Document.directoryName
 *
 * @param title 目录名称（展示在卡片上）
 * @param isExpanded 是否展开（展开时显示文档列表）
 * @param documents 该目录下的文档列表
 * @param selectedDocIds 已勾选的文档ID集合
 * @param onDirectoryClick 点击目录行（展开/收起）
 * @param onDocumentToggle 勾选/取消勾选文档
 */
@Composable
fun DocumentDirectoryItem(
    title: String,
    isExpanded: Boolean,
    documents: List<Document>,
    selectedDocIds: Set<String>,
    onDirectoryClick: () -> Unit,
    onDocumentToggle: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 目录项
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDirectoryClick() }
                .padding(horizontal = Dimens.middleGap, vertical = Dimens.middleGap),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = Dimens.normalFontSize,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // 箭头图标（展开时顺时针旋转90度，朝下）
            Icon(
                painter = painterResource(id = R.drawable.icon_arrow),
                contentDescription = if (isExpanded) "收起" else "展开",
                tint = Color.Secondary,
                modifier = Modifier
                    .size(Dimens.smallIconSize)
                    .rotate(if (isExpanded) 90f else 0f)
            )
        }

        // 分隔线
        Divider(color = Color.Gray.copy(alpha = 0.2f))

        // 文档列表（展开时显示）
        if (isExpanded) {
            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.middleGap),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "该目录下暂无文档",
                        color = Color.DisableColor,
                        fontSize = Dimens.normalFontSize
                    )
                }
            } else {
                documents.forEach { document ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDocumentToggle(document.id) }
                            .padding(horizontal = Dimens.middleGap, vertical = Dimens.middleGap),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = document.name,
                            color = Color.Black,
                            fontSize = Dimens.normalFontSize,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // 复选框
                        Checkbox(
                            checked = selectedDocIds.contains(document.id),
                            onCheckedChange = { onDocumentToggle(document.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color.Primary,
                                uncheckedColor = Color.Secondary     // 未选中时的颜色
                            )
                        )
                    }

                    if (documents.indexOf(document) < documents.size - 1) {
                        Divider(
                            color = Color.Gray.copy(alpha = 0.1f),
                            modifier = Modifier.padding(start = Dimens.middleGap)
                        )
                    }
                }
            }
        }
    }
}
