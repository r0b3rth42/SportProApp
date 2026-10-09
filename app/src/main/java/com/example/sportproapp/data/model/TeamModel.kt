package com.example.sportproapp.data.model

import com.google.firebase.Timestamp

data class TeamModel(
    val id: String = "",
    val academyId: String = "",        // ID de la Academia a la que pertenece
    val name: String = "",             // Ej: "Tigres Academia"
    val category: String = "",         // Ej: "Sub-15", "Sub-18"
    val coachName: String = "",        // Nombre del DT a cargo
    val coachId: String = "",          // ID del usuario DT (si aplica)
    val stripeColorHex: String = "#CCFF00", // Hexadecimal del color distintivo (Ej: "#FF2A6D")
    val hasSubdivision: Boolean = true,// Si habilita la etiqueta "+ sub-división"
    val playerCount: Int = 0,          // Contador dinámico de jugadores
    val createdAt: Timestamp = Timestamp.now(),
    val status: String = "ACTIVE"      // "ACTIVE", "INACTIVE"
)