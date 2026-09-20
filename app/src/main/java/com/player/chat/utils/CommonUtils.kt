package com.player.chat.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object CommonUtils {

    /**
     * 对字符串进行 MD5 加密
     * @param input 要加密的字符串
     * @return 32位小写MD5字符串
     */
    fun md5(input: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(input.toByteArray(Charsets.UTF_8))
            // 转换为十六进制字符串
            BigInteger(1, digest).toString(16).padStart(32, '0')
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 对字符串进行 MD5 加密（大写）
     * @return 32位大写MD5字符串
     */
    fun md5UpperCase(input: String): String {
        return md5(input).uppercase()
    }

    /**
     * 带盐值的 MD5 加密
     */
    fun md5WithSalt(input: String, salt: String): String {
        return md5("$input{$salt}")
    }

    /**
     * 计算时间差并返回相对时间字符串
     * @param targetTime 目标时间字符串，格式："yyyy-MM-dd HH:mm:ss"
     * @return 相对时间描述
     */
    fun formatRelativeTime(targetTime: String): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val targetDate = formatter.parse(targetTime)
            val now = Date()

            if (targetDate == null) {
                return targetTime
            }

            val diffInMillis = now.time - targetDate.time
            val diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis)
            val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
            val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
            val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

            when {
                diffInSeconds < 60 -> "刚刚"
                diffInMinutes < 60 -> "${diffInMinutes}分钟前"
                diffInHours < 24 -> "${diffInHours}小时前"
                diffInDays < 30 -> "${diffInDays}天前"
                diffInDays < 360 -> "${diffInDays / 30}个月前"
                else -> "${diffInDays / 365}年前"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            targetTime
        }
    }

    /**
     * 根据 Uri 获取文件的真实显示名称（用于上传时展示文件信息）
     * @param context 上下文
     * @param uri 文件 Uri
     * @return 文件名，获取失败时退化为 Uri 路径末段
     */
    fun getFileName(context: Context, uri: Uri): String {
        var fileName = ""
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex) ?: ""
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // 部分 ContentProvider 不返回 DISPLAY_NAME，退化为从 Uri 路径截取
        return fileName.ifBlank { uri.lastPathSegment?.substringAfterLast('/') ?: "unknown" }
    }

    /**
     * 根据文件名获取后缀（不含点），如 a.pdf -> pdf
     * @param fileName 文件名
     * @return 小写后缀，无后缀时返回空串
     */
    fun getFileExtension(fileName: String): String =
        fileName.substringAfterLast('.', "").lowercase(Locale.getDefault())

}
