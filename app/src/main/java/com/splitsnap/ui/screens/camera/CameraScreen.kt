package com.splitsnap.ui.screens.camera

import android.Manifest
import android.content.Context
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.splitsnap.ui.theme.*
import com.splitsnap.viewmodel.CameraViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReceipt: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(uiState.createdReceiptId) {
        uiState.createdReceiptId?.let { receiptId ->
            viewModel.clearCreatedReceipt()
            onNavigateToReceipt(receiptId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Receipt") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !cameraPermissionState.status.isGranted -> {
                    PermissionRequest(
                        shouldShowRationale = cameraPermissionState.status.shouldShowRationale,
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                        onUseMockData = { viewModel.captureWithMockData() }
                    )
                }
                uiState.isCapturing -> {
                    CapturingState()
                }
                uiState.isProcessing -> {
                    ProcessingState(processingStep = uiState.processingStep)
                }
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = { viewModel.retryCapture() },
                        onUseMockData = { viewModel.captureWithMockData() }
                    )
                }
                else -> {
                    CameraPreviewContent(
                        context = context,
                        viewModel = viewModel,
                        onCapture = { viewModel.captureReceipt() },
                        onUseMockData = { viewModel.captureWithMockData() }
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreviewContent(
    context: Context,
    viewModel: CameraViewModel,
    onCapture: () -> Unit,
    onUseMockData: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(previewView) {
        previewView?.let { view ->
            viewModel.initializeCamera(context, lifecycleOwner, view)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1E293B))
            ) {
                if (uiState.isCameraInitialized) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                previewView = this
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                    )
                } else {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                previewView = this
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                    )
                    
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                CameraOverlay()
            }
        }

        CaptureControls(
            isCameraReady = uiState.isCameraInitialized,
            onCapture = onCapture,
            onUseMockData = onUseMockData
        )
    }
}

@Composable
private fun CameraOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.7f)
                .align(Alignment.Center)
                .border(
                    width = 2.dp,
                    color = Primary.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        val cornerSize = 24.dp
        val cornerStroke = 3.dp

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 28.dp, top = 60.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(cornerSize, cornerStroke)
                    .background(Primary)
            )
            Box(
                modifier = Modifier
                    .size(cornerStroke, cornerSize)
                    .background(Primary)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 28.dp, top = 60.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(cornerSize, cornerStroke)
                    .align(Alignment.TopEnd)
                    .background(Primary)
            )
            Box(
                modifier = Modifier
                    .size(cornerStroke, cornerSize)
                    .align(Alignment.TopEnd)
                    .background(Primary)
            )
        }

        Text(
            text = "Position receipt within frame",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun CaptureControls(
    isCameraReady: Boolean,
    onCapture: () -> Unit,
    onUseMockData: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 32.dp)
    ) {
        Button(
            onClick = onCapture,
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                disabledContainerColor = Primary.copy(alpha = 0.5f)
            ),
            enabled = isCameraReady,
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capture",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onUseMockData) {
            Text(
                text = "Use demo receipt instead",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionRequest(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
    onUseMockData: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (shouldShowRationale) {
                    "Camera access is needed to scan receipts. Please grant permission to continue."
                } else {
                    "We need camera access to scan and process your receipts."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Grant Permission",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onUseMockData) {
                Text("Use demo receipt instead")
            }
        }
    }
}

@Composable
private fun CapturingState() {
    val infiniteTransition = rememberInfiniteTransition(label = "capture")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Capturing...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProcessingState(processingStep: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Primary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(80.dp)
                )
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Processing Receipt",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = processingStep.ifEmpty { "Extracting items and prices..." },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    onUseMockData: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Try Again")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onUseMockData) {
                Text("Use demo receipt instead")
            }
        }
    }
}
