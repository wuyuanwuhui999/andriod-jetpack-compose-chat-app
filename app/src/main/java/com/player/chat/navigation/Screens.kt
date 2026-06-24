package com.player.chat.navigation

sealed class Screens(val route: String) {
    object Launch : Screens("launch")
    object Login : Screens("login")
    object Chat : Screens("chat")
    object User : Screens("user")
    object TenantManage : Screens("tenant_manage")
    object UpdatePassword : Screens("update_password")
    object ForgetPassword : Screens("forget_password")
    object ResetPassword : Screens("reset_password")
    object Register : Screens("register")
    object Company : Screens("company")
    object UserManage : Screens("user_manage")
    object AddUser : Screens("add_user")
    object AddTenantUser : Screens("add_tenant_user") 
}