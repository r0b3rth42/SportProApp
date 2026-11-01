package com.example.sportproapp.data.remote.supabase

import com.example.sportproapp.data.model.Ejercicio
import com.example.sportproapp.data.model.EntrenamientoEjercicioRelation
import com.example.sportproapp.data.remote.supabase
import com.example.sportproapp.presentation.entrenamiento.Entrenamiento
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

object EntrenamientoRepository {


    suspend fun saveEntrenamiento(entrenamiento: Entrenamiento): Result<Unit> {
        return try {
            supabase.from("entrenamientos").insert(entrenamiento)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEntrenamientosPorEquipo(equipoId: String): Result<List<Entrenamiento>> {
        return try {
            val lista = supabase.from("entrenamientos")
                .select {
                    filter {
                        eq("equipo_id", equipoId)
                    }
                }
                .decodeList<Entrenamiento>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener los ejercicios asociados a un entrenamiento a través de la tabla de unión
    suspend fun getEjerciciosPorEntrenamiento(entrenamientoId: String): Result<List<Ejercicio>> {
        return try {
            val ejercicios = supabase.from("ejercicios")
                .select(Columns.raw("*, entrenamiento_ejercicio!inner(entrenamiento_id)")) {
                    filter {
                        eq("entrenamiento_ejercicio.entrenamiento_id", entrenamientoId)
                    }
                }
                .decodeList<Ejercicio>()
            Result.success(ejercicios.sortedBy { it.orden })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Vincular un ejercicio existente de la academia al entrenamiento actual
    suspend fun asociarEjercicioAEntrenamiento(
        entrenamientoId: String,
        ejercicioId: String
    ): Result<Unit> {
        return try {
            supabase.from("entrenamiento_ejercicio").insert(
                EntrenamientoEjercicioRelation(
                    entrenamientoId = entrenamientoId,
                    ejercicioId = ejercicioId
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}