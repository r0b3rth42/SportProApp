package com.example.sportproapp.data.remote.supabase

import com.example.sportproapp.data.model.JugadorPerfil
import com.example.sportproapp.data.model.Usuarios
import com.example.sportproapp.data.remote.supabase
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.tasks.await

object UsuarioRepository {

    private val auth = FirebaseAuth.getInstance()

    suspend fun registerUser(name: String, email: String, password: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("User ID es nulo")

            val nuevoUsuario = Usuarios(
                id = uid,
                name = name,
                email = email,
                phone = "999999999",
                role = "PENDIENTE" // Se asignará en la pantalla de rol
            )
            supabase.from("usuarios").insert(nuevoUsuario)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- NUEVA FUNCIÓN PARA ACTUALIZAR EL ROL SELECCIONADO ---
    suspend fun updateUserRole(userId: String, roleCode: String): Result<Unit> {
        return try {
            supabase.from("usuarios").update({
                set("role", roleCode) // Ajusta "role" o "rol" según el campo exacto en tu tabla de Supabase
            }) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- NUEVA FUNCIÓN PARA CONSULTAR EL ROL DEL USUARIO ---
    suspend fun getUser(userId: String): Result<Usuarios> {
        return try {
            val usuario = supabase.from("usuarios")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<Usuarios>()

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Agrega esta función dentro de tu UsuarioRepository.kt

    suspend fun savePlayerProfile(perfil: JugadorPerfil): Result<Unit> {
        return try {
            supabase.from("jugadores").insert(perfil)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Agrega esta función dentro de UsuarioRepository.kt

    suspend fun updateLinkingCode(userId: String, code: String): Result<Unit> {
        return try {
            supabase.from("jugadores").update({
                set("codigo_vinculacion", code)
                set("estado_vinculacion", "PENDIENTE")
            }) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Agrega esta función dentro de UsuarioRepository.kt

    suspend fun getPlayerProfile(userId: String): Result<JugadorPerfil> {
        return try {
            val perfil = supabase.from("jugadores")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<JugadorPerfil>()
            Result.success(perfil)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}