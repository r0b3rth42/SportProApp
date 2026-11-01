package com.example.sportproapp.data.session

data class UserSession(
    val uid: String,
    val name: String,
    val email: String,
    val role: String,          // "JUG", "PAD", "ADM", "DT"
    val academiaId: String? = null,
    val phone: String? = null,
    val perfilId: String? = null
)