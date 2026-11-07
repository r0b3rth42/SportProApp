package com.example.sportproapp.data.remote.supabase

import android.util.Log
import com.example.sportproapp.data.model.DesafioPartido
import com.example.sportproapp.data.model.Equipo
import com.example.sportproapp.data.model.EstadoDesafio
import com.example.sportproapp.data.model.Partido
import com.example.sportproapp.data.model.PartidoEquipo
import com.example.sportproapp.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object DesafioPartidoRepository {
    private const val TAG = "DesafioRepository"

    // 1. Enviar un nuevo reto a otra academia
    suspend fun enviarDesafio(desafio: DesafioPartido): Result<Unit> {
        return try {
            Log.d(TAG, "Creando desafío amistoso para academia receptor: ${desafio.academiaReceptorId}")
            supabase.from("desafio_partido").insert(desafio)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al emitir desafío", e)
            Result.failure(e)
        }
    }

    // 2. Obtener retos recibidos por la academia actual
    suspend fun getDesafiosRecibidos(academiaId: String): Result<List<DesafioPartido>> {
        return try {
            val lista = supabase.from("desafio_partido")
                .select {
                    filter { eq("academia_receptor_id", academiaId) }
                }
                .decodeList<DesafioPartido>()
            Result.success(lista)
        } catch (e: Exception) {
            Log.e(TAG, "Error al listar desafíos recibidos", e)
            Result.failure(e)
        }
    }

    // 3. Aceptar o Rechazar el desafío
    suspend fun responderDesafio(
        desafioId: Long,
        nuevoEstado: EstadoDesafio,
        equipoReceptorId: String? = null
    ): Result<Unit> {
        return try {
            Log.d(TAG, "Respondiendo desafío $desafioId -> Nuevo estado: $nuevoEstado")

            val updates = buildJsonObject {
                put("estado", nuevoEstado.name)
                equipoReceptorId?.let { put("equipo_receptor_id", it) }
            }

            supabase.from("desafio_partido").update(updates) {
                filter { eq("id", desafioId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar estado del desafío", e)
            Result.failure(e)
        }
    }

    // 4. Obtener partidos amistosos filtrados por un solo equipo
    suspend fun getPartidosAmistososPorEquipo(equipoId: String): Result<List<Partido>> {
        return try {
            val relaciones = supabase.from("partido_equipo")
                .select {
                    filter { eq("equipo_id", equipoId) }
                }
                .decodeList<PartidoEquipo>()

            val partidoIds = relaciones.map { it.partidoId }

            if (partidoIds.isEmpty()) return Result.success(emptyList())

            val partidos = supabase.from("partido")
                .select {
                    filter {
                        isIn("id", partidoIds)
                        eq("tipo_partido", "AMISTOSO")
                    }
                }
                .decodeList<Partido>()

            Result.success(partidos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 5. Obtener únicamente los partidos AMISTOSOS programados de toda la Academia
    suspend fun getPartidosAmistososPorAcademia(academiaId: String): Result<List<Partido>> {
        return try {
            // A. Obtener IDs de equipos pertenecientes a la academia
            val equipos = supabase.from("equipos")
                .select {
                    filter { eq("academia_id", academiaId) }
                }
                .decodeList<Equipo>()

            val equiposIds = equipos.map { it.id }
            if (equiposIds.isEmpty()) return Result.success(emptyList())

            // B. Buscar IDs de partidos vinculados en partido_equipo
            val relaciones = supabase.from("partido_equipo")
                .select {
                    filter { isIn("equipo_id", equiposIds) }
                }
                .decodeList<PartidoEquipo>()

            val partidoIds = relaciones.map { it.partidoId }.distinct()
            if (partidoIds.isEmpty()) return Result.success(emptyList())

            // C. Obtener únicamente partidos con tipo_partido = 'AMISTOSO'
            val partidos = supabase.from("partido")
                .select {
                    filter {
                        isIn("id", partidoIds)
                        eq("tipo_partido", "AMISTOSO")
                    }
                }
                .decodeList<Partido>()

            Result.success(partidos)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar partidos amistosos de la academia", e)
            Result.failure(e)
        }
    }
}