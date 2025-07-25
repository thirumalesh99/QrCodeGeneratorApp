package tirumalesh.app.qrcodegeneratorapp

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase


@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current as Activity

    // Function to validate email format
    fun validateEmail(email: String): String? {
        return if (email.isBlank()) {
            "Email is required."
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Invalid email format."
        } else {
            null
        }
    }

    // Function to validate password
    fun validatePassword(password: String): String? {
        return if (password.isBlank()) {
            "Password is required."
        } else {
            null
        }
    }


    fun userSignIn(userData: UserData, context: Context, navController: NavController) {

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val databaseReference =
            firebaseDatabase.getReference("UserData").child(userData.emailid.replace(".", ","))

        databaseReference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dbData = task.result?.getValue(UserData::class.java)
                if (dbData != null) {
                    if (dbData.password == userData.password) {
                        QRCodeGeneratorData.writeLS(context, true)
                        QRCodeGeneratorData.writeMail(context, dbData.emailid)
                        QRCodeGeneratorData.writeUserName(context, dbData.name)

                        Toast.makeText(context, "Login Successfully", Toast.LENGTH_SHORT).show()

                        navController.popBackStack() // Clear back stack
                        navController.navigate(Screen.Home.route)

                    } else {
                        isLoading=false
                        Toast.makeText(context, "Seems Incorrect Credentials", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    isLoading=false
                    Toast.makeText(context, "Your account not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                isLoading=false
                Toast.makeText(
                    context,
                    "Something went wrong",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }





    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
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
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "QR Code Generator",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 30.dp)
        )

        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null // Clear error on change
                loginError = null
            },
            label = { Text("Email") },
            isError = emailError != null,
            supportingText = {
                emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            shape = RoundedCornerShape(10.dp) // Rounded corners
        )

        // Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null // Clear error on change
                loginError = null
            },
            label = { Text("Password") },
            isError = passwordError != null,
            supportingText = {
                passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(10.dp) // Rounded corners
        )

        // Display general login error
        loginError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 10.dp))
        }

        // Login Button
        Button(
            onClick = {
                emailError = validateEmail(email)
                passwordError = validatePassword(password)
                loginError = null // Clear previous login errors

                if (emailError == null && passwordError == null) {
                    isLoading = true

                    val userData = UserData(
                        "",
                        email,
                        "",
                        password
                    )

                    userSignIn(userData, context, navController)

                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading, // Disable button while loading
            shape = RoundedCornerShape(10.dp), // Rounded corners
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Register Link
        TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
            Text(
                text = "Don't have an account? Register",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            )
        }
    }
}

data class UserData(
    var name : String = "",
    var emailid : String = "",
    var country : String = "",
    var password: String = ""
)