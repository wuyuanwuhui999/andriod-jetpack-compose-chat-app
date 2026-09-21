package com.player.chat

import com.google.gson.Gson
import com.player.chat.model.Document
import com.player.chat.utils.DocumentListUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Document 的 Gson 空值安全回归测试
 *
 * 背景（用户实际报错）：点击"修改权限 → 确定"后抛出
 * "Parameter specified as non-null is null: method com.player.chat..."，
 * 该异常被 ViewModel 的 try/catch 捕获后当成提示语弹出。
 *
 * 成因：后端 getDocListByDirId 的 JSON 若缺少 Document 的某个非空字段（如 directoryId），
 * Gson 通过 Unsafe 分配对象、绕过构造函数，于是非空类型的字段里实际是 null；
 * 此时调用 Kotlin 数据类的 copy() 会对**所有**非空参数做 null 校验并抛异常。
 * 因此本地同步一律不使用 copy()，改为直接改可变字段（见 DocumentListUtils）。
 */
class DocumentGsonNullSafetyTest {

    /** 只返回部分字段的 JSON，模拟后端未下发 directoryId 等字段 */
    private fun partialDoc(): Document =
        Gson().fromJson("""{"id":"d1","name":"n.pdf","ext":"pdf"}""", Document::class.java)

    /** 现象确认：非空类型的字段在运行时可能是 null（这就是 copy() 崩溃的根源） */
    @Test
    fun gsonPartialJson_leavesRuntimeNullInNonNullTypedField() {
        val doc = partialDoc()
        // 字段声明是 String（非空），但运行时为 null
        val directoryId: String? = doc.directoryId
        assertNull("后端未下发 directoryId 时，非空类型字段实际为 null", directoryId)
    }

    /** 说明性用例：数据类 copy() 在这种文档上不安全（模型若改为可空字段，本用例可删） */
    @Test
    fun dataClassCopy_isUnsafeOnGsonPartialDoc() {
        val failure = runCatching { partialDoc().copy(permission = "tenant") }.exceptionOrNull()
        if (failure != null) {
            assertTrue("copy() 应抛 NPE，实际: $failure", failure is NullPointerException)
            assertTrue(
                "应命中 Kotlin 非空参数校验: ${failure.message}",
                failure.message.orEmpty().contains("Parameter specified as non-null")
            )
        }
    }

    /** 回归：本地同步不使用 copy()，因此在"字段为 null"的文档上也不会崩，并且权限被正确写入 */
    @Test
    fun updatePermission_isSafeOnGsonPartialDoc() {
        val doc = partialDoc()
        val lists = mapOf("d1" to listOf(doc))

        // 修复前这里调用 doc.copy(permission = ...) -> 抛 "Parameter specified as non-null is null"
        DocumentListUtils.updatePermission(lists, "d1", "tenant")

        assertEquals("tenant", doc.permission)
        assertEquals("tenant", DocumentListUtils.findDocument(lists, "d1")?.permission)
    }

    /** 删除同理：不使用 copy()，字段为 null 的文档也能从列表中移除 */
    @Test
    fun removeDocument_isSafeOnGsonPartialDoc() {
        val lists = mapOf("d1" to listOf(partialDoc()))
        val updated = DocumentListUtils.removeDocument(lists, "d1")
        assertTrue(updated.getValue("d1").isEmpty())
    }
}
