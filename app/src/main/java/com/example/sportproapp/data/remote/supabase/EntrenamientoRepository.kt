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

    // Obtener todos los ejercicios ya registrados en la academia
    suspend fun getEjerciciosPorAcademia(academiaId: String): Result<List<Ejercicio>> {
        return try {
            val ejercicios = supabase.from("ejercicios")
                .select {
                    filter {
                        eq("academia_id", academiaId)
                    }
                }
                .decodeList<Ejercicio>()
            Result.success(ejercicios)
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

    // Crear un nuevo ejercicio en la academia y vincularlo inmediatamente al entrenamiento
    suspend fun crearYAsociarEjercicio(
        entrenamientoId: String,
        ejercicio: Ejercicio
    ): Result<Unit> {
        return try {
            val nuevoEjercicio = supabase.from("ejercicios")
                .insert(ejercicio) {
                    select()
                }
                .decodeSingle<Ejercicio>()

            val nuevoId = nuevoEjercicio.id ?: return Result.failure(Exception("No se obtuvo el ID generado"))
            asociarEjercicioAEntrenamiento(entrenamientoId, nuevoId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar un ejercicio de una sesión (elimina la relación en entrenamiento_ejercicio)
    suspend fun desasociarEjercicioDeEntrenamiento(
        entrenamientoId: String,
        ejercicioId: String
    ): Result<Unit> {
        return try {
            supabase.from("entrenamiento_ejercicio").delete {
                filter {
                    eq("entrenamiento_id", entrenamientoId)
                    eq("ejercicio_id", ejercicioId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}