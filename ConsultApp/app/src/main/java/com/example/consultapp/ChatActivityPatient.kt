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


class ChatActivityPatient : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsultAppTheme {
                val doctorName = intent.getStringExtra("doctorName") ?: "Unknown Doctor"
                val navController = rememberNavController()
                ChatView(doctorName, navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatView(doctorName: String, navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val message = remember { mutableStateOf("") }
    val messagesList = remember { mutableStateListOf<Pair<String, String>>() } // Pair<sender, message>

    // Sanitize the doctor name for Firebase path
    val sanitizedDoctorName = doctorName.replace(".", "").replace("#", "")
        .replace("$", "").replace("[", "").replace("]", "")

    val database = FirebaseDatabase.getInstance().reference.child("chats").child(sanitizedDoctorName)

    // Listen for new messages
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
                title = { Text("Chat with $doctorName") },
                navigationIcon = {
                    IconButton(onClick = {
                        activity?.finish()
                    }) {
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

                OutlinedTextField(
                    value = message.value,
                    onValueChange = { message.value = it },
                    label = { Text("Enter your message") },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (message.value.isNotEmpty()) {
                            val newMessage = mapOf(
                                "sender" to "Patient",
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
