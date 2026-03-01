package com.splitsnap.ui.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.splitsnap.ui.screens.editreceipt.EditReceiptScreen
import com.splitsnap.viewmodel.EditReceiptViewModel
import com.splitsnap.viewmodel.EditReceiptViewModelImpl

fun NavGraphBuilder.editReceiptNavigation(navController: NavHostController) {
    composable<EditReceiptRoute> {
        val viewModel: EditReceiptViewModel = hiltViewModel<EditReceiptViewModelImpl>()
        EditReceiptScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToSummary = { id ->
                navController.navigate(SplitSummaryRoute(id))
            }
        )
    }
}