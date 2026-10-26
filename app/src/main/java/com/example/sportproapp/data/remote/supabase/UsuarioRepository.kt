package com.example.sportproapp.data.remote.supabase

import PadrePerfil
import com.example.sportproapp.data.model.JugadorConUsuario
import com.example.sportproapp.data.model.JugadorPerfil
import com.example.sportproapp.data.model.PadreConUsuario
import com.example.sportproapp.data.model.Usuarios
import com.example.sportproapp.data.remote.supabase
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
                role = "PENDIENTE"
            )
            supabase.from("usuarios").insert(nuevoUsuario)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserRole(userId: String, roleCode: String): Result<Unit> {
        return try {
            supabase.from("usuarios").update({
                set("role", roleCode)
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

    suspend fun savePlayerProfile(perfil: JugadorPerfil): Result<Unit> {
        return try {
            supabase.from("jugadores").insert(perfil)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    // --- OBTENER JUGADOR CON DATOS BASE DE USUARIO (Nombre, Teléfono, Email) ---
    suspend fun getJugadorConUsuario(userId: String): Result<JugadorConUsuario> = coroutineScope {
        try {
            val usuarioDeferred = async { getUser(userId).getOrThrow() }
            val perfilDeferred = async { getPlayerProfile(userId).getOrThrow() }

            val usuario = usuarioDeferred.await()
            val perfil = perfilDeferred.await()

            Result.success(JugadorConUsuario(usuario = usuario, perfil = perfil))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun vincularJugadorPorCodigo(idPadre: String, codigoIngresado: String): Result<String> {
        return try {
            val codigoLimpio = codigoIngresado.trim().uppercase()

            val jugadores = supabase.from("jugadores")
                .select {
                    filter {
                        eq("codigo_vinculacion", codigoLimpio)
                    }
                }
                .decodeList<JugadorPerfil>()

            val jugador = jugadores.firstOrNull()
                ?: return Result.failure(Exception("El código ingresado no existe o es incorrecto."))

            supabase.from("jugadores").update({
                set("apoderado_id", idPadre)
                set("estado_vinculacion", "VINCULADO")
            }) {
                filter {
                    eq("id", jugador.id)
                }
            }

            Result.success(jugador.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMisRepresentados(idPadre: String): Result<List<JugadorPerfil>> {
        return try {
            val lista = supabase.from("jugadores")
                .select {
                    filter {
                        eq("apoderado_id", idPadre)
                    }
                }
                .decodeList<JugadorPerfil>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- OBTENER LISTA DE REPRESENTADOS CON SUS DATOS PERSONALES DE USUARIO ---
    suspend fun getMisRepresentadosConUsuario(idPadre: String): Result<List<JugadorConUsuario>> {
        return try {
            val representados = getMisRepresentados(idPadre).getOrThrow()
            val listaCompleta = representados.mapNotNull { perfilJugador ->
                val usuarioData = getUser(perfilJugador.id).getOrNull()
                if (usuarioData != null) {
                    JugadorConUsuario(usuario = usuarioData, perfil = perfilJugador)
                } else null
            }
            Result.success(listaCompleta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveParentProfile(idPadre: String, direccion: String): Result<Unit> {
        return try {
            supabase.from("apoderados").upsert(
                PadrePerfil(
                    id = idPadre,
                    direccion = direccion.trim()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getParentProfile(idPadre: String): Result<PadrePerfil> {
        return try {
            val perfil = supabase.from("apoderados")
                .select {
                    filter {
                        eq("id", idPadre)
                    }
                }
                .decodeSingle<PadrePerfil>()
            Result.success(perfil)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- OBTENER APODERADO CON DATOS BASE DE USUARIO (Nombre, Teléfono, Dirección) ---
    suspend fun getPadreConUsuario(idPadre: String): Result<PadreConUsuario> = coroutineScope {
        try {
            val usuarioDeferred = async { getUser(idPadre).getOrThrow() }
            val perfilDeferred = async { getParentProfile(idPadre).getOrThrow() }

            val usuario = usuarioDeferred.await()
            val perfil = perfilDeferred.await()

            Result.success(PadreConUsuario(usuario = usuario, perfil = perfil))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}