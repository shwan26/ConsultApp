package com.example.consultapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MessageBubble(message: String, isPatient: Boolean) {
    val bubbleColor = if (isPatient) Color(0xFFDCF8C6) else Color(0xFFE0E0E0)
    val alignment = if (isPatient) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(bubbleColor)
                .padding(12.dp)
                .align(alignment)
        ) {
            Text(text = message)
        }
    }
}
