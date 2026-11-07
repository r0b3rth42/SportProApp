package com.example.sportproapp.data.remote.supabase

import com.example.sportproapp.data.model.Academia
import com.example.sportproapp.data.remote.supabase
import io.github.jan.supabase.postgrest.from

object AcademiaRepository {

    suspend fun getAcademias(): Result<List<Academia>> {
        return try {
            val lista = supabase.from("academias")
                .select()
                .decodeList<Academia>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}