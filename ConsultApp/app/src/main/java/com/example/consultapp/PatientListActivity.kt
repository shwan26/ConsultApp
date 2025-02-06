package com.example.consultapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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


class PatientListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsultAppTheme {
                // Even if you're using navigation, here we use finish() to go back.
                val navController = rememberNavController()
                PatientListActivityView(navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListActivityView(navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as? Activity
    // Create a list to hold approved patients.
    val patients = remember { mutableStateListOf<Patient>() }
    val database = FirebaseDatabase.getInstance().reference.child("patients")

    LaunchedEffect(Unit) {
        // Add sample patients for demonstration if the list is empty.
        if (patients.isEmpty()) {
            patients.addAll(
                listOf(
                    Patient(id = "sample1", name = "Sample Patient 1", approved = true),
                    Patient(id = "sample2", name = "Sample Patient 2", approved = true),
                    Patient(id = "sample3", name = "Sample Patient 3", approved = true)
                )
            )
        }
        // Listen for changes in Firebase
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val patient = snapshot.getValue(Patient::class.java)
                patient?.let {
                    // Only add if approved
                    if (it.approved) {
                        val updatedPatient = it.copy(id = snapshot.key ?: "")
                        if (patients.none { p -> p.id == updatedPatient.id }) {
                            patients.add(updatedPatient)
                        }
                    }
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val updated = snapshot.getValue(Patient::class.java)
                updated?.let { newPatient ->
                    val key = snapshot.key
                    val index = patients.indexOfFirst { it.id == key }
                    if (newPatient.approved) {
                        if (index != -1) {
                            patients[index] = newPatient.copy(id = key ?: "")
                        } else {
                            patients.add(newPatient.copy(id = key ?: ""))
                        }
                    } else {
                        // If a patient becomes unapproved, remove them.
                        patients.removeAll { it.id == key }
                    }
                }
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val key = snapshot.key
                patients.removeAll { it.id == key }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }
            override fun onCancelled(error: DatabaseError) { }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Approved Patients") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(patients) { patient ->
                    // Each item is clickable; tapping it opens the chat with that patient.
                    Text(
                        text = patient.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(context, ChatActivityDoctor::class.java)
                                // Passing the patient name as the "chatPartner" extra.
                                intent.putExtra("chatPartner", patient.name)
                                context.startActivity(intent)
                            }
                            .padding(16.dp)
                    )
                }
            }
        }
    )
}
