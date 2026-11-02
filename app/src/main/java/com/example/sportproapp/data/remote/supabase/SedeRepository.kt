package com.example.sportproapp.data.remote.supabase

import com.example.sportproapp.data.model.Sede
import com.example.sportproapp.data.remote.supabase
import io.github.jan.supabase.postgrest.from

object SedeRepository {

    suspend fun getSedes(academiaId: String): Result<List<Sede>> {
        return try {
            val sedes = supabase.from("sedes")
                .select {
                    filter {
                        eq("academia_id", academiaId)
                    }
                }
                .decodeList<Sede>()
            Result.success(sedes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}