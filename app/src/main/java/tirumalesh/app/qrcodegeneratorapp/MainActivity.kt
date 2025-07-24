package tirumalesh.app.qrcodegeneratorapp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import tirumalesh.app.qrcodegeneratorapp.qrcodefunctions.DeleteQrCodeScreen
import tirumalesh.app.qrcodegeneratorapp.qrcodefunctions.GenerateQRCodeScreen
import tirumalesh.app.qrcodegeneratorapp.qrcodefunctions.QRScannerScreen
import tirumalesh.app.qrcodegeneratorapp.qrcodefunctions.SavedQrCodesScreen
import tirumalesh.app.qrcodegeneratorapp.ui.theme.QrCodeGeneratorAppTheme

// Define sealed class for navigation routes
sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object Home : Screen("home_screen")
    object QRGenerator : Screen("generate_qr_code_screen")
    object ScanQR : Screen("scan_qr_code_screen")
    object SavedQR : Screen("saved_qr_code_screen")
    object DeleteQR : Screen("delete_qr_code_screen")
    object ContactUs : Screen("about_us_screen")

    object Profile : Screen("user_profile_screen")

    object Logout : Screen("logout")



}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrCodeGeneratorAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Screen.Splash.route) {
                        composable(Screen.Splash.route) {
                            SplashScreen(navController = navController)
                        }
                        composable(Screen.Login.route) {
                            LoginScreen(navController = navController)
                        }
                        composable(Screen.Register.route) {
                            RegisterScreen(navController = navController)
                        }
                        composable(Screen.Home.route) {
                            HomeScreen(navController)
                        }
                        composable(Screen.QRGenerator.route)
                        {
                            GenerateQRCodeScreen(navController = navController)
                        }

                        composable(Screen.ScanQR.route)
                        {
                            QRScannerScreen(navController = navController)
                        }

                        composable(Screen.SavedQR.route)
                        {
                            SavedQrCodesScreen(navController = navController)
                        }

                        composable(Screen.DeleteQR.route)
                        {
                            DeleteQrCodeScreen(navController = navController)
                        }

                        composable(Screen.ContactUs.route)
                        {
                            AboutUsScreen(navController = navController)
                        }

                        composable(Screen.Profile.route)
                        {
                            UserProfileScreen(navController = navController)
                        }



                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(navController: NavController) {

    val context = LocalContext.current as Activity

  LaunchedEffect(key1 = true) {
        delay(2000L) // 2 seconds delay
        // Navigate to Login and remove Splash from the back stack

        if(QRCodeGeneratorData.readLS(context))
        {
            navController.popBackStack() // Remove current (Splash) from back stack
            navController.navigate(Screen.Home.route) // Navigate to Login
        }else{
            navController.popBackStack() // Remove current (Splash) from back stack
            navController.navigate(Screen.Login.route) // Navigate to Login
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary), // Use primary color for splash background
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_qrcode),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "QR Code Generator",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "by Tirumalesh",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


// Preview for the Splash Screen
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    QrCodeGeneratorAppTheme {
        // For preview, we pass a dummy NavController
        SplashScreen(navController = rememberNavController())
    }
}





