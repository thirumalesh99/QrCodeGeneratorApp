package tirumalesh.app.qrcodegeneratorapp.qrcodefunctions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tirumalesh.app.qrcodegeneratorapp.R
import tirumalesh.app.qrcodegeneratorapp.qrcodedatabase.QrCodeDatabase
import tirumalesh.app.qrcodegeneratorapp.qrcodedatabase.QrCodeEntity


@Composable
fun QrCodeSavedItem(
    qrCode: QrCodeEntity,
    onShareClick: (Bitmap) -> Unit,
    onCopyClick: (String) -> Unit,
    onOpenLinkClick: (String) -> Unit
) {
    val context = LocalContext.current
    val bitmap: Bitmap? = remember(qrCode.image) {
        BitmapFactory.decodeByteArray(qrCode.image, 0, qrCode.image.size)
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp), // More rounded corners
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant, // Slightly different surface color
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // QR Code Image
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Saved QR Code",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp)) // Rounded corners for image
                        .background(MaterialTheme.colorScheme.surface) // Background for image
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        ), // Subtle border
                    contentScale = ContentScale.Fit
                )
            } else {
                // Placeholder if bitmap fails to load
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // QR Code Details and Actions
            Column(
                modifier = Modifier.weight(1f) // Take remaining space
            ) {
                Text(
                    text = "Content:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = qrCode.content,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2, // Limit lines to prevent overflow
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Type: ${qrCode.type}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share Button
                    FilledTonalIconButton(
                        onClick = { bitmap?.let { onShareClick(it) } }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share QR Code")
                    }

                    // Copy / Open Link Button
                    if (qrCode.content.isValidUrl()) {
                        FilledTonalIconButton(
                            onClick = { onOpenLinkClick(qrCode.content) }
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = "Open Link")
                        }
                    } else {
                        FilledTonalIconButton(
                            onClick = { onCopyClick(qrCode.content) }
                        ) {
                            Icon(painter = painterResource(id = R.drawable.copy), contentDescription = "Copy Content", modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}


/**
 * Redesigned Composable for displaying saved QR Codes.
 * Fetches QR codes from the database and displays them in a scrollable list.
 * @param navController NavController for navigation actions.
 */
@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar
@Composable
fun SavedQrCodesScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Use mutableStateListOf for observable list
    val qrCodeList = remember { mutableStateListOf<QrCodeEntity>() }

    // Fetch data when the composable enters composition
    LaunchedEffect(Unit) {
        val db = QrCodeDatabase.getDatabase(context)
        val data = withContext(Dispatchers.IO) {
            db.qrCodeDao().getAllQrCodes()
        }
        qrCodeList.clear() // Clear existing data to avoid duplicates on recomposition
        qrCodeList.addAll(data)
    }

    // Define custom colors for the top section background and main content background
    // Ideally, these should be part of your MaterialTheme.colorScheme in ui.theme.Theme.kt
    val color1 = Color(0xFF3498db) // Example: A shade of blue
    val color2 = Color(0xFFecf0f1) // Example: A light grey/off-white

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Saved QR Codes",
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
                text = "List of QR Codes you have saved",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            )

            // Main content area with list or empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    .padding(
                        top = 24.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ), // Increased padding
                horizontalAlignment = Alignment.CenterHorizontally // Center content if list is empty
            ) {

                if (qrCodeList.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp), // Padding around the list items
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing between items
                    ) {
                        itemsIndexed(qrCodeList) { index, qrCode ->
                            QrCodeSavedItem(
                                qrCode = qrCode,
                                onShareClick = { bitmapToShare ->
                                    shareQRCode(context, bitmapToShare)
                                },
                                onCopyClick = { contentToCopy ->
                                    val clipboardManager =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData =
                                        ClipData.newPlainText("QR Code Content", contentToCopy)
                                    clipboardManager.setPrimaryClip(clipData)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Content copied to clipboard!")
                                    }
                                },
                                onOpenLinkClick = { url ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Could not open link: ${e.message}")
                                        }
                                    }
                                }
                            )
                        }
                    }
                } else {
                    // Empty state message
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info, // A suitable icon for empty state
                            contentDescription = "No QR Codes",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No QR Codes Saved Yet!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Generate and save QR codes to see them here.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        }
    }
}

