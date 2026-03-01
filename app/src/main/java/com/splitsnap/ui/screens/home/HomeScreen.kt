package com.splitsnap.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitsnap.R
import com.splitsnap.domain.model.Receipt
import com.splitsnap.ui.components.ReceiptCard
import com.splitsnap.ui.theme.Primary
import com.splitsnap.ui.theme.PrimaryContainer
import com.splitsnap.ui.theme.SecondaryContainer
import com.splitsnap.ui.util.FontSize
import com.splitsnap.ui.util.Spacing
import com.splitsnap.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = Spacing.Spacing16),
                title = {
                    HomeScreenTitle()
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = colorScheme.background
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::onNavigateToCamera,
                containerColor = Primary,
                contentColor = Color.White,
                icon = {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null
                    )
                },
                text = { },
                expanded = false
            )
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = Primary)
            } else if (state.receipts.isEmpty()) {
                PlaceHolderComponent(viewModel::onNavigateToCamera)
            } else {
                HomeScreenComponent(
                    receiptsList = state.receipts,
                    onNavigateToReceipt = viewModel::onNavigateToReceipt,
                )
            }
        }
    }
}

@Composable
private fun HomeScreenTitle() {
    Column {
        Text(
            text = stringResource(id = R.string.home_title),
            fontSize = FontSize.Size30
        )
        Spacer(modifier = Modifier.height(Spacing.Spacing8))
        Text(
            text = stringResource(id = R.string.home_subtitle),
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlaceHolderComponent(
    onNavigateToCamera: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(Spacing.Spacing32)
    ) {
        Box(
            modifier = Modifier
                .size(Spacing.Spacing120)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryContainer, SecondaryContainer)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(Spacing.Spacing56),
                tint = Primary
            )
        }

        Spacer(modifier = Modifier.height(Spacing.Spacing24))

        Text(
            text = stringResource(id = R.string.home_empty_title),
            style = typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(Spacing.Spacing8))

        Text(
            text = stringResource(id = R.string.home_empty_body),
            style = typography.bodyLarge,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.Spacing32))

        Button(
            onClick = onNavigateToCamera,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(Spacing.Spacing16),
            contentPadding = PaddingValues(
                horizontal = Spacing.Spacing24,
                vertical = Spacing.Spacing16
            )
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(Spacing.Spacing8))
            Text(
                text = stringResource(id = R.string.home_scan_receipt),
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HomeScreenComponent(
    receiptsList: List<Receipt>,
    onNavigateToReceipt: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(Spacing.Spacing16),
        verticalArrangement = Arrangement.spacedBy(Spacing.Spacing12)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.home_recent_receipts),
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Spacing.Spacing8)
            )
        }

        items(receiptsList, key = { it.id }) { receipt ->
            ReceiptCard(
                receipt = receipt,
                onClick = { onNavigateToReceipt(receipt.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.Spacing80))
        }
    }
}