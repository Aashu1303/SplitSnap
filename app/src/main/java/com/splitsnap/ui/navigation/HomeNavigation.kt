package com.splitsnap.ui.navigation

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.splitsnap.ui.screens.home.HomeScreen
import com.splitsnap.viewmodel.HomeViewModel
import com.splitsnap.viewmodel.HomeViewModelImpl

sealed interface HomeNavigationAction {
    data object NavigateToCamera : HomeNavigationAction
    data class NavigateToReceipt(val receiptId: String) : HomeNavigationAction
}

class HomeNavigator(private val navController: NavController) {

    fun navigateToCamera() {
        navController.navigate(CameraRoute)
    }

    fun navigateToReceipt(receiptId: String) {
        navController.navigate(EditReceiptRoute(receiptId))
    }
}

fun NavGraphBuilder.homeNavigation(navController: NavHostController) {
    composable<HomeRoute> {
        val viewModel: HomeViewModel = hiltViewModel<HomeViewModelImpl>()
        val navigator = remember { HomeNavigator(navController) }

        viewModel.navigationAction.navigateOnLifeCycleResumed { action ->
            when (action) {
                is HomeNavigationAction.NavigateToCamera -> navigator.navigateToCamera()
                is HomeNavigationAction.NavigateToReceipt -> navigator.navigateToReceipt(action.receiptId)
            }
        }
        HomeScreen(viewModel)
    }
}