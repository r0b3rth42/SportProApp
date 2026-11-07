package com.example.sportproapp.data.remote.supabase

import android.util.Log
import com.example.sportproapp.data.model.Notificacion
import com.example.sportproapp.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object NotificacionRepository {
    private const val TAG = "NotificacionRepository"

    // 0. Registrar varios avisos a la vez (por ejemplo, al crear las mensualidades de un equipo)
    suspend fun crearNotificaciones(notificaciones: List<Notificacion>): Result<Unit> {
        if (notificaciones.isEmpty()) return Result.success(Unit)
        return try {
            supabase.from("notificaciones").insert(notificaciones)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear notificaciones", e)
            Result.failure(e)
        }
    }

    // 1. Avisos del usuario, el más reciente primero (2.05)
    suspend fun getNotificaciones(usuarioId: String): Result<List<Notificacion>> {
        return try {
            val lista = supabase.from("notificaciones")
                .select {
                    filter { eq("usuario_id", usuarioId) }
                }
                .decodeList<Notificacion>()
            Result.success(lista.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Log.e(TAG, "Error al listar notificaciones", e)
            Result.failure(e)
        }
    }

    // 2. Cantidad de avisos sin leer, para la campana del Home
    suspend fun contarNoLeidas(usuarioId: String): Result<Int> {
        return try {
            val lista = supabase.from("notificaciones")
                .select(Columns.list("id")) {
                    filter {
                        eq("usuario_id", usuarioId)
                        eq("leida", false)
                    }
                }
                .decodeList<Map<String, String>>()
            Result.success(lista.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. Marcar un aviso como leído
    suspend fun marcarComoLeida(notificacionId: String): Result<Unit> {
        return try {
            supabase.from("notificaciones").update(buildJsonObject { put("leida", true) }) {
                filter { eq("id", notificacionId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. Marcar todos los avisos del usuario como leídos
    suspend fun marcarTodasComoLeidas(usuarioId: String): Result<Unit> {
        return try {
            supabase.from("notificaciones").update(buildJsonObject { put("leida", true) }) {
                filter {
                    eq("usuario_id", usuarioId)
                    eq("leida", false)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al marcar notificaciones como leídas", e)
            Result.failure(e)
        }
    }
}
