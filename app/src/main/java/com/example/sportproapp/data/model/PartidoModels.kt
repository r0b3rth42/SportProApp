package com.example.sportproapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CondicionEquipo {
    @SerialName("LOCAL") LOCAL,
    @SerialName("VISITANTE") VISITANTE
}

@Serializable
enum class EstadoDesafio {
    @SerialName("PENDIENTE") PENDIENTE,
    @SerialName("ACEPTADO") ACEPTADO,
    @SerialName("RECHAZADO") RECHAZADO,
    @SerialName("CANCELADO") CANCELADO
}

// Tabla desafio_partido
@Serializable
data class DesafioPartido(
    @SerialName("id") val id: Long? = null,
    @SerialName("equipo_emisor_id") val equipoEmisorId: String,
    @SerialName("academia_emisor_id") val academiaEmisorId: String,
    @SerialName("academia_receptor_id") val academiaReceptorId: String,
    @SerialName("equipo_receptor_id") val equipoReceptorId: String? = null,
    @SerialName("sede_id") val sedeId: String,
    @SerialName("fecha") val fecha: String,
    @SerialName("hora") val hora: String,
    @SerialName("mensaje") val mensaje: String? = null,
    @SerialName("estado") val estado: EstadoDesafio = EstadoDesafio.PENDIENTE
)

// Tabla partido
@Serializable
data class Partido(
    @SerialName("id") val id: Long? = null,
    @SerialName("tipo_partido") val tipoPartido: String = "AMISTOSO",
    @SerialName("fase_id") val faseId: Long? = null,
    @SerialName("sede_id") val sedeId: String,
    @SerialName("fecha") val fecha: String,
    @SerialName("hora") val hora: String,
    @SerialName("estado") val estado: String = "PROGRAMADO",
    @SerialName("desafio_id") val desafioId: Long? = null
)

// Tabla partido_equipo
@Serializable
data class PartidoEquipo(
    @SerialName("partido_id") val partidoId: Long,
    @SerialName("equipo_id") val equipoId: String,
    @SerialName("condicion") val condicion: CondicionEquipo,
    @SerialName("goles") val goles: Int? = null
)

@Serializable
data class PartidoDetalleDto(
    @SerialName("id") val id: Long,
    @SerialName("tipo_partido") val tipoPartido: String,
    @SerialName("fecha") val fecha: String,
    @SerialName("hora") val hora: String,
    @SerialName("estado") val estado: String,
    @SerialName("sede_id") val sedeId: String
)