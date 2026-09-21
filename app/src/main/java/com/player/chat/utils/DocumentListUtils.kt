package com.player.chat.utils

import com.player.chat.model.Document

/**
 * "目录 -> 文档列表" 本地缓存的同步工具
 *
 * 为什么不用 directoryId 直接查 Map：
 * Map 的 key 来自 Directory.id，而 Document.directoryId 由后端返回，
 * 两者可能不一致（或为空），一旦不一致就直接查不到、改动静默丢失，
 * 表现为"修改权限成功后再打开对话框还是旧权限"。
 * 因此这里统一按文档 id 在整个 Map 上查找/更新，不依赖目录 key。
 */
object DocumentListUtils {

    /**
     * 更新指定文档的权限（就地修改，不使用数据类 copy()）
     *
     * 为什么不用 copy()：Document 的非空字段在运行时可能是 null（后端未下发该字段时，
     * Gson 通过 Unsafe 分配对象、绕过构造函数的非空校验），而 copy() 会对**所有**非空参数
     * 做 null 校验，直接抛
     * "Parameter specified as non-null is null: method com.player.chat.model.Document.copy, parameter xxx"，
     * 该异常会被 ViewModel 的 catch 当成提示语弹出（用户点击"确定"时看到的报错）。
     * 因此这里直接给可变字段 permission 赋值。
     *
     * 注意：就地修改不会改变 Map 内容（同一个 Document 实例），StateFlow 不会重复发射；
     * 当前 UI 在每次打开对话框时重新读取列表，故回显仍是最新值。
     *
     * @param lists 目录-文档列表缓存
     * @param docId 文档ID
     * @param permission 新的权限值
     * @return 实际更新的文档数量（0 表示列表中不存在该文档）
     */
    fun updatePermission(
        lists: Map<String, List<Document>>,
        docId: String,
        permission: String
    ): Int {
        var updated = 0
        lists.values.forEach { documents ->
            documents.forEach { doc ->
                if (doc.id == docId) {
                    doc.permission = permission
                    updated++
                }
            }
        }
        return updated
    }

    /**
     * 从所有目录中移除指定文档
     * @param lists 目录-文档列表缓存
     * @param docId 文档ID
     * @return 移除后的新 Map
     */
    fun removeDocument(
        lists: Map<String, List<Document>>,
        docId: String
    ): Map<String, List<Document>> =
        lists.mapValues { (_, documents) -> documents.filterNot { it.id == docId } }

    /**
     * 按文档 id 在所有目录中查找文档
     * 用于对话框回显时取"当前最新"的文档对象，避免持有旧快照
     * @param lists 目录-文档列表缓存
     * @param docId 文档ID，可为 null
     * @return 命中的文档，未命中返回 null
     */
    fun findDocument(
        lists: Map<String, List<Document>>,
        docId: String?
    ): Document? {
        if (docId == null) return null
        return lists.values.firstNotNullOfOrNull { documents ->
            documents.firstOrNull { it.id == docId }
        }
    }
}
