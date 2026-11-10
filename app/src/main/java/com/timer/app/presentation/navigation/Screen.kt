package com.timer.app.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Apps : Screen("apps")
    object Synergy : Screen("synergy")
    object Coupons : Screen("coupons")
    object Settings : Screen("settings")
}
