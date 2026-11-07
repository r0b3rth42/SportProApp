package com.example.sportproapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Tabla notificaciones: avisos dentro de la app (pantalla 2.05)
@Serializable
data class Notificacion(
    @SerialName("id") val id: String? = null,
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("tipo") val tipo: String = "GENERAL",          // "MENSUALIDAD", "GENERAL"
    @SerialName("titulo") val titulo: String,
    @SerialName("mensaje") val mensaje: String,
    @SerialName("referencia_id") val referenciaId: String? = null,
    @SerialName("leida") val leida: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)
