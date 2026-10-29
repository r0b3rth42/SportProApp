package com.example.sportproapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminPerfil(
    @SerialName("id") val id: String,
    @SerialName("academia_id") val academiaId: String,
    @SerialName("cargo") val cargo: String
)