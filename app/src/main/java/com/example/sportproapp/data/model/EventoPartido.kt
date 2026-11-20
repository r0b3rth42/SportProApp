package com.example.sportproapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TipoEvento(val label: String) {
    @SerialName("GOL") GOL("Gol"),
    @SerialName("ASISTENCIA") ASISTENCIA("Asistencia"),
    @SerialName("TARJETA_AMARILLA") TARJETA_AMARILLA("T. Amarilla"),
    @SerialName("TARJETA_ROJA") TARJETA_ROJA("T. Roja"),
    @SerialName("CAMBIO") CAMBIO("Cambio"),
    @SerialName("FALTA") FALTA("Falta"),
    @SerialName("TIRO") TIRO("Tiro"),
    @SerialName("CORNER") CORNER("Corner");

    companion object {
        fun fromString(tipo: String): TipoEvento {
            return entries.firstOrNull { it.name.equals(tipo, ignoreCase = true) } ?: GOL
        }
    }
}

// DTO para la tabla 'eventos_partido' en Supabase
@Serializable
data class EventoPartido(
    @SerialName("id") val id: String? = null,
    @SerialName("partido_id") val partidoId: Long,
    @SerialName("equipo_id") val equipoId: String,
    @SerialName("jugador_id") val jugadorId: String? = null,
    @SerialName("tipo_evento") val tipoEvento: String,
    @SerialName("minuto") val minuto: Int = 0,
    @SerialName("detalle") val detalle: String? = null,
    @SerialName("jugador_sale_id") val jugadorSaleId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// Modelo para renderizado en la interfaz con nombres de jugador y dorsal resueltos
data class EventoPartidoUI(
    val id: String,
    val partidoId: Long,
    val equipoId: String,
    val equipoNombre: String,
    val jugadorId: String?,
    val jugadorNombre: String?,
    val jugadorDorsal: Int?,
    val tipoEvento: TipoEvento,
    val minuto: Int,
    val detalle: String?,
    val jugadorSaleId: String?,
    val jugadorSaleNombre: String?
)
