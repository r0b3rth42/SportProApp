package com.example.sportproapp.data.remote.supabase

import com.example.sportproapp.data.model.Asistencia
import com.example.sportproapp.data.model.JugadorConUsuario
import com.example.sportproapp.data.model.JugadorPerfil
import com.example.sportproapp.data.remote.supabase
import io.github.jan.supabase.postgrest.from

object AsistenciaRepository {

    // Obtiene los jugadores del equipo y sus asistencias previas si ya existen
    suspend fun getJugadoresYAsistencias(
        entrenamientoId: String,
        equipoId: String
    ): Result<Pair<List<JugadorConUsuario>, List<Asistencia>>> {
        return try {
            val jugadores = UsuarioRepository.getJugadoresConEquipoConUsuario(equipoId).getOrThrow()


            val asistencias = supabase.from("asistencias")
                .select {
                    filter { eq("entrenamiento_id", entrenamientoId) }
                }
                .decodeList<Asistencia>()

            Result.success(Pair(jugadores, asistencias))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Guarda o actualiza masivamente las asistencias en Supabase
    suspend fun guardarAsistencias(asistencias: List<Asistencia>): Result<Unit> {
        return try {
            supabase.from("asistencias").upsert(asistencias)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}