package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Home : Screen("home", "首页", { Icon(Icons.Filled.Home, contentDescription = "Home") })
    object Tags : Screen("tags", "标签", { Icon(Icons.Filled.List, contentDescription = "Tags") })
    object Favorites : Screen("favorites", "收藏", { Icon(Icons.Filled.Favorite, contentDescription = "Favorites") })
    object Settings : Screen("settings", "设置", { Icon(Icons.Filled.Settings, contentDescription = "Settings") })
    object Edit : Screen("edit/{itemId}", "编辑", { })
}

@Composable
fun MainScreen(
    viewModel: ClipboardViewModel = viewModel()
) {
    val navController = rememberNavController()
    
    val items = listOf(
        Screen.Home,
        Screen.Tags,
        Screen.Favorites,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val isBottomBarVisible = items.any { it.route == currentDestination?.route }
            
            if (isBottomBarVisible) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = screen.icon,
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel, navController)
            }
            composable(Screen.Tags.route) {
                TagsScreen(viewModel, navController)
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(viewModel, navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel)
            }
            composable(Screen.Edit.route) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull()
                EditScreen(viewModel, itemId, navController)
            }
        }
    }
}
