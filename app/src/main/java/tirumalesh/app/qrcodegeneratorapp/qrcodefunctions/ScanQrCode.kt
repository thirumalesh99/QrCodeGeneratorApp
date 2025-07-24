package tirumalesh.app.qrcodegeneratorapp.qrcodefunctions

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Size
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview.Builder
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import tirumalesh.app.qrcodegeneratorapp.R
import androidx.compose.ui.geometry.Size as ComposeSize


// Function to check if a string is a valid URL
fun String.isValidUrl(): Boolean {
    return try {
        Uri.parse(this).host != null
    } catch (e: Exception) {
        false
    }
}

/**
 * Redesigned Composable for scanning QR Codes.
 * Handles camera permission, displays camera preview, and processes QR code scans.
 * @param navController NavController for navigation actions.
 */
@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar and ModalBottomSheet
@Composable
fun QRScannerScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val scannerResult =
        remember { mutableStateOf<String?>(null) } // Nullable to indicate no scan yet
    val showResultSheet =
        remember { mutableStateOf(false) } // State to control bottom sheet visibility

    val hasCameraPermission = remember { mutableStateOf(false) }
    val cameraInitialized = remember { mutableStateOf(false) } // To show loading indicator

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission.value = isGranted
        if (!isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Camera permission is required to scan QR codes.",
                    actionLabel = "Grant",
                    withDismissAction = true,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    // Request permission on launch
    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                hasCameraPermission.value = true
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan QR Code",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                text = "Point your camera at a QR code to scan",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            )

            if (hasCameraPermission.value) {
                // Camera preview area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Take up remaining vertical space
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp)) // Rounded corners for camera preview
                        .background(Color.Black), // Black background behind preview
                    contentAlignment = Alignment.Center
                ) {
                    val previewView = remember {
                        PreviewView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    }

                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Scanning Overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val overlayColor = Color.Black.copy(alpha = 0.6f)
                        val cutoutSize =
                            ComposeSize(size.width * 0.7f, size.width * 0.7f) // Square cutout
                        val cutoutOffset = Offset(
                            (size.width - cutoutSize.width) / 2f,
                            (size.height - cutoutSize.height) / 2f
                        )

                        // Draw the dark overlay
                        drawRect(color = overlayColor)

                        // Clear the cutout area to show camera preview
                        drawRect(
                            color = Color.Transparent,
                            topLeft = cutoutOffset,
                            size = cutoutSize,
                            blendMode = BlendMode.Clear // This is key to making it transparent
                        )

                        // Draw a border around the cutout
                        drawRect(
                            color = Color.White.copy(alpha = 0.8f),
                            topLeft = cutoutOffset,
                            size = cutoutSize,
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }

                    // --- ALTERNATIVE LOADING INDICATOR ---
                    val animatedAlpha by animateFloatAsState(
                        targetValue = if (!cameraInitialized.value) 1f else 0f,
                        animationSpec = tween(durationMillis = 300),
                        label = "loading_alpha_animation"
                    )

                    if (!cameraInitialized.value || animatedAlpha > 0f) { // Render if loading or fading out
                        CircularProgressIndicator(
                            modifier = Modifier.alpha(animatedAlpha), // Apply animated alpha
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    // --- END ALTERNATIVE LOADING INDICATOR ---


                    // Camera binding logic
                    DisposableEffect(lifecycleOwner, previewView) {
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val analyzer = ImageAnalysis.Builder()
                            .setTargetResolution(Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                                    processImageProxy(imageProxy, scannerResult, showResultSheet)
                                }
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            // Unbind all use cases before rebinding
                            cameraProvider.unbindAll()
                            // Bind use cases to camera
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                analyzer
                            )
                            cameraInitialized.value = true
                        } catch (exc: Exception) {
                            println("Use case binding failed: ${exc.message}")
                            cameraInitialized.value = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Failed to start camera. Please try again.")
                            }
                        }

                        onDispose {
                            cameraProvider.unbindAll()
                            cameraInitialized.value = false
                        }
                    }
                }
            } else {
                // Permission denied or not yet granted UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Camera permission is essential for QR code scanning.",
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Camera Permission")
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet to display scanned result
    if (showResultSheet.value && scannerResult.value != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showResultSheet.value = false
                scannerResult.value = null // Clear result when dismissed
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = Modifier.fillMaxHeight(0.5f) // Adjust height as needed
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Scanned QR Code Content:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                SelectionContainer { // Allows text selection
                    Text(
                        text = scannerResult.value ?: "No content scanned.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Copy Button
                    Button(
                        onClick = {
                            val clipboardManager =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData =
                                ClipData.newPlainText("QR Code Content", scannerResult.value)
                            clipboardManager.setPrimaryClip(clipData)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Copied to clipboard!")
                            }
                            showResultSheet.value = false // Dismiss after copying
                            scannerResult.value = null // Clear result
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.copy),
                            contentDescription = "Copy",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Copy")
                    }

                    // Open Link Button (if content is a URL)
                    if (scannerResult.value?.isValidUrl() == true) {
                        Button(
                            onClick = {
                                val intent =
                                    Intent(Intent.ACTION_VIEW, Uri.parse(scannerResult.value))
                                context.startActivity(intent)
                                showResultSheet.value = false // Dismiss after opening link
                                scannerResult.value = null // Clear result
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Icon(imageVector = Icons.Filled.Info, contentDescription = "Open Link")
                            Spacer(Modifier.width(8.dp))
                            Text("Open Link")
                        }
                    }
                }

                // Close Button
                TextButton(
                    onClick = {
                        showResultSheet.value = false
                        scannerResult.value = null // Clear result when dismissed
                    }
                ) {
                    Text("Close")
                }
            }
        }
    }
}

/**
 * Processes an ImageProxy from CameraX to detect barcodes.
 * @param imageProxy The ImageProxy containing the camera frame.
 * @param resultState MutableState to update with the scanned QR code content.
 * @param showResultSheet MutableState to control the visibility of the result bottom sheet.
 */
@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    resultState: MutableState<String?>,
    showResultSheet: MutableState<Boolean>
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val scannedData = barcodes[0].rawValue
                    if (scannedData != null && scannedData != resultState.value) { // Only update if new data
                        resultState.value = scannedData
                        showResultSheet.value = true // Show the bottom sheet
                    }
                }
            }
            .addOnFailureListener { e ->
                println("QR Scan failed: ${e.message}")
                // Optionally show a snackbar for scan failure, but avoid spamming
            }
            .addOnCompleteListener {
                imageProxy.close() // Important: Close the image proxy to release resources
            }
    } else {
        imageProxy.close()
    }
}

