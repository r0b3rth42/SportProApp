package com.example.sportproapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BloqueEntrenamiento(val tituloMostrar: String) {
    @SerialName("CALENTAMIENTO") CALENTAMIENTO("Calentamiento"),
    @SerialName("PARTE_PRINCIPAL") PARTE_PRINCIPAL("Parte principal"),
    @SerialName("VUELTA_A_LA_CALMA") VUELTA_A_LA_CALMA("Vuelta a la calma")
}

@Serializable
data class Ejercicio(
    @SerialName("id") val id: String? = null,
    @SerialName("academia_id") val academiaId: String? = null,
    @SerialName("bloque") val bloque: BloqueEntrenamiento,
    @SerialName("titulo") val titulo: String,
    @SerialName("descripcion") val descripcion: String? = null,
    @SerialName("duracion_min") val duracionMin: Int = 0,
    @SerialName("orden") val orden: Int = 0
)

@Serializable
data class EntrenamientoEjercicioRelation(
    @SerialName("entrenamiento_id") val entrenamientoId: String,
    @SerialName("ejercicio_id") val ejercicioId: String
)