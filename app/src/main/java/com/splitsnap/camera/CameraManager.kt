package com.splitsnap.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraManager(private val context: Context) {
    
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    companion object {
        private const val TAG = "CameraManager"
    }

    suspend fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setJpegQuality(95)
                    .build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                cameraProvider?.unbindAll()

                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                continuation.resume(true)
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
                continuation.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    suspend fun captureImage(): Bitmap? = suspendCancellableCoroutine { continuation ->
        val imageCapture = imageCapture ?: run {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val tempFile = File.createTempFile("receipt_", ".jpg", context.cacheDir)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                        tempFile.delete()
                        
                        if (bitmap != null) {
                            continuation.resume(bitmap)
                        } else {
                            continuation.resumeWithException(Exception("Failed to decode captured image"))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Image loading failed", e)
                        tempFile.delete()
                        continuation.resumeWithException(e)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Image capture failed", exception)
                    tempFile.delete()
                    continuation.resumeWithException(exception)
                }
            }
        )
    }

    fun shutdown() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }
}
