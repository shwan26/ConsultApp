package com.example.consultapp

data class AppointmentRequest(
    val id: String = "",
    val patientName: String = "",
    val status: String = "pending"
)
