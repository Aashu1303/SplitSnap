package com.splitsnap.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

@Serializable
data object HomeRoute

@Serializable
data object CameraRoute

@Serializable
data class EditReceiptRoute(val receiptId: String)

@Serializable
data class SplitSummaryRoute(val receiptId: String)
