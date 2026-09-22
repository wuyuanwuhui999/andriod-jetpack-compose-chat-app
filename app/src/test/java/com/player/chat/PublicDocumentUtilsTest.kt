package com.player.chat

import com.google.gson.Gson
import com.player.chat.model.Document
import com.player.chat.utils.PublicDocumentUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 公共文档"按 directoryName 分组"的行为测试
 *
 * 需求：getPublicDocList 一次返回全部公共文档，文档自带 directoryName（文档目录名称），
 * 界面按 directoryName 分组展示目录卡片，点击展开直接显示该目录下的文档（不再请求接口）。
 */
class PublicDocumentUtilsTest {

    private fun doc(id: String, directoryName: String?, name: String = "doc-$id.pdf") = Document(
        id = id, name = name, ext = "pdf", userId = "u1",
        createTime = "2026-01-01 00:00:00", updateTime = "2026-01-01 00:00:00",
        directoryId = "dir-$directoryName", directoryName = directoryName!!, permission = "tenant"
    )

    /** 同名目录合并到一组，顺序按文档首次出现顺序（而不是分组后的字母序） */
    @Test
    fun groupByDirectoryName_groupsSameNameAndKeepsFirstSeenOrder() {
        val docs = listOf(
            doc("a", "目录B"),
            doc("b", "目录A"),
            doc("c", "目录B")
        )

        val groups = PublicDocumentUtils.groupByDirectoryName(docs)

        assertEquals(listOf("目录B", "目录A"), groups.keys.toList())
        assertEquals(listOf("a", "c"), groups.getValue("目录B").map { it.id })
        assertEquals(listOf("b"), groups.getValue("目录A").map { it.id })
    }

    /** 空列表不抛异常，也不产生分组 */
    @Test
    fun groupByDirectoryName_emptyList() {
        assertTrue(PublicDocumentUtils.groupByDirectoryName(emptyList()).isEmpty())
    }

    /** 空字符串目录名归到"未分类"，避免界面上出现空标题的目录卡片 */
    @Test
    fun groupByDirectoryName_blankNameFallsBackToUngrouped() {
        val docs = listOf(doc("a", ""), doc("b", "  "))

        val groups = PublicDocumentUtils.groupByDirectoryName(docs)

        assertEquals(setOf(PublicDocumentUtils.UNGROUPED_DIRECTORY), groups.keys)
        assertEquals(listOf("a", "b"), groups.getValue(PublicDocumentUtils.UNGROUPED_DIRECTORY).map { it.id })
    }

    /**
     * 后端未下发 directoryName 时（Gson 会把 null 写进非空类型的字段，见 DocumentGsonNullSafetyTest），
     * 分组逻辑不能崩，并且要有兜底分组名
     */
    @Test
    fun groupByDirectoryName_gsonNullNameIsSafe() {
        val missingName = Gson().fromJson(
            """{"id":"x1","name":"n.pdf","ext":"pdf","directoryName":null}""",
            Document::class.java
        )
        val normal = doc("x2", "公共目录")

        val groups = PublicDocumentUtils.groupByDirectoryName(listOf(missingName, normal))

        assertEquals(listOf(PublicDocumentUtils.UNGROUPED_DIRECTORY, "公共目录"), groups.keys.toList())
        assertEquals(listOf("x1"), groups.getValue(PublicDocumentUtils.UNGROUPED_DIRECTORY).map { it.id })
    }

    /** 分组不修改原列表、不改动文档对象（纯函数） */
    @Test
    fun groupByDirectoryName_doesNotMutateInput() {
        val docs = listOf(doc("a", "目录A"), doc("b", "目录B"))
        val snapshot = docs.map { it.id to it.directoryName }

        PublicDocumentUtils.groupByDirectoryName(docs)

        assertEquals(snapshot, docs.map { it.id to it.directoryName })
    }
}
