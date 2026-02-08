package com.player.chat.utils

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

}