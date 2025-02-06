package com.example.consultapp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.consultapp.ui.theme.ConsultAppTheme
import com.google.firebase.database.*
import com.example.consultapp.ui.components.MessageBubble


class ChatActivityDoctor : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsultAppTheme {
                // Retrieve the patient name from the intent extras.
                val patientName = intent.getStringExtra("chatPartner") ?: "Unknown Patient"
                val navController = rememberNavController()
                ChatViewDoctor(patientName, navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatViewDoctor(patientName: String, navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val message = remember { mutableStateOf("") }
    val messagesList = remember { mutableStateListOf<Pair<String, String>>() } // Pair<sender, message>

    // Sanitize the patient name to form a valid Firebase node path.
    val sanitizedPatientName = patientName.replace(".", "")
        .replace("#", "").replace("$", "")
        .replace("[", "").replace("]", "")

    // Use the same Firebase node structure as the patient chat screen.
    val database = FirebaseDatabase.getInstance().reference
        .child("chats").child(sanitizedPatientName)

    // Listen for new messages from Firebase.
    LaunchedEffect(Unit) {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val t = object : GenericTypeIndicator<Map<String, String>>() {}
                val messageMap = snapshot.getValue(t)
                val sender = messageMap?.get("sender") ?: "Unknown"
                val text = messageMap?.get("message") ?: ""
                messagesList.add(Pair(sender, text))
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat with $patientName") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display messages in a list.
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messagesList) { (sender, msg) ->
                        MessageBubble(message = msg, isPatient = sender == "Patient")
                    }
                }
                // Text field for entering a new message.
                OutlinedTextField(
                    value = message.value,
                    onValueChange = { message.value = it },
                    label = { Text("Enter your message") },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
                // Send button: pushes message with sender "Doctor".
                Button(
                    onClick = {
                        if (message.value.isNotEmpty()) {
                            val newMessage = mapOf(
                                "sender" to "Doctor",
                                "message" to message.value
                            )
                            database.push().setValue(newMessage)
                            message.value = ""
                        }
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Send")
                }
            }
        }
    )
}
