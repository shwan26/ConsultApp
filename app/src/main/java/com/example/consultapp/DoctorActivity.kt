package com.example.consultapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.consultapp.ui.theme.ConsultAppTheme
import com.google.firebase.auth.FirebaseAuth

class DoctorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsultAppTheme {
                val navController = rememberNavController()
                DoctorView(navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorView(navController: NavHostController) {
    val context = LocalContext.current  // Obtain the current context
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor View") }
                // Removed the navigationIcon to eliminate the back arrow
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {
                    val intent = Intent(context, ApprovePatientsActivity::class.java)
                    context.startActivity(intent)
                }, modifier = Modifier.padding(8.dp)) {
                    Text(text = "Approve Patients")
                }
                Button(onClick = {
                    val intent = Intent(context, PatientListActivity::class.java)
                    context.startActivity(intent)
                }, modifier = Modifier.padding(8.dp)) {
                    Text(text = "Patient List")
                }
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "Logout")
                }
            }
        }
    )
}
