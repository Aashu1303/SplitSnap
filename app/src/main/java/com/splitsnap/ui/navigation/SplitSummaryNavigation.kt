package com.splitsnap.ui.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.splitsnap.ui.screens.splitsummary.SplitSummaryScreen
import com.splitsnap.viewmodel.SplitSummaryViewModel
import com.splitsnap.viewmodel.SplitSummaryViewModelImpl

fun NavGraphBuilder.splitSummaryNavigation(navController: NavHostController) {
    composable<SplitSummaryRoute> {
        val viewModel: SplitSummaryViewModel = hiltViewModel<SplitSummaryViewModelImpl>()
        SplitSummaryScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateHome = {
                navController.navigate(HomeRoute) {
                    popUpTo(HomeRoute) { inclusive = true }
                }
            }
        )
    }
}