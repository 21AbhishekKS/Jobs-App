package com.abhi.jobsapp.navigation

import JobDetailScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.abhi.jobsapp.screens.Bookmarks
import com.abhi.jobsapp.screens.Jobs


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNav(
    navHostController: NavHostController,
    padding: Modifier
) {
    val navController1 = rememberNavController()

    Scaffold(
        bottomBar = {
            MyBottomBar(navController1)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController1,
            startDestination = Routes.Jobs.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Jobs.route) {
                Jobs(navController1 , padding)
            }
            composable(Routes.Bookmarks.route) {
                Bookmarks(padding)
            }

            composable(
                route = Routes.JobDetail.route,
                arguments = listOf(navArgument("jobId") { type = NavType.IntType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getInt("jobId") ?: 0
                JobDetailScreen(jobId = jobId , navHostController)
            }

        }
    }
}

@Composable
fun MyBottomBar(navController: NavHostController) {
    val backStackEntry = navController.currentBackStackEntryAsState()

    val items = listOf(
        BottomNavItem(Routes.Jobs.route, "Jobs", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Routes.Bookmarks.route, "Bookmarks", Icons.Filled.Favorite, Icons.Outlined.Favorite)
    )
    Column {
        Divider(color = Color.Gray, thickness = 0.5.dp)
    NavigationBar(
        containerColor = Color.White
    ) {
        items.forEach { item ->
            val selected = item.route == backStackEntry.value?.destination?.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unSelectedIcon,
                        contentDescription = item.title,
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (selected) Color.Black else Color.Black
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFFFFC107),
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color(0xFFFFC107),
                    selectedTextColor = Color(0xFFFFC107),
                    unselectedTextColor = Color.Black
                )
            )
        }
    }
    }
}


data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unSelectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
