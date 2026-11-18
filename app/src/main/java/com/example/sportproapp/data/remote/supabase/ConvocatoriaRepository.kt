package com.example.sportproapp.data.remote.supabase

import android.util.Log
import com.example.sportproapp.data.model.Convocatoria
import com.example.sportproapp.data.remote.supabase
import io.github.jan.supabase.postgrest.from

object ConvocatoriaRepository {

    private const val TAG = "ConvocatoriaRepository"

    suspend fun getConvocatorias(partidoId: Long, equipoId: String): Result<List<Convocatoria>> {
        return try {
            val lista = supabase.from("convocatorias")
                .select {
                    filter {
                        eq("partido_id", partidoId)
                        eq("equipo_id", equipoId)
                    }
                }
                .decodeList<Convocatoria>()
            Result.success(lista)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener convocatorias", e)
            Result.failure(e)
        }
    }

    suspend fun getConvocatoriasParaPartido(partidoId: Long): Result<List<Convocatoria>> {
        return try {
            val lista = supabase.from("convocatorias")
                .select {
                    filter {
                        eq("partido_id", partidoId)
                    }
                }
                .decodeList<Convocatoria>()
            Result.success(lista)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener convocatorias del partido", e)
            Result.failure(e)
        }
    }

    suspend fun getConvocatoriasParaJugador(jugadorId: String): Result<List<Convocatoria>> {
        return try {
            val lista = supabase.from("convocatorias")
                .select {
                    filter {
                        eq("jugador_id", jugadorId)
                    }
                }
                .decodeList<Convocatoria>()
            Result.success(lista)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener convocatorias del jugador", e)
            Result.failure(e)
        }
    }

    suspend fun guardarConvocatorias(convocatorias: List<Convocatoria>): Result<Unit> {
        return try {
            if (convocatorias.isNotEmpty()) {
                supabase.from("convocatorias")
                    .upsert(convocatorias) {
                        onConflict = "partido_id,jugador_id"
                    }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar convocatorias", e)
            Result.failure(e)
        }
    }
}
