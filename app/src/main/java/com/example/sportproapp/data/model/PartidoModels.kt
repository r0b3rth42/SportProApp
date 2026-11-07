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

// Tabla campeonato
@Serializable
data class Campeonato(
    @SerialName("id") val id: Long? = null,
    @SerialName("nombre") val nombre: String = "",
    @SerialName("temporada") val temporada: String = "",
    @SerialName("fecha_inicio") val fechaInicio: String = "",
    @SerialName("fecha_fin") val fechaFin: String = "",
    @SerialName("estado") val estado: String = "PROGRAMADO"
)

// Tabla fase_campeonato
@Serializable
data class FaseCampeonato(
    @SerialName("id") val id: Long? = null,
    @SerialName("campeonato_id") val campeonatoId: Long = 0,
    @SerialName("nombre") val nombre: String = "",
    @SerialName("tipo_fase") val tipoFase: String = "LIGA",
    @SerialName("orden") val orden: Int = 1,
    @SerialName("estado") val estado: String = "PENDIENTE"
)

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

@Serializable
data class PartidoConDetalles(
    @SerialName("id") val id: Long = 0,
    @SerialName("tipo_partido") val tipoPartido: String = "AMISTOSO",
    @SerialName("fecha") val fecha: String = "",
    @SerialName("hora") val hora: String = "",
    @SerialName("estado") val estado: String = "PROGRAMADO",
    @SerialName("sede_id") val sedeId: String = "",
    @SerialName("sede_nombre") val sedeNombre: String = "Sede Principal",
    @SerialName("sede_direccion") val sedeDireccion: String = "",
    @SerialName("equipo_local_id") val equipoLocalId: String = "",
    @SerialName("equipo_local_nombre") val equipoLocalNombre: String = "Equipo Local",
    @SerialName("equipo_local_categoria") val equipoLocalCategoria: String = "",
    @SerialName("goles_local") val golesLocal: Int? = null,
    @SerialName("equipo_visitante_id") val equipoVisitanteId: String = "",
    @SerialName("equipo_visitante_nombre") val equipoVisitanteNombre: String = "Equipo Visitante",
    @SerialName("equipo_visitante_categoria") val equipoVisitanteCategoria: String = "",
    @SerialName("goles_visitante") val golesVisitante: Int? = null,
    @SerialName("desafio_id") val desafioId: Long? = null,
    @SerialName("mensaje_desafio") val mensajeDesafio: String? = null
)