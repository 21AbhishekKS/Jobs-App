package com.abhi.jobsapp.navigation

sealed class Routes(val route: String) {
    object BottomNav : Routes("BottomNav")
    object Jobs : Routes("Jobs")
    object Bookmarks : Routes("Bookmarks")
    object JobDetail {
            const val route = "jobDetail/{jobId}"
            fun createRoute(jobId: Int): String = "jobDetail/$jobId"
        }


}


