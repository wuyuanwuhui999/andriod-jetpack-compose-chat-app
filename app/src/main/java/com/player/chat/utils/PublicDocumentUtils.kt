package com.player.chat.utils

import com.player.chat.model.Document

/**
 * 公共文档列表工具：按文档自带的 directoryName 字段分组
 *
 * 与"我的文档"的区别：
 * 1. 公共文档接口（GET /service/chat/getPublicDocList）一次性返回全部公共文档；
 * 2. 文档里自带 directoryName（文档目录名称），因此不需要按目录 id 再查一次接口，
 *    界面按 directoryName 分组展示，点击展开时直接显示分组内的文档。
 */
object PublicDocumentUtils {

    /** directoryName 缺失时的兜底分组名 */
    const val UNGROUPED_DIRECTORY = "未分类"

    /**
     * 按 directoryName 分组，同名目录合并，并保持文档首次出现的先后顺序
     *
     * 注意：Document.directoryName 声明为非空 String，但后端未下发该字段时 Gson 会把 null 写进来
     * （见 DocumentGsonNullSafetyTest），所以这里用 orEmpty()/ifBlank 做容错，
     * 缺失名称的文档归到 [UNGROUPED_DIRECTORY] 分组，避免界面上出现空标题的卡片。
     *
     * @param documents 公共文档列表（可以是空列表）
     * @return 目录名 -> 该目录下的文档列表（LinkedHashMap 语义，顺序 = 首次出现顺序）
     */
    fun groupByDirectoryName(documents: List<Document>): Map<String, List<Document>> =
        documents.groupBy { doc ->
            doc.directoryName.orEmpty().ifBlank { UNGROUPED_DIRECTORY }
        }
}
