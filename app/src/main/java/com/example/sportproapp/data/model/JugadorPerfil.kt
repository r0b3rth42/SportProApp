package com.example.sportproapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JugadorPerfil(
    @SerialName("id") val id: String,
    @SerialName("equipo_id") val equipoId: String? = null,
    @SerialName("fecha_nacimiento") val fechaNacimiento: String,
    @SerialName("posicion") val posicion: String,
    @SerialName("altura_cm") val alturaCm: Double,
    @SerialName("fotografia_url") val fotografiaUrl: String? = null,
    @SerialName("contacto_emergencia_nombre") val contactoEmergenciaNombre: String,
    @SerialName("contacto_emergencia_telefono") val contactoEmergenciaTelefono: String
)