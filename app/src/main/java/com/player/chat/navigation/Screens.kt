package com.player.chat.navigation

sealed class Screens(val route: String) {
    object Launch : Screens("launch")
    object Login : Screens("login")
    object Chat : Screens("chat")
    object User : Screens("user")
    object TenantManage : Screens("tenant_manage")
}