package com.example.sportproapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Usuarios (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val estado: Boolean = true,
    val fechaRegistro: String = ""
)

enum class UserRole {
    DT,
    JUG,
    PAD,
    ADM
}

enum class UserStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED
}