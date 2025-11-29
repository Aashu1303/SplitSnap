package com.splitsnap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.splitsnap.SplitSnapApplication
import com.splitsnap.ui.screens.camera.CameraScreen
import com.splitsnap.ui.screens.editreceipt.EditReceiptScreen
import com.splitsnap.ui.screens.home.HomeScreen
import com.splitsnap.ui.screens.splash.SplashScreen
import com.splitsnap.ui.screens.splitsummary.SplitSummaryScreen
import com.splitsnap.viewmodel.CameraViewModel
import com.splitsnap.viewmodel.EditReceiptViewModel
import com.splitsnap.viewmodel.HomeViewModel
import com.splitsnap.viewmodel.SplitSummaryViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Camera : Screen("camera")
    object EditReceipt : Screen("edit_receipt/{receiptId}") {
        fun createRoute(receiptId: String) = "edit_receipt/$receiptId"
    }
    object SplitSummary : Screen("split_summary/{receiptId}") {
        fun createRoute(receiptId: String) = "split_summary/$receiptId"
    }
}

@Composable
fun SplitSnapNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    val context = LocalContext.current
    val application = context.applicationContext as SplitSnapApplication
    val repository = application.repository

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(repository)
            )
            HomeScreen(
                viewModel = viewModel,
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route)
                },
                onNavigateToReceipt = { receiptId ->
                    navController.navigate(Screen.EditReceipt.createRoute(receiptId))
                }
            )
        }

        composable(Screen.Camera.route) {
            val viewModel: CameraViewModel = viewModel(
                factory = CameraViewModel.Factory(repository)
            )
            CameraScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToReceipt = { receiptId ->
                    navController.navigate(Screen.EditReceipt.createRoute(receiptId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.EditReceipt.route,
            arguments = listOf(
                navArgument("receiptId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getString("receiptId") ?: return@composable
            val viewModel: EditReceiptViewModel = viewModel(
                factory = EditReceiptViewModel.Factory(receiptId, repository)
            )
            EditReceiptScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSummary = { id ->
                    navController.navigate(Screen.SplitSummary.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.SplitSummary.route,
            arguments = listOf(
                navArgument("receiptId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getString("receiptId") ?: return@composable
            val viewModel: SplitSummaryViewModel = viewModel(
                factory = SplitSummaryViewModel.Factory(receiptId, repository)
            )
            SplitSummaryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
