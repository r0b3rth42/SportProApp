package com.example.sportproapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Convocatoria(
    @SerialName("id") val id: String? = null,
    @SerialName("partido_id") val partidoId: Long,
    @SerialName("equipo_id") val equipoId: String,
    @SerialName("jugador_id") val jugadorId: String,
    @SerialName("es_titular") val esTitular: Boolean = false,
    @SerialName("dorsal") val dorsal: Int? = null,
    @SerialName("posicion") val posicion: String? = null,
    @SerialName("estado") val estado: String = "CONVOCADO",
    @SerialName("created_at") val createdAt: String? = null
)
