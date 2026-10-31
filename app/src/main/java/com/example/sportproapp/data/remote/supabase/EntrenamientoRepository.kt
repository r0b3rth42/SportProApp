package com.example.sportproapp.data.remote.supabase

import com.example.sportproapp.data.model.Equipo
import com.example.sportproapp.data.remote.supabase
import com.example.sportproapp.presentation.entrenamiento.Entrenamiento
import io.github.jan.supabase.postgrest.from

object EntrenamientoRepository {


    suspend fun saveEntrenamiento(entrenamiento: Entrenamiento): Result<Unit> {
        return try {
            supabase.from("entrenamientos").insert(entrenamiento)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEntrenamientosPorEquipo(academiaId: String): Result<List<Entrenamiento>> {
        return try {
            val lista = supabase.from("entrenamientos")
                .select {
                    filter {
                        eq("equipo_id", academiaId)
                    }
                }
                .decodeList<Entrenamiento>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}