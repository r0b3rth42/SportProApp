package com.example.sportproapp.data.model

import PadrePerfil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Usuarios (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    @SerialName("academia_id") val academiaId: String? = null,
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

data class JugadorConUsuario(
    val usuario: Usuarios,
    val perfil: JugadorPerfil
)

data class PadreConUsuario(
    val usuario: Usuarios,
    val perfil: PadrePerfil
)
