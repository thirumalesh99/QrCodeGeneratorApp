package tirumalesh.app.qrcodegeneratorapp

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import tirumalesh.app.qrcodegeneratorapp.ui.theme.QrCodeGeneratorAppTheme

// --- Dummy implementations for demonstration ---
// In your actual project, these would be your real classes/objects
object SelectedFile {
    var selectedOption: Int = 0
}

// --- End Dummy implementations ---


/**
 * Data class to represent an action item on the home screen.
 * @param title The title of the action.
 * @param icon The icon for the action.
 * @param destinationRoute The route to navigate to when this action is clicked.
 * Use null if it triggers a direct activity launch (less recommended).
 */
data class HomeAction(
    val title: String,
    val icon: Int, // Using ImageVector for Material Icons
    val destinationRoute: String? = null,
    val onClickAction: ((NavController, Context) -> Unit)? = null // Custom action for special cases
)

/**
 * Redesigned Home Screen Composable for the QR Code Generator app.
 * Features a modern UI with Material Design 3 components and improved navigation.
 * @param navController NavController for navigating between screens.
 */
@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    val color1 = Color(0xFF3498db) // Example: A shade of blue
    val color2 = Color(0xFFecf0f1) // Example: A light grey/off-white

    // Define the list of actions for the grid
    val homeActions = remember {
        listOf(
            HomeAction(
                title = "Generate QR Code",
                icon = R.drawable.iv_generate_code,
                destinationRoute = "generate_qr_code_screen" // Replace with your actual route
            ),
            HomeAction(
                title = "Scan QR Code",
                icon = R.drawable.iv_scan_qr,
                destinationRoute = "scan_qr_code_screen" // Replace with your actual route
            ),
            HomeAction(
                title = "Saved QR Codes",
                icon = R.drawable.iv_savedqr,
                destinationRoute = "saved_qr_code_screen" // Replace with your actual route
            ),
            HomeAction(
                title = "Delete QR Code",
                icon = R.drawable.iv_deleteqr,
                destinationRoute = "delete_qr_code_screen" // Replace with your actual route
            ),
            HomeAction(
                title = "Contact Us",
                icon = R.drawable.contactus,
                destinationRoute = "about_us_screen", // Assuming AboutUsActivity handles both
                onClickAction = { nav, _ ->
                    SelectedFile.selectedOption = 1
                    nav.navigate("about_us_screen") // Navigate to AboutUs screen
                }
            ),
            HomeAction(
                title = "About Us",
                icon = R.drawable.info,
                destinationRoute = "about_us_screen",
                onClickAction = { nav, ctx ->
                    SelectedFile.selectedOption = 2
                    nav.navigate("about_us_screen") // Navigate to AboutUs screen
                }
            ),
            HomeAction(
                title = "Profile",
                icon = R.drawable.profile,
                destinationRoute = "user_profile_screen" // Replace with your actual route
            ),
            HomeAction(
                title = "Logout",
                icon = R.drawable.logout,
                onClickAction = { nav, ctx ->
                    println("Logout button clicked!")
                    QRCodeGeneratorData.writeLS(ctx, false)
                    nav.navigate(Screen.Login.route) {
                        popUpTo(nav.graph.id) { // Pop up to the start of the entire graph
                            inclusive = true // Include the start destination itself
                        }
                    }
                }
            )
        )
    }

    Scaffold(
        topBar = {
            // Optional: A more sophisticated TopAppBar can be added here
            // For this design, the app name is part of the main content.
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply Scaffold's padding
                .background(MaterialTheme.colorScheme.background) // Use theme background
        ) {
            // Top Section: App Logo, Name, and Caption
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color1) // Use color1 for the top section background
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Using a placeholder for ic_qrcode, replace with your actual drawable
                Image(
                    painter = painterResource(id = R.drawable.ic_qrcode), // Replace with your actual drawable
                    contentDescription = "QR Code App Icon",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f)) // Subtle background for icon
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "QR Code Generator",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White, // White text for app name
                        fontWeight = FontWeight.ExtraBold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Generate, Scan, Customize QR Codes",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White.copy(alpha = 0.8f) // Slightly transparent white
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Main Content Area: Grid of Action Cards
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = color2, // Use color2 for the main content background
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // 2 columns
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(homeActions) { action ->
                        ActionCard(
                            icon = action.icon,
                            title = action.title,
                            onClick = {
                                if (action.onClickAction != null) {
                                    action.onClickAction.invoke(navController, context)
                                } else if (action.destinationRoute != null) {
                                    navController.navigate(action.destinationRoute)
                                }
                                // If neither, it's a dummy action or needs specific handling
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ActionCard(
    icon: Int,
    title: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp) // Fixed height for consistent card size
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp), // More rounded corners
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp), // Increased elevation
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface, // Use theme surface color
            contentColor = MaterialTheme.colorScheme.onSurface // Use theme onSurface color for text/icons
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Padding inside the card
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary // Use theme primary color for icons
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold, // Slightly less bold than headline
                    fontSize = 16.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Preview for the Redesigned Home Screen
@Preview(showBackground = true, widthDp = 360)
@Composable
fun QRCodeHomeScreenPreview() {
    QrCodeGeneratorAppTheme {
        // For preview, we pass a dummy NavController
        HomeScreen(navController = rememberNavController())
    }
}
