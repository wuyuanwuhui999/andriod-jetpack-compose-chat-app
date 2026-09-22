package com.player.chat

import com.player.chat.model.Document
import com.player.chat.utils.DocumentListUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 本地"目录 -> 文档列表"同步逻辑的行为测试
 *
 * 两个回归点：
 * 1. 本地 Map 的 key 是 Directory.id，而 Document.directoryId 来自后端，两者不一致时也要按文档 id 生效；
 * 2. 同步不使用数据类 copy()（后端缺字段时 Gson 会把 null 写进非空类型字段，copy() 会抛异常）。
 */
class DocumentListUtilsTest {

    private fun doc(id: String, dirId: String, permission: String? = "private") =
        Document(
            id = id, name = "doc-$id", ext = "pdf", userId = "u1",
            createTime = "2026-01-01 00:00:00", updateTime = "2026-01-01 00:00:00",
            directoryId = dirId, directoryName = "dir", permission = permission
        )

    /** 正常情况：文档在 key 匹配的目录下，权限被更新 */
    @Test
    fun updatePermission_updatesMatchingDoc() {
        val lists = mapOf("d1" to listOf(doc("a", "d1"), doc("b", "d1")))

        val updated = DocumentListUtils.updatePermission(lists, "a", "tenant")

        assertEquals("命中 1 个文档", 1, updated)
        assertEquals("tenant", lists.getValue("d1").first { it.id == "a" }.permission)
        assertEquals("其他文档不受影响", "private", lists.getValue("d1").first { it.id == "b" }.permission)
    }

    /** 回归用例：Document.directoryId 与 Map 的 key 不一致时，仍要能更新 */
    @Test
    fun updatePermission_worksWhenDirectoryIdDoesNotMatchMapKey() {
        val lists = mapOf("dir-key-1" to listOf(doc("a", "OTHER-DIR-ID")))

        DocumentListUtils.updatePermission(lists, "a", "company")

        assertEquals("company", lists.getValue("dir-key-1").single().permission)
    }

    /** 文档 id 不存在时，列表内容保持不变且返回 0 */
    @Test
    fun updatePermission_unknownDocLeavesListsIntact() {
        val lists = mapOf("d1" to listOf(doc("a", "d1")))

        val updated = DocumentListUtils.updatePermission(lists, "missing", "tenant")

        assertEquals(0, updated)
        assertEquals(listOf("a"), lists.getValue("d1").map { it.id })
        assertEquals("private", lists.getValue("d1").single().permission)
    }

    /** 删除：按 id 从所有目录移除，key 不匹配也能删掉 */
    @Test
    fun removeDocument_removesFromEveryListByDocId() {
        val lists = mapOf(
            "d1" to listOf(doc("a", "MISMATCHED"), doc("b", "d1")),
            "d2" to listOf(doc("c", "d2"))
        )
        val updated = DocumentListUtils.removeDocument(lists, "a")
        assertEquals(listOf("b"), updated.getValue("d1").map { it.id })
        assertEquals(listOf("c"), updated.getValue("d2").map { it.id })
    }

    /** 查找：findDocument 返回列表中最新的对象，且支持 null / 未命中 */
    @Test
    fun findDocument_returnsLatestAndHandlesMisses() {
        val lists = mapOf("d1" to listOf(doc("a", "d1", "tenant")))
        val found = DocumentListUtils.findDocument(lists, "a")
        assertEquals("tenant", found?.permission)
        assertSame(found, lists.getValue("d1").single())
        assertNull(DocumentListUtils.findDocument(lists, "nope"))
        assertNull(DocumentListUtils.findDocument(lists, null))
    }

    /** 回归用户反馈："点击取消/确定后修改权限对话框不消失" */
    @Test
    fun resolveDialogDocument_closedWhenDocIdIsNull_evenIfSnapshotRemains() {
        val lists = mapOf("d1" to listOf(doc("a", "d1")))
        val snapshot = doc("a", "d1")

        // 关闭时只置空 docId，快照还残留（或相反）都必须返回 null，否则对话框关不掉
        assertNull(DocumentListUtils.resolveDialogDocument(lists, null, snapshot))
        assertNull(DocumentListUtils.resolveDialogDocument(lists, null, null))
    }

    /** 打开时：优先列表最新值；列表里查不到才退回快照 */
    @Test
    fun resolveDialogDocument_prefersLatestThenFallsBackToSnapshot() {
        val lists = mapOf("d1" to listOf(doc("a", "d1", "tenant")))
        val staleSnapshot = doc("a", "d1", "private")

        val latest = DocumentListUtils.resolveDialogDocument(lists, "a", staleSnapshot)
        assertEquals("列表里有则以列表为准（回显最新权限）", "tenant", latest?.permission)

        val fallback = DocumentListUtils.resolveDialogDocument(emptyMap(), "a", staleSnapshot)
        assertSame("列表里没有则退回点击时的快照", staleSnapshot, fallback)
    }

    /** 空缓存不抛异常 */
    @Test
    fun emptyLists_areSafe() {
        assertEquals(0, DocumentListUtils.updatePermission(emptyMap(), "a", "tenant"))
        assertTrue(DocumentListUtils.removeDocument(emptyMap(), "a").isEmpty())
        assertNull(DocumentListUtils.findDocument(emptyMap(), "a"))
    }

    /** 回归用户反馈："修改权限成功后再打开对话框，下拉框还是旧权限" */
    @Test
    fun reopenAfterSuccessfulUpdate_seedsNewPermission() {
        // 初始：文档权限为 private（下拉框回显 private）
        val lists = mapOf("d1" to listOf(doc("a", "d1", "private")))
        assertEquals("private", DocumentListUtils.findDocument(lists, "a")?.permission)

        // 用户把权限改成 tenant 且接口返回 data>0 -> ViewModel 同步本地缓存
        DocumentListUtils.updatePermission(lists, "a", "tenant")

        // 再次点击"修改权限"：对话框应回显 tenant，而不是旧的 private
        assertEquals("tenant", DocumentListUtils.findDocument(lists, "a")?.permission)
    }
}
