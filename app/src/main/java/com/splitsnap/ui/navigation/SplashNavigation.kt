package com.splitsnap.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.splitsnap.ui.screens.splash.SplashScreen

fun NavGraphBuilder.splashNavigation(navController: NavHostController) {
    composable<SplashRoute> {
        SplashScreen(
            onSplashComplete = {
                navController.navigate(HomeRoute) {
                    popUpTo(SplashRoute) { inclusive = true }
                }
            }
        )
    }
}