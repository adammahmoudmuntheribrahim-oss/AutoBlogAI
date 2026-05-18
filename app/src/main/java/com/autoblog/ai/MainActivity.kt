package com.autoblog.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.autoblog.ai.ui.screens.DashboardScreen
import com.autoblog.ai.ui.screens.SettingsScreen
import com.autoblog.ai.ui.theme.AutoBlogAITheme
import com.autoblog.ai.viewmodel.DashboardViewModel
import com.autoblog.ai.workers.Scheduler
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Scheduler.start(this)

        setContent {
            AutoBlogAITheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("الرئيسية") },
                    selected = currentDestination?.route == "dashboard",
                    onClick = { navController.navigate("dashboard") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("الإعدادات") },
                    selected = currentDestination?.route == "settings",
                    onClick = { navController.navigate("settings") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                val viewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(viewModel)
            }
            composable("settings") {
                val viewModel: DashboardViewModel = hiltViewModel()
                SettingsScreen(viewModel)
            }
        }
    }
}
