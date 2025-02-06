package com.example.consultapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()

        setContent {
            ConsultAppTheme {
                // Create a NavController for navigation between screens
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginPage(onRoleSelected = { role ->
                            when (role) {
                                "patient" -> navController.navigate("patient")
                                "doctor" -> navController.navigate("doctor")
                                "admin" -> navController.navigate("admin")
                            }
                        })
                    }
                    composable("patient") {
                        PatientView(navController)
                    }
                    composable("doctor") {
                        DoctorView(navController)
                    }
                    composable("admin") {
                        AdminView(navController)
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(onRoleSelected: (String) -> Unit) {
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
                Text(text = "Login as:")
                Button(
                    onClick = { onRoleSelected("patient") },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "Patient")
                }
                Button(
                    onClick = { onRoleSelected("doctor") },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "Doctor")
                }
                Button(
                    onClick = { onRoleSelected("admin") },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "Admin")
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
