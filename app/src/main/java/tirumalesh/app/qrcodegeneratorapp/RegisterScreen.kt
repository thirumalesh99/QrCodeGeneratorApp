package tirumalesh.app.qrcodegeneratorapp

import android.app.Activity
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay

/**
 * Registration Screen Composable
 * Allows users to register with username, email, place, and password, includes validation.
 * @param navController NavController for navigation actions.
 */
// Required for OutlinedTextField
@Composable
fun RegisterScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var placeError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var registerError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current as Activity

    // Validation functions
    fun validateUsername(username: String): String? {
        return if (username.isBlank()) "Username is required." else null
    }

    fun validateEmail(email: String): String? {
        return if (email.isBlank()) {
            "Email is required."
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Invalid email format."
        } else {
            null
        }
    }

    fun validatePlace(place: String): String? {
        return if (place.isBlank()) "Place is required." else null
    }

    fun validatePassword(password: String): String? {
        return if (password.isBlank()) {
            "Password is required."
        } else if (password.length < 6) {
            "Password must be at least 6 characters long."
        } else {
            null
        }
    }



    fun registerUser(userData: UserData, context: Context) {

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val databaseReference = firebaseDatabase.getReference("UserData")
        databaseReference.child(userData.emailid.replace(".", ","))
            .setValue(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "You Registered Successfully", Toast.LENGTH_SHORT)
                        .show()

                    navController.popBackStack() // Clear back stack
                    navController.navigate(Screen.Login.route)

                } else {
                    isLoading=false
                    Toast.makeText(
                        context,
                        "Registration Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { _ ->
                isLoading=false
                Toast.makeText(
                    context,
                    "Something went wrong",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Register",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 30.dp)
        )

        // Username Input Field
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                usernameError = null
                registerError = null
            },
            label = { Text("Username") },
            isError = usernameError != null,
            supportingText = {
                usernameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            shape = RoundedCornerShape(10.dp)
        )

        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
                registerError = null
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
            shape = RoundedCornerShape(10.dp)
        )

        // Place Input Field
        OutlinedTextField(
            value = place,
            onValueChange = {
                place = it
                placeError = null
                registerError = null
            },
            label = { Text("Place") },
            isError = placeError != null,
            supportingText = {
                placeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            shape = RoundedCornerShape(10.dp)
        )

        // Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
                registerError = null
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
            shape = RoundedCornerShape(10.dp)
        )

        // Display general registration error
        registerError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 10.dp))
        }

        // Register Button
        Button(
            onClick = {
                usernameError = validateUsername(username)
                emailError = validateEmail(email)
                placeError = validatePlace(place)
                passwordError = validatePassword(password)
                registerError = null // Clear previous errors

                if (usernameError == null && emailError == null && placeError == null && passwordError == null) {
                    isLoading = true
                    val userData = UserData(
                        username,
                        email,
                        place,
                        password
                    )
                    registerUser(userData,context)

//                    coroutineScope.launch {
//                        dummySignUp(username, email, place, password)
//                            .onSuccess {
//                                println(it)
//                                // Navigate to Login after successful registration
//                                navController.popBackStack() // Clear back stack
//                                navController.navigate(Screen.Login.route)
//                            }
//                            .onFailure { e ->
//                                registerError = e.message ?: "An unexpected error occurred during registration."
//                                println("Registration failed: ${e.message}")
//                            }
//                        isLoading = false
//                    }
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
                Text("Register", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Login Link
        TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
            Text(
                text = "Already have an account? Login",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            )
        }
    }
}
