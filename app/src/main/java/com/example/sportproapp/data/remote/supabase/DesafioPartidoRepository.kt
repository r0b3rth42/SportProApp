package com.example.sportproapp.data.remote.supabase

import android.util.Log
import com.example.sportproapp.data.model.Campeonato
import com.example.sportproapp.data.model.CondicionEquipo
import com.example.sportproapp.data.model.DesafioPartido
import com.example.sportproapp.data.model.Equipo
import com.example.sportproapp.data.model.EstadoDesafio
import com.example.sportproapp.data.model.FaseCampeonato
import com.example.sportproapp.data.model.Partido
import com.example.sportproapp.data.model.PartidoConDetalles
import com.example.sportproapp.data.model.PartidoEquipo
import com.example.sportproapp.data.model.Sede
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

    // 3. Obtener fases de campeonato registradas
    suspend fun getFasesCampeonato(): Result<List<FaseCampeonato>> {
        return try {
            val fases = supabase.from("fase_campeonato")
                .select()
                .decodeList<FaseCampeonato>()
            Result.success(fases)
        } catch (e: Exception) {
            Log.e(TAG, "Error al listar fases de campeonato", e)
            Result.failure(e)
        }
    }

    // 4. Obtener o crear una fase de campeonato oficial por defecto
    suspend fun obtenerOCrearFaseOficialDefault(): Result<Long> {
        return try {
            val fasesExistentes = supabase.from("fase_campeonato")
                .select()
                .decodeList<FaseCampeonato>()

            if (fasesExistentes.isNotEmpty()) {
                val idFase = fasesExistentes.first().id
                if (idFase != null) return Result.success(idFase)
            }

            // Buscar o crear campeonato
            val campeonatosExistentes = supabase.from("campeonato")
                .select()
                .decodeList<Campeonato>()

            val campeonatoId = if (campeonatosExistentes.isNotEmpty() && campeonatosExistentes.first().id != null) {
                campeonatosExistentes.first().id!!
            } else {
                val nuevoCamp = buildJsonObject {
                    put("nombre", "Campeonato Oficial 2025")
                    put("temporada", "2025-I")
                    put("fecha_inicio", "2025-01-01")
                    put("fecha_fin", "2025-12-31")
                    put("estado", "EN_CURSO")
                }
                val campInsertado = supabase.from("campeonato")
                    .insert(nuevoCamp) { select() }
                    .decodeSingle<Campeonato>()
                campInsertado.id ?: 1L
            }

            // Crear fase por defecto
            val nuevaFase = buildJsonObject {
                put("campeonato_id", campeonatoId)
                put("nombre", "Fase Regular")
                put("tipo_fase", "LIGA")
                put("orden", 1)
                put("estado", "EN_CURSO")
            }

            val faseInsertada = supabase.from("fase_campeonato")
                .insert(nuevaFase) { select() }
                .decodeSingle<FaseCampeonato>()

            val idCreado = faseInsertada.id ?: throw Exception("No se pudo generar ID para la fase")
            Result.success(idCreado)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener u obtener fase oficial por defecto", e)
            Result.failure(e)
        }
    }

    // 5. Crear un partido directamente en la base de datos
    suspend fun crearPartidoDirecto(
        partido: Partido,
        equipoLocalId: String,
        equipoVisitanteId: String
    ): Result<Partido> {
        return try {
            Log.d(TAG, "Creando partido directo tipo '${partido.tipoPartido}' para la sede ${partido.sedeId}")

            var faseIdFinal = partido.faseId
            if (partido.tipoPartido == "OFICIAL" && faseIdFinal == null) {
                val resFase = obtenerOCrearFaseOficialDefault()
                faseIdFinal = resFase.getOrNull()
            }

            val insertJson = buildJsonObject {
                put("tipo_partido", partido.tipoPartido)
                put("sede_id", partido.sedeId)
                put("fecha", partido.fecha)
                put("hora", partido.hora)
                put("estado", partido.estado)
                faseIdFinal?.let { put("fase_id", it) }
                partido.desafioId?.let { put("desafio_id", it) }
            }

            val partidoInsertado = supabase.from("partido")
                .insert(insertJson) {
                    select()
                }
                .decodeSingle<Partido>()

            val idGenerado = partidoInsertado.id ?: throw Exception("No se pudo obtener el ID del partido generado")

            val relaciones = listOf(
                buildJsonObject {
                    put("partido_id", idGenerado)
                    put("equipo_id", equipoLocalId)
                    put("condicion", CondicionEquipo.LOCAL.name)
                },
                buildJsonObject {
                    put("partido_id", idGenerado)
                    put("equipo_id", equipoVisitanteId)
                    put("condicion", CondicionEquipo.VISITANTE.name)
                }
            )

            supabase.from("partido_equipo").insert(relaciones)

            Result.success(partidoInsertado)
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear partido directo", e)
            Result.failure(e)
        }
    }

    // 6. Aceptar o Rechazar el desafío
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

            if (nuevoEstado == EstadoDesafio.ACEPTADO) {
                // Al aceptar el desafío, se crea automáticamente el partido correspondiente
                val desafios = supabase.from("desafio_partido")
                    .select { filter { eq("id", desafioId) } }
                    .decodeList<DesafioPartido>()

                val desafio = desafios.firstOrNull()
                if (desafio != null) {
                    val receptorId = equipoReceptorId ?: desafio.equipoReceptorId
                    if (receptorId != null) {
                        val nuevoPartido = Partido(
                            tipoPartido = "AMISTOSO",
                            sedeId = desafio.sedeId,
                            fecha = desafio.fecha,
                            hora = desafio.hora,
                            estado = "PROGRAMADO",
                            desafioId = desafioId
                        )
                        crearPartidoDirecto(
                            partido = nuevoPartido,
                            equipoLocalId = desafio.equipoEmisorId,
                            equipoVisitanteId = receptorId
                        )
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar estado del desafío", e)
            Result.failure(e)
        }
    }

    // 7. Obtener partidos con información detallada completa de la Academia
    suspend fun getPartidosConDetallesPorAcademia(academiaId: String): Result<List<PartidoConDetalles>> {
        return try {
            // A. Obtener equipos pertenecientes a la academia
            val misEquipos = supabase.from("equipos")
                .select {
                    filter { eq("academia_id", academiaId) }
                }
                .decodeList<Equipo>()

            val misEquiposIds = misEquipos.map { it.id }
            if (misEquiposIds.isEmpty()) return Result.success(emptyList())

            // B. Buscar relaciones en partido_equipo
            val relacionesAcademia = supabase.from("partido_equipo")
                .select {
                    filter { isIn("equipo_id", misEquiposIds) }
                }
                .decodeList<PartidoEquipo>()

            val partidoIds = relacionesAcademia.map { it.partidoId }.distinct()
            if (partidoIds.isEmpty()) return Result.success(emptyList())

            // C. Obtener registros de partidos
            val listaPartidos = supabase.from("partido")
                .select {
                    filter { isIn("id", partidoIds) }
                }
                .decodeList<Partido>()

            // D. Obtener TODAS las relaciones de los partidos (Local y Visitante)
            val todasRelaciones = supabase.from("partido_equipo")
                .select {
                    filter { isIn("partido_id", partidoIds) }
                }
                .decodeList<PartidoEquipo>()

            // E. Obtener todos los equipos involucrados
            val todosEquipoIds = todasRelaciones.map { it.equipoId }.distinct()
            val todosEquiposMap = if (todosEquipoIds.isNotEmpty()) {
                supabase.from("equipos")
                    .select {
                        filter { isIn("id", todosEquipoIds) }
                    }
                    .decodeList<Equipo>()
                    .associateBy { it.id }
            } else emptyMap()

            // F. Obtener todas las sedes involucradas
            val todasSedeIds = listaPartidos.map { it.sedeId }.distinct()
            val todasSedesMap = if (todasSedeIds.isNotEmpty()) {
                supabase.from("sedes")
                    .select {
                        filter { isIn("id", todasSedeIds) }
                    }
                    .decodeList<Sede>()
                    .associateBy { it.id }
            } else emptyMap()

            // G. Obtener desafíos asociados si los hay
            val desafioIds = listaPartidos.mapNotNull { it.desafioId }.distinct()
            val desafiosMap = if (desafioIds.isNotEmpty()) {
                supabase.from("desafio_partido")
                    .select {
                        filter { isIn("id", desafioIds) }
                    }
                    .decodeList<DesafioPartido>()
                    .associateBy { it.id }
            } else emptyMap()

            // H. Mapear a PartidoConDetalles
            val resultadoDetallado = listaPartidos.map { partido ->
                val relLocal = todasRelaciones.firstOrNull { it.partidoId == partido.id && it.condicion == CondicionEquipo.LOCAL }
                val relVisitante = todasRelaciones.firstOrNull { it.partidoId == partido.id && it.condicion == CondicionEquipo.VISITANTE }

                val equipoLocal = relLocal?.let { todosEquiposMap[it.equipoId] }
                val equipoVisitante = relVisitante?.let { todosEquiposMap[it.equipoId] }
                val sede = todasSedesMap[partido.sedeId]
                val desafio = partido.desafioId?.let { desafiosMap[it] }

                PartidoConDetalles(
                    id = partido.id ?: 0,
                    tipoPartido = partido.tipoPartido,
                    fecha = partido.fecha,
                    hora = partido.hora,
                    estado = partido.estado,
                    sedeId = partido.sedeId,
                    sedeNombre = sede?.nombre ?: "Sede Principal",
                    sedeDireccion = sede?.direccion ?: "",
                    equipoLocalId = relLocal?.equipoId ?: "",
                    equipoLocalNombre = equipoLocal?.nombre ?: "Equipo Local",
                    equipoLocalCategoria = equipoLocal?.categoria ?: "",
                    golesLocal = relLocal?.goles,
                    equipoVisitanteId = relVisitante?.equipoId ?: "",
                    equipoVisitanteNombre = equipoVisitante?.nombre ?: "Equipo Visitante",
                    equipoVisitanteCategoria = equipoVisitante?.categoria ?: "",
                    golesVisitante = relVisitante?.goles,
                    desafioId = partido.desafioId,
                    mensajeDesafio = desafio?.mensaje
                )
            }

            Result.success(resultadoDetallado)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar partidos con detalles", e)
            Result.failure(e)
        }
    }

    // 8. Obtener partidos amistosos por un solo equipo
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

    // 9. Obtener únicamente partidos simples
    suspend fun getPartidosAmistososPorAcademia(academiaId: String): Result<List<Partido>> {
        return try {
            val equipos = supabase.from("equipos")
                .select {
                    filter { eq("academia_id", academiaId) }
                }
                .decodeList<Equipo>()

            val equiposIds = equipos.map { it.id }
            if (equiposIds.isEmpty()) return Result.success(emptyList())

            val relaciones = supabase.from("partido_equipo")
                .select {
                    filter { isIn("equipo_id", equiposIds) }
                }
                .decodeList<PartidoEquipo>()

            val partidoIds = relaciones.map { it.partidoId }.distinct()
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
            Log.e(TAG, "Error al cargar partidos amistosos de la academia", e)
            Result.failure(e)
        }
    }
}