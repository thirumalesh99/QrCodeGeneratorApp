package tirumalesh.app.qrcodegeneratorapp.qrcodefunctions

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tirumalesh.app.qrcodegeneratorapp.qrcodedatabase.QrCodeDatabase
import tirumalesh.app.qrcodegeneratorapp.qrcodedatabase.QrCodeEntity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import android.graphics.Color as AndroidColor

/**
 * Generates a QR Code Bitmap from the given content.
 * @param content The string content to encode into the QR code.
 * @return A Bitmap representing the QR code, or null if generation fails.
 */
fun generateQRCode(content: String): Bitmap? {
    return try {
        val writer = MultiFormatWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Shares a generated QR Code Bitmap via an Intent.
 * @param context The current Android context.
 * @param bitmap The Bitmap of the QR code to share.
 */
fun shareQRCode(context: Context, bitmap: Bitmap) {
    val cachePath = File(context.cacheDir, "images")
    cachePath.mkdirs()
    val file = File(cachePath, "qr_image.png")
    val stream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    stream.close()

    val qrUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider", // Make sure this matches your provider authority in AndroidManifest.xml
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, qrUri)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    context.startActivity(Intent.createChooser(intent, "Share QR Code"))
}

@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar and OutlinedTextField
@Composable
fun GenerateQRCodeScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var content by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // New: Create a ScrollState for the inner Column
    val scrollState = rememberScrollState()

    // Define custom colors for the top section background and main content background
    // Ideally, these should be part of your MaterialTheme.colorScheme in ui.theme.Theme.kt
    val color1 = Color(0xFF3498db) // Example: A shade of blue
    val color2 = Color(0xFFecf0f1) // Example: A light grey/off-white
    val pureWhite = Color.White
    val skyBlue = Color(0xFF87CEEB) // A light blue color

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Snackbar host for messages
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Generate QR Code",
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
                    containerColor = color1, // Use custom color for top app bar
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply Scaffold's padding
                .background(MaterialTheme.colorScheme.background) // Use theme background
        ) {
            // Subtitle for the screen
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                text = "Start generating QR Codes",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            )

            // Main content area with inputs and QR display
            Column(
                modifier = Modifier
                    .fillMaxWidth() // Keep fillMaxWidth
                    .verticalScroll(scrollState) // <-- Apply verticalScroll here
                    .background(
                        color = color2, // Use custom color for the main content background
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .border(
                        width = 1.dp,
                        color = color2, // Border color matching background for subtle separation
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(24.dp), // Increased padding for content
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                // Content Input Field
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Enter Content") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp), // Rounded corners for text field
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = pureWhite,
                        unfocusedContainerColor = pureWhite,
                        disabledContainerColor = pureWhite,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // QR Code Type Input Field
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("QR Code Type (e.g., URL, Text, WiFi)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(12.dp), // Rounded corners for text field
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = pureWhite,
                        unfocusedContainerColor = pureWhite,
                        disabledContainerColor = pureWhite,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Generate QR Button
                ElevatedButton(
                    onClick = {
                        if (content.isNotBlank()) {
                            val qrContent = "Content: $content\nType: ${type.ifBlank { "N/A" }}"
                            qrBitmap = generateQRCode(qrContent)
                            if (qrBitmap == null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Failed to generate QR Code. Please try again.")
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please enter content to generate QR Code.")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp), // Rounded corners
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Use primary color
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp)
                ) {
                    Text(text = "Generate QR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Display Generated QR Code and Action Buttons
                qrBitmap?.let { bitmap ->
                    ElevatedCard(
                        modifier = Modifier
                            .size(250.dp) // Larger size for QR code display
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Generated QR Code",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp) // Padding inside the card
                                .clip(RoundedCornerShape(8.dp)), // Slightly rounded image itself
                            alignment = Alignment.Center
                        )
                    }

                    // Share QR Code Button
                    FilledTonalButton(
                        onClick = {
                            shareQRCode(context, bitmap)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Share QR Code", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Save QR Code Button
                    Button(
                        onClick = {
                            if (isSaving) return@Button // Prevent multiple clicks
                            isSaving = true
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val outputStream = ByteArrayOutputStream()
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                                    val imageBytes = outputStream.toByteArray()

                                    val db = QrCodeDatabase.getDatabase(context)
                                    db.qrCodeDao().insertQrCode(
                                        QrCodeEntity(
                                            content = content,
                                            type = type,
                                            image = imageBytes
                                        )
                                    )
                                    launch(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("QR Code Saved Successfully!")
                                        // Optionally navigate back or clear fields after saving
                                        // navController.popBackStack()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    launch(Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Failed to save QR Code: ${e.message}")
                                    }
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 8.dp),
                        enabled = !isSaving, // Disable button while saving
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = skyBlue, // Use a custom SkyBlue
                            contentColor = pureWhite
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = pureWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Save QR Code", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// Preview for the Redesigned Generate QR Code Screen
@Preview(showBackground = true, widthDp = 360)
@Composable
fun GenerateQRCodeScreenPreview() {
    // For preview, pass a dummy NavController
    GenerateQRCodeScreen(navController = rememberNavController())
}