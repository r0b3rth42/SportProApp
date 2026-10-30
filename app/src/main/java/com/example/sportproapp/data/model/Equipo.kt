package com.example.sportproapp.data.model

import com.google.firebase.Timestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Equipo(
    @SerialName("id") val id: String = "",
    @SerialName("academia_id") val academiaId: String? = null,        // ID de la Academia a la que pertenece
    @SerialName("nombre") val nombre: String = "",             // Ej: "Tigres Academia"
    @SerialName("categoria") val categoria: String = "",         // Ej: "Sub-15", "Sub-18"
    @SerialName("stripeColorHex") val stripeColorHex: String = "#CCFF00", // Hexadecimal del color distintivo (Ej: "#FF2A6D")
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("status") val status: String = "ACTIVE"      // "ACTIVE", "INACTIVE"
)