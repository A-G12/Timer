package com.timer.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.timer.app.presentation.screen.apps.AppsScreen
import com.timer.app.presentation.screen.coupons.CouponsScreen
import com.timer.app.presentation.screen.home.HomeScreen
import com.timer.app.presentation.screen.settings.SettingsScreen
import com.timer.app.presentation.screen.synergy.SynergyScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val items = listOf(
        Triple(Screen.Home, "Inicio", Icons.Filled.Home),
        Triple(Screen.Apps, "Apps", Icons.Filled.Apps),
        Triple(Screen.Synergy, "Sinergia", Icons.Filled.Bolt),
        Triple(Screen.Coupons, "Cupones", Icons.Filled.ConfirmationNumber),
        Triple(Screen.Settings, "Ajustes", Icons.Filled.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDest = navBackStackEntry?.destination
                items.forEach { (screen, label, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentDest?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Apps.route) { AppsScreen() }
            composable(Screen.Synergy.route) { SynergyScreen() }
            composable(Screen.Coupons.route) { CouponsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
