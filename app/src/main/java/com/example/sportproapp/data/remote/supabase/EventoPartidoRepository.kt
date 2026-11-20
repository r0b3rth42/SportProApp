package com.example.sportproapp.data.remote.supabase

import android.util.Log
import com.example.sportproapp.data.model.Equipo
import com.example.sportproapp.data.model.EventoPartido
import com.example.sportproapp.data.model.EventoPartidoUI
import com.example.sportproapp.data.model.PartidoEquipo
import com.example.sportproapp.data.model.TipoEvento
import com.example.sportproapp.data.model.Usuarios
import com.example.sportproapp.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import kotlin.math.max

object EventoPartidoRepository {

    private const val TAG = "EventoPartidoRepo"

    suspend fun getEventosPartido(partidoId: Long): Result<List<EventoPartidoUI>> {
        return try {
            val eventosRaw = supabase.from("eventos_partido")
                .select {
                    filter { eq("partido_id", partidoId) }
                }
                .decodeList<EventoPartido>()

            if (eventosRaw.isEmpty()) return Result.success(emptyList())

            // Obtener IDs de equipos y jugadores únicos para optimizar consultas
            val equipoIds = eventosRaw.map { it.equipoId }.distinct()
            val jugadorIds = (eventosRaw.mapNotNull { it.jugadorId } + eventosRaw.mapNotNull { it.jugadorSaleId }).distinct()

            val equiposMap = if (equipoIds.isNotEmpty()) {
                supabase.from("equipos")
                    .select { filter { isIn("id", equipoIds) } }
                    .decodeList<Equipo>()
                    .associateBy { it.id }
            } else emptyMap()

            val usuariosMap = if (jugadorIds.isNotEmpty()) {
                supabase.from("usuarios")
                    .select { filter { isIn("id", jugadorIds) } }
                    .decodeList<Usuarios>()
                    .associateBy { it.id }
            } else emptyMap()

            val listaUI = eventosRaw.sortedByDescending { it.minuto }.map { evt ->
                val equipo = equiposMap[evt.equipoId]
                val usuarioJugador = evt.jugadorId?.let { usuariosMap[it] }
                val usuarioJugadorSale = evt.jugadorSaleId?.let { usuariosMap[it] }

                val nombreJugador = usuarioJugador?.name?.ifBlank { null }
                val nombreJugadorSale = usuarioJugadorSale?.name?.ifBlank { null }

                EventoPartidoUI(
                    id = evt.id ?: "",
                    partidoId = evt.partidoId,
                    equipoId = evt.equipoId,
                    equipoNombre = equipo?.nombre ?: "Equipo",
                    jugadorId = evt.jugadorId,
                    jugadorNombre = nombreJugador,
                    jugadorDorsal = null,
                    tipoEvento = TipoEvento.fromString(evt.tipoEvento),
                    minuto = evt.minuto,
                    detalle = evt.detalle,
                    jugadorSaleId = evt.jugadorSaleId,
                    jugadorSaleNombre = nombreJugadorSale
                )
            }

            Result.success(listaUI)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener eventos del partido $partidoId", e)
            Result.failure(e)
        }
    }

    suspend fun guardarEvento(evento: EventoPartido): Result<Unit> {
        return try {
            supabase.from("eventos_partido").insert(evento)

            // Si el evento es un GOL, incrementar goles en partido_equipo
            if (evento.tipoEvento.equals("GOL", ignoreCase = true)) {
                actualizarGolesPartidoEquipo(evento.partidoId, evento.equipoId, deltaGoles = 1)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar evento de partido", e)
            Result.failure(e)
        }
    }

    suspend fun eliminarEvento(eventoId: String, partidoId: Long, equipoId: String, tipoEvento: String): Result<Unit> {
        return try {
            supabase.from("eventos_partido").delete {
                filter { eq("id", eventoId) }
            }

            // Si era un GOL, reducir en 1 los goles en partido_equipo
            if (tipoEvento.equals("GOL", ignoreCase = true)) {
                actualizarGolesPartidoEquipo(partidoId, equipoId, deltaGoles = -1)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar evento $eventoId", e)
            Result.failure(e)
        }
    }

    private suspend fun actualizarGolesPartidoEquipo(partidoId: Long, equipoId: String, deltaGoles: Int) {
        try {
            val relaciones = supabase.from("partido_equipo")
                .select {
                    filter {
                        eq("partido_id", partidoId)
                        eq("equipo_id", equipoId)
                    }
                }
                .decodeList<PartidoEquipo>()

            val relacionActual = relaciones.firstOrNull() ?: return
            val golesActuales = relacionActual.goles ?: 0
            val nuevosGoles = max(0, golesActuales + deltaGoles)

            supabase.from("partido_equipo").update({
                set("goles", nuevosGoles)
            }) {
                filter {
                    eq("partido_id", partidoId)
                    eq("equipo_id", equipoId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar marcador en partido_equipo", e)
        }
    }
}
