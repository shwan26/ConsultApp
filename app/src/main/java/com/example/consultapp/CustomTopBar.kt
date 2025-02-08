@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.consultapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun CustomTopAppBar(
    title: String,
    onBack: (() -> Unit)? = null, // Optional: only show the back icon if provided.
    darkModeEnabled: Boolean,
    onToggleDarkMode: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Display the logo image.
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(40.dp) // Adjust the height as needed.
                )
                // Display the title text.
                Text(text = title)
            }
        },
        navigationIcon = {
            if (onBack != null) {
                // Directly supply the composable lambda for navigationIcon.
                IconButton(onClick = { onBack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            } else null
        },
        actions = {
            // Add a dark/light mode toggle button.
            IconButton(onClick = onToggleDarkMode) {
                if (darkModeEnabled) {
                    Icon(Icons.Filled.BrightnessHigh, contentDescription = "Switch to Light Mode")
                } else {
                    Icon(Icons.Filled.Brightness4, contentDescription = "Switch to Dark Mode")
                }
            }
        }
    )
}
