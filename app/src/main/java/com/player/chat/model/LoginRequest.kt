package com.player.chat.model

data class AccountLoginRequest(
    val userAccount: String,
    val password: String
)

data class EmailLoginRequest(
    val email: String,
    val code: String
)

data class SendEmailRequest(
    val email: String
)