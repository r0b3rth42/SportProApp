package com.example.sportproapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Sede (
    @SerialName("id") val id: String,
    @SerialName("nombre") val nombre: String,
    @SerialName("direccion") val direccion: String,
    @SerialName("telefono") val telefono: String,
    @SerialName("email") val email: String,
    @SerialName("fecha_registro") val fechaRegistro: String,
    @SerialName("academia_id") val academiaId: String
    )