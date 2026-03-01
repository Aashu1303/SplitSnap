package com.splitsnap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun SplitSnapNavGraph(navController: NavHostController) {
    val startDestinationRoute = SplashRoute

    NavHost(
        navController = navController,
        startDestination = startDestinationRoute,
    ) {
        splashNavigation(navController)
        homeNavigation(navController)
        cameraNavigation(navController)
        editReceiptNavigation(navController)
        splitSummaryNavigation(navController)
    }
}