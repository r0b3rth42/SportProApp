package com.example.sportproapp.data.remote.supabase

import com.example.sportproapp.data.model.Equipo
import com.example.sportproapp.data.remote.supabase
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.postgrest.from

object EquipoRepository {

    private val auth = FirebaseAuth.getInstance()

    suspend fun saveEquipo(equipo: Equipo): Result<Unit> {
        return try {
            supabase.from("equipos").insert(equipo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEquipos(): Result<List<Equipo>> {
        return try {
            val lista = supabase.from("equipos")
                .select()
                .decodeList<Equipo>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}