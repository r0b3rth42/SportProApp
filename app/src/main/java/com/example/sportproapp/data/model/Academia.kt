package com.example.sportproapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Academia(
    @SerialName("id") val id: String,
    @SerialName("nombre") val nombre: String
)