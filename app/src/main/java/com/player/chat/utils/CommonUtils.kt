package com.player.chat.utils

import java.math.BigInteger
import java.security.MessageDigest

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
}