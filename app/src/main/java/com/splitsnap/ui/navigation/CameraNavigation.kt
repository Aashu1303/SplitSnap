package com.splitsnap.ui.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.splitsnap.ui.screens.camera.CameraScreen
import com.splitsnap.viewmodel.CameraViewModel
import com.splitsnap.viewmodel.CameraViewModelImpl

fun NavGraphBuilder.cameraNavigation(navController: NavHostController) {
    composable<CameraRoute> {
        val viewModel: CameraViewModel = hiltViewModel<CameraViewModelImpl>()
        CameraScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToReceipt = { receiptId ->
                navController.navigate(EditReceiptRoute(receiptId)) {
                    popUpTo(HomeRoute)
                }
            }
        )
    }
}