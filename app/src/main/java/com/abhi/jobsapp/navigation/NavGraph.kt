package com.abhi.jobsapp.navigation

import JobDetailScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.abhi.jobsapp.screens.Bookmarks
import com.abhi.jobsapp.screens.Jobs

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navHostController,
        startDestination = Routes.BottomNav.route,
        modifier = modifier
    ) {
        composable(Routes.BottomNav.route) {
            BottomNav(navHostController, modifier)
        }
        composable(Routes.Jobs.route) {
            Jobs(navHostController )
        }
        composable(Routes.Bookmarks.route) {
            Bookmarks()
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