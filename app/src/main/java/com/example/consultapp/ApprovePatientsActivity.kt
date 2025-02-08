package com.example.consultapp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.consultapp.ui.theme.ConsultAppTheme
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class ApprovePatientsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            ConsultAppTheme(darkTheme = isDarkMode)   {
                val navController = rememberNavController()
                // For demonstration, we use a hardcoded doctor ID.
                ApproveAppointmentsView(
                    doctorId = "DrJohnDoe",
                    navController = navController,
                    darkModeEnabled = isDarkMode,
                    onToggleDarkMode = {isDarkMode = !isDarkMode }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApproveAppointmentsView(
    doctorId: String,
    navController: NavHostController,
    darkModeEnabled: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val context = LocalContext.current
    // A list to hold appointment requests retrieved from Firebase.
    val requests = remember { mutableStateListOf<AppointmentRequest>() }
    // Reference to Firebase node: "appointments/<doctorId>"
    val database = FirebaseDatabase.getInstance().reference.child("appointments").child(doctorId)

    LaunchedEffect(Unit) {
        // Add a sample appointment request for demonstration purposes if list is empty.
        if (requests.isEmpty()) {
            requests.add(
                AppointmentRequest(
                    id = "sample1",
                    patientName = "Sample Patient",
                    status = "pending"
                )
            )
        }
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Retrieve the request and update the request's id using snapshot.key
                val appointment = snapshot.getValue(AppointmentRequest::class.java)
                appointment?.let {
                    val updatedRequest = it.copy(id = snapshot.key ?: "")
                    // Avoid duplicate entries if sample data already exists.
                    if (requests.none { r -> r.id == updatedRequest.id }) {
                        requests.add(updatedRequest)
                    }
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Optional: Update the request in the list if it changes.
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val key = snapshot.key
                requests.removeAll { it.id == key }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Approve Patients",
                onBack = { (context as? Activity)?.finish() },
                darkModeEnabled = darkModeEnabled,
                onToggleDarkMode = onToggleDarkMode
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(requests) { request ->
                        AppointmentRequestItemUI(
                            request = request,
                            onAccept = {
                                // Update the appointment status to "accepted" in Firebase.
                                database.child(request.id).updateChildren(mapOf("status" to "accepted"))
                            },
                            onReject = {
                                // Update the appointment status to "rejected" in Firebase.
                                database.child(request.id).updateChildren(mapOf("status" to "rejected"))
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun AppointmentRequestItemUI(
    request: AppointmentRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display the patient’s name
        Text(text = request.patientName, modifier = Modifier.weight(1f))
        // Accept button (tick)
        Button(onClick = onAccept, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text("✓")
        }
        // Reject button (cross)
        Button(onClick = onReject, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text("✕")
        }
    }
}
