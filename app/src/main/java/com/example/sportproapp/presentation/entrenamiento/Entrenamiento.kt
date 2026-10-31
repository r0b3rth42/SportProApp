package com.example.sportproapp.presentation.entrenamiento

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class EstadoEntrenamiento(val titulo: String) {
    EN_CURSO("En curso"),
    PLANIFICADO("Planificado"),
    COMPLETADO("Completado")
}

@Serializable
data class Entrenamiento(
    @SerialName("id") val id: String? = null,
    @SerialName("fecha") val fecha: String, // Formato YYYY-MM-DD
    @SerialName("hora_ini") val horaIni: String,
    @SerialName("hora_fin") val horaFin: String,
    @SerialName("equipo_id") val equipoId: String,
    @SerialName("sede_id") val sedeId: String,
    @SerialName("estado") val estado: Int
)
