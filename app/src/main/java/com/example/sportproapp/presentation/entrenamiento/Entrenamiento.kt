package com.example.sportproapp.presentation.entrenamiento

enum class EstadoEntrenamiento(val titulo: String) {
    EN_CURSO("En curso"),
    PLANIFICADO("Planificado"),
    COMPLETADO("Completado")
}

data class Entrenamiento(
    val id: String,
    val horaInicioFin: String,
    val equipo: String,
    val lugar: String,
    val cantidadEjercicios: Int,
    val estado: EstadoEntrenamiento
)
