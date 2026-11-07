package com.example.sportproapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class EstadoAsistencia(val label: String) {
    @SerialName("PRESENTE") PRESENTE("Presente"),
    @SerialName("AUSENTE") AUSENTE("Ausente"),
    @SerialName("JUSTIFICADO") JUSTIFICADO("Justificado")
}

@Serializable
data class Asistencia(
    @SerialName("id") val id: String? = null,
    @SerialName("entrenamiento_id") val entrenamientoId: String,
    @SerialName("jugador_id") val jugadorId: String,
    @SerialName("estado") val estado: EstadoAsistencia = EstadoAsistencia.PRESENTE,
    @SerialName("observacion") val observacion: String? = null
)

// Wrapper de ayuda para renderizar la UI
data class JugadorAsistenciaUI(
    val jugadorId: String,
    val nombreCompleto: String,
    val dorsal: String? = null,
    val estado: EstadoAsistencia = EstadoAsistencia.PRESENTE,
    val fotoUrl: String? = null
)