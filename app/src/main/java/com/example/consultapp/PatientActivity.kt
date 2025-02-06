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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.consultapp.ui.theme.ConsultAppTheme
import com.google.firebase.auth.FirebaseAuth

class PatientActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsultAppTheme {
                // Create a NavController (though not used here for back navigation)
                val navController = rememberNavController()
                PatientView(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientView(navController: NavController) {
    // Get current context to use for starting new activities
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient View") }
                // No navigationIcon here, so no back arrow is shown
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
                Button(
                    onClick = {
                        val intent = Intent(context, SelectDoctorActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "Select Doctor")
                }
                Button(
                    onClick = {
                        val intent = Intent(context, LiveConsultationActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "Live Consultation")
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
