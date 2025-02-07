package com.example.consultapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.consultapp.ui.theme.ConsultAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = Firebase.auth

        setContent {
            ConsultAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "roleSelection") {
                    // Role Selection Screen (only patient and doctor)
                    composable("roleSelection") {
                        RoleSelectionScreen(onRoleSelected = { role ->
                            navController.navigate("login/$role")
                        })
                    }
                    // Login Screen: Includes back and create account options.
                    composable(
                        "login/{role}",
                        arguments = listOf(navArgument("role") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val role = backStackEntry.arguments?.getString("role") ?: ""
                        RoleLoginScreen(
                            role = role,
                            onLoginSuccess = { navController.navigate(role) },
                            onBack = { navController.popBackStack() },
                            onNavigateToCreateAccount = { navController.navigate("createAccount/$role") }
                        )
                    }
                    // Create Account Screen: Stores the selected role.
                    composable(
                        "createAccount/{role}",
                        arguments = listOf(navArgument("role") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val role = backStackEntry.arguments?.getString("role") ?: ""
                        CreateAccountScreen(
                            role = role,
                            onCreateAccountSuccess = { navController.navigate(role) },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    // Role-specific Home Screens
                    composable("patient") { PatientView(navController) }
                    composable("doctor") { DoctorView(navController) }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("MainActivity", "User already signed in: ${currentUser.email}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(onRoleSelected: (String) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Login") }) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Login as:")
                Button(
                    onClick = { onRoleSelected("patient") },
                    modifier = Modifier.padding(8.dp)
                ) { Text("Patient") }
                Button(
                    onClick = { onRoleSelected("doctor") },
                    modifier = Modifier.padding(8.dp)
                ) { Text("Doctor") }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleLoginScreen(
    role: String,
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit,
    onNavigateToCreateAccount: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login as $role") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") }
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") }
                )
                errorMessage?.let { msg ->
                    Text(text = msg, color = Color.Red)
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            Firebase.auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // After sign in, verify that the stored role matches the login role.
                                        val uid = Firebase.auth.currentUser?.uid
                                        if (uid != null) {
                                            Firebase.database.getReference("users")
                                                .child(uid)
                                                .child("role")
                                                .get()
                                                .addOnSuccessListener { snapshot ->
                                                    val storedRole = snapshot.getValue<String>()
                                                    if (storedRole == role) {
                                                        Log.d("Login", "Role verified: $storedRole")
                                                        onLoginSuccess()
                                                    } else {
                                                        Log.w("Login", "Role mismatch. Expected: $role, Found: $storedRole")
                                                        errorMessage = "Your account is registered as $storedRole, not $role."
                                                        Firebase.auth.signOut()
                                                    }
                                                }
                                                .addOnFailureListener { exception ->
                                                    errorMessage = exception.message ?: "Role verification failed."
                                                }
                                        }
                                    } else {
                                        Log.w("Login", "signInWithEmail:failure", task.exception)
                                        errorMessage = task.exception?.message ?: "Login failed"
                                    }
                                }
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Login")
                }
                Button(
                    onClick = onNavigateToCreateAccount,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Create Account")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    role: String,
    onCreateAccountSuccess: () -> Unit,
    onBack: () -> Unit
) {
    // New state holder for the user's name.
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account as $role") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Input for the user's name.
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") }
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") }
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") }
                )
                errorMessage?.let { msg ->
                    Text(text = msg, color = Color.Red)
                }
                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match"
                        } else {
                            Firebase.auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("CreateAccount", "createUserWithEmail:success")
                                        val uid = Firebase.auth.currentUser?.uid
                                        if (uid != null) {
                                            // Create a map with the user's info.
                                            val userMap = mapOf(
                                                "name" to name,
                                                "email" to email,
                                                "role" to role
                                            )
                                            // Store the user's info in the database.
                                            Firebase.database.getReference("users")
                                                .child(uid)
                                                .setValue(userMap)
                                                .addOnCompleteListener { roleTask ->
                                                    if (roleTask.isSuccessful) {
                                                        onCreateAccountSuccess()
                                                    } else {
                                                        errorMessage = roleTask.exception?.message
                                                            ?: "Failed to save user info."
                                                    }
                                                }
                                        }
                                    } else {
                                        Log.w("CreateAccount", "createUserWithEmail:failure", task.exception)
                                        errorMessage = task.exception?.message ?: "Account creation failed"
                                    }
                                }
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Create Account")
                }
            }
        }
    )
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ConsultAppTheme {
        Greeting("Android")
    }
}
