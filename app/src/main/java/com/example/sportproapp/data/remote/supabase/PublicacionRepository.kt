package com.example.sportproapp.data.remote.supabase

import android.util.Log
import com.example.sportproapp.data.model.ComentarioConAutor
import com.example.sportproapp.data.model.ComentarioLike
import com.example.sportproapp.data.model.ComentarioLikeInsert
import com.example.sportproapp.data.model.Equipo
import com.example.sportproapp.data.model.MiembroComunidad
import com.example.sportproapp.data.model.Publicacion
import com.example.sportproapp.data.model.PublicacionComentario
import com.example.sportproapp.data.model.PublicacionComentarioInsert
import com.example.sportproapp.data.model.PublicacionConAutor
import com.example.sportproapp.data.model.PublicacionInsert
import com.example.sportproapp.data.model.PublicacionLike
import com.example.sportproapp.data.model.PublicacionLikeInsert
import com.example.sportproapp.data.model.Usuarios
import com.example.sportproapp.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object PublicacionRepository {

    private const val TAG = "PublicacionRepository"

    // --- OBTENER ACADEMIA_ID DEL USUARIO DE FORMA ROBUSTA ---
    suspend fun obtenerAcademiaIdDelUsuario(userId: String): String {
        return try {
            // 1. Probar campo directo academia_id en la tabla usuarios
            val usuario = UsuarioRepository.getUser(userId).getOrNull()
            if (usuario?.academiaId != null && usuario.academiaId.isNotBlank()) {
                return usuario.academiaId
            }

            // 2. Probar si es Admin
            val adminPerfil = UsuarioRepository.getAdminProfile(userId).getOrNull()
            if (adminPerfil != null && adminPerfil.academiaId.isNotBlank()) {
                return adminPerfil.academiaId
            }

            // 3. Probar si es Jugador (obtener equipo -> academia_id)
            val jugadorPerfil = UsuarioRepository.getPlayerProfile(userId).getOrNull()
            if (jugadorPerfil != null && !jugadorPerfil.equipoId.isNullOrBlank()) {
                val equipos = supabase.from("equipos").select {
                    filter { eq("id", jugadorPerfil.equipoId) }
                }.decodeList<Equipo>()
                val acadId = equipos.firstOrNull()?.academiaId
                if (!acadId.isNullOrBlank()) return acadId
            }

            // 4. Fallback: Obtener la primera academia registrada
            val academias = UsuarioRepository.getAcademias().getOrNull()
            academias?.firstOrNull()?.id ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error resolviendo academiaId: ${e.message}", e)
            val academias = UsuarioRepository.getAcademias().getOrNull()
            academias?.firstOrNull()?.id ?: ""
        }
    }

    // --- OBTENER EL NOMBRE DE LA ACADEMIA PARA LA CABECERA ---
    suspend fun obtenerNombreAcademia(academiaId: String): String {
        return try {
            val academias = supabase.from("academias").select {
                filter { eq("id", academiaId) }
            }.decodeList<com.example.sportproapp.data.model.Academia>()
            academias.firstOrNull()?.nombre?.ifBlank { "Mi Club" } ?: "Mi Club"
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener nombre de academia: ${e.message}", e)
            "Mi Club"
        }
    }

    // --- OBTENER ÚNICAMENTE PUBLICACIONES APROBADAS PARA EL MURO PÚBLICO ---
    suspend fun getPublicacionesAprobadas(
        academiaId: String,
        usuarioActualId: String,
        equipoId: String? = null,
        filtro: String = "Todos"
    ): Result<List<PublicacionConAutor>> = coroutineScope {
        try {
            val query = supabase.from("publicaciones")
                .select {
                    filter {
                        eq("academia_id", academiaId)
                        eq("estado", "APROBADO") // ESTRICTAMENTE SOLO APROBADAS
                        when (filtro) {
                            "Mi equipo" -> {
                                if (!equipoId.isNullOrBlank()) {
                                    eq("equipo_id", equipoId)
                                } else {
                                    eq("tipo", "MI_EQUIPO")
                                }
                            }
                            "Anuncios" -> eq("tipo", "ANUNCIO")
                        }
                    }
                }

            val publicaciones = query.decodeList<Publicacion>()
            val ordenadas = publicaciones.sortedByDescending { it.createdAt }

            val listaFinal = ordenadas.map { pub ->
                val autorRes = UsuarioRepository.getUser(pub.autorId).getOrNull()
                val isLiked = checkIsLikedByUser(pub.id ?: "", usuarioActualId)

                PublicacionConAutor(
                    publicacion = pub,
                    autorNombre = autorRes?.name?.ifBlank { "Usuario" } ?: "Usuario",
                    autorRol = mapRolToNombre(autorRes?.role ?: "JUG"),
                    autorAvatarUrl = null,
                    isLikedByCurrentUser = isLiked,
                    tiempoRelativo = calcularTiempoRelativo(pub.createdAt),
                    fechaHoraFormateada = formatearFechaHoraCompleta(pub.updatedAt ?: pub.createdAt)
                )
            }

            Result.success(listaFinal)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener publicaciones aprobadas: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- OBTENER PUBLICACIONES PENDIENTES DE APROBACIÓN (EXCLUSIVO PARA ROL ADM) ---
    suspend fun getPublicacionesPendientes(academiaId: String): Result<List<PublicacionConAutor>> {
        return try {
            val publicaciones = supabase.from("publicaciones")
                .select {
                    filter {
                        eq("academia_id", academiaId)
                        eq("estado", "PENDIENTE")
                    }
                }
                .decodeList<Publicacion>()

            val listaFinal = publicaciones.map { pub ->
                val autorRes = UsuarioRepository.getUser(pub.autorId).getOrNull()
                PublicacionConAutor(
                    publicacion = pub,
                    autorNombre = autorRes?.name?.ifBlank { "Usuario" } ?: "Usuario",
                    autorRol = mapRolToNombre(autorRes?.role ?: "JUG"),
                    tiempoRelativo = calcularTiempoRelativo(pub.createdAt),
                    fechaHoraFormateada = formatearFechaHoraCompleta(pub.updatedAt ?: pub.createdAt)
                )
            }
            Result.success(listaFinal)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener publicaciones pendientes: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- OBTENER PUBLICACIONES DEL PROPIO USUARIO CON CAMBIOS SOLICITADOS ---
    suspend fun getMisPublicacionesConCambiosSolicitados(
        academiaId: String,
        usuarioId: String
    ): Result<List<PublicacionConAutor>> {
        return try {
            val publicaciones = supabase.from("publicaciones")
                .select {
                    filter {
                        eq("academia_id", academiaId)
                        eq("autor_id", usuarioId)
                        eq("estado", "CAMBIOS_SOLICITADOS")
                    }
                }
                .decodeList<Publicacion>()

            val listaFinal = publicaciones.map { pub ->
                val autorRes = UsuarioRepository.getUser(pub.autorId).getOrNull()
                PublicacionConAutor(
                    publicacion = pub,
                    autorNombre = autorRes?.name?.ifBlank { "Usuario" } ?: "Usuario",
                    autorRol = mapRolToNombre(autorRes?.role ?: "JUG"),
                    tiempoRelativo = calcularTiempoRelativo(pub.createdAt),
                    fechaHoraFormateada = formatearFechaHoraCompleta(pub.updatedAt ?: pub.createdAt)
                )
            }
            Result.success(listaFinal)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener mis publicaciones con cambios solicitados: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- OBTENER PUBLICACIÓN POR ID ---
    suspend fun getPublicacionPorId(
        publicacionId: String,
        usuarioActualId: String
    ): Result<PublicacionConAutor> {
        return try {
            val pub = supabase.from("publicaciones")
                .select {
                    filter {
                        eq("id", publicacionId)
                    }
                }
                .decodeSingle<Publicacion>()

            val autorRes = UsuarioRepository.getUser(pub.autorId).getOrNull()
            val isLiked = checkIsLikedByUser(pub.id ?: "", usuarioActualId)

            val pubConAutor = PublicacionConAutor(
                publicacion = pub,
                autorNombre = autorRes?.name?.ifBlank { "Usuario" } ?: "Usuario",
                autorRol = mapRolToNombre(autorRes?.role ?: "JUG"),
                isLikedByCurrentUser = isLiked,
                tiempoRelativo = calcularTiempoRelativo(pub.createdAt),
                fechaHoraFormateada = formatearFechaHoraCompleta(pub.updatedAt ?: pub.createdAt)
            )

            Result.success(pubConAutor)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener publicación $publicacionId: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- CREAR NUEVA PUBLICACIÓN (NACE EN ESTADO PENDIENTE - VERSIÓN 1) ---
    suspend fun crearPublicacion(
        academiaId: String,
        autorId: String,
        contenido: String,
        tipo: String = "GENERAL",
        equipoId: String? = null,
        imagenUrl: String? = null
    ): Result<Unit> {
        return try {
            val pubInsert = PublicacionInsert(
                academiaId = academiaId,
                autorId = autorId,
                contenido = contenido,
                tipo = tipo,
                equipoId = equipoId,
                imagenUrl = imagenUrl,
                estado = "PENDIENTE",
                versionInfo = "Versión 1"
            )

            supabase.from("publicaciones").insert(pubInsert)
            Log.d(TAG, "Publicación creada en estado PENDIENTE")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error crítico al insertar publicación en Supabase: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- EDITAR Y REENVIAR PUBLICACIÓN (INCREMENTA VERSIÓN Y PASA A PENDIENTE) ---
    suspend fun editarYReenviarPublicacion(
        publicacionId: String,
        nuevoContenido: String
    ): Result<Unit> {
        return try {
            val currentPub = supabase.from("publicaciones").select {
                filter { eq("id", publicacionId) }
            }.decodeSingle<Publicacion>()

            val verActual = currentPub.versionInfo?.filter { it.isDigit() }?.toIntOrNull() ?: 1
            val siguienteVersion = "Versión ${verActual + 1}"

            supabase.from("publicaciones").update({
                set("estado", "PENDIENTE")
                set("contenido", nuevoContenido)
                set("version_info", siguienteVersion)
            }) {
                filter {
                    eq("id", publicacionId)
                }
            }
            Log.d(TAG, "Publicación actualizada a $siguienteVersion y re-enviada a revisión")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al editar y reenviar publicación: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- APROBAR Y PUBLICAR PUBLICACIÓN (EXCLUSIVO ADMIN) ---
    suspend fun aprobarYPublicar(
        publicacionId: String,
        textoOficial: String,
        notasInternas: String? = null
    ): Result<Unit> {
        return try {
            supabase.from("publicaciones").update({
                set("estado", "APROBADO")
                set("contenido", textoOficial)
                if (notasInternas != null) set("notas_internas", notasInternas)
            }) {
                filter {
                    eq("id", publicacionId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al aprobar publicación: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- SOLICITAR CAMBIOS (EXCLUSIVO ADMIN) ---
    suspend fun solicitarCambios(
        publicacionId: String,
        textoOficial: String,
        notasInternas: String
    ): Result<Unit> {
        return try {
            supabase.from("publicaciones").update({
                set("estado", "CAMBIOS_SOLICITADOS")
                set("contenido", textoOficial)
                set("notas_internas", notasInternas)
            }) {
                filter {
                    eq("id", publicacionId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al solicitar cambios: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- RECHAZAR PUBLICACIÓN (EXCLUSIVO ADMIN) ---
    suspend fun rechazarPublicacion(
        publicacionId: String,
        notasInternas: String? = null
    ): Result<Unit> {
        return try {
            supabase.from("publicaciones").update({
                set("estado", "RECHAZADO")
                if (notasInternas != null) set("notas_internas", notasInternas)
            }) {
                filter {
                    eq("id", publicacionId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al rechazar publicación: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- MANEJO DE LIKES EN PUBLICACIONES ---
    private suspend fun checkIsLikedByUser(publicacionId: String, usuarioId: String): Boolean {
        return try {
            val likes = supabase.from("publicacion_likes")
                .select {
                    filter {
                        eq("publicacion_id", publicacionId)
                        eq("usuario_id", usuarioId)
                    }
                }
                .decodeList<PublicacionLike>()
            likes.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleLikePublicacion(publicacionId: String, usuarioId: String): Result<Boolean> {
        return try {
            val isLiked = checkIsLikedByUser(publicacionId, usuarioId)
            if (isLiked) {
                supabase.from("publicacion_likes").delete {
                    filter {
                        eq("publicacion_id", publicacionId)
                        eq("usuario_id", usuarioId)
                    }
                }
                val countRes = supabase.from("publicacion_likes").select {
                    filter { eq("publicacion_id", publicacionId) }
                }.decodeList<PublicacionLike>()

                supabase.from("publicaciones").update({
                    set("likes_count", countRes.size)
                }) {
                    filter { eq("id", publicacionId) }
                }
                Result.success(false)
            } else {
                val nuevoLike = PublicacionLikeInsert(
                    publicacionId = publicacionId,
                    usuarioId = usuarioId
                )
                supabase.from("publicacion_likes").insert(nuevoLike)

                val countRes = supabase.from("publicacion_likes").select {
                    filter { eq("publicacion_id", publicacionId) }
                }.decodeList<PublicacionLike>()

                supabase.from("publicaciones").update({
                    set("likes_count", countRes.size)
                }) {
                    filter { eq("id", publicacionId) }
                }
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en toggleLikePublicacion: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- COMENTARIOS Y RESPUESTAS ---
    suspend fun getComentariosConAutor(
        publicacionId: String,
        usuarioActualId: String
    ): Result<List<ComentarioConAutor>> {
        return try {
            val comentariosRaw = supabase.from("publicacion_comentarios")
                .select {
                    filter {
                        eq("publicacion_id", publicacionId)
                    }
                }
                .decodeList<PublicacionComentario>()

            val todosConAutor = comentariosRaw.map { com ->
                val autorRes = UsuarioRepository.getUser(com.autorId).getOrNull()
                val isLiked = checkComentarioIsLikedByUser(com.id ?: "", usuarioActualId)
                ComentarioConAutor(
                    comentario = com,
                    autorNombre = autorRes?.name?.ifBlank { "Usuario" } ?: "Usuario",
                    autorRol = mapRolToNombre(autorRes?.role ?: "JUG"),
                    isLikedByCurrentUser = isLiked,
                    tiempoRelativo = calcularTiempoRelativo(com.createdAt)
                )
            }

            val comentariosPadre = todosConAutor.filter { it.comentario.comentarioPadreId == null }
            val comentariosFinales = comentariosPadre.map { padre ->
                val respuestas = todosConAutor.filter { it.comentario.comentarioPadreId == padre.comentario.id }
                    .sortedBy { it.comentario.createdAt }
                padre.copy(respuestas = respuestas)
            }.sortedBy { it.comentario.createdAt }

            Result.success(comentariosFinales)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener comentarios: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun agregarComentario(
        publicacionId: String,
        autorId: String,
        contenido: String,
        comentarioPadreId: String? = null
    ): Result<Unit> {
        return try {
            val comentarioInsert = PublicacionComentarioInsert(
                publicacionId = publicacionId,
                autorId = autorId,
                comentarioPadreId = comentarioPadreId,
                contenido = contenido
            )
            supabase.from("publicacion_comentarios").insert(comentarioInsert)

            val countRes = supabase.from("publicacion_comentarios").select {
                filter { eq("publicacion_id", publicacionId) }
            }.decodeList<PublicacionComentario>()

            supabase.from("publicaciones").update({
                set("comments_count", countRes.size)
            }) {
                filter { eq("id", publicacionId) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al agregar comentario: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun checkComentarioIsLikedByUser(comentarioId: String, usuarioId: String): Boolean {
        return try {
            val likes = supabase.from("comentario_likes")
                .select {
                    filter {
                        eq("comentario_id", comentarioId)
                        eq("usuario_id", usuarioId)
                    }
                }
                .decodeList<ComentarioLike>()
            likes.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleLikeComentario(comentarioId: String, usuarioId: String): Result<Boolean> {
        return try {
            val isLiked = checkComentarioIsLikedByUser(comentarioId, usuarioId)
            if (isLiked) {
                supabase.from("comentario_likes").delete {
                    filter {
                        eq("comentario_id", comentarioId)
                        eq("usuario_id", usuarioId)
                    }
                }
                val countRes = supabase.from("comentario_likes").select {
                    filter { eq("comentario_id", comentarioId) }
                }.decodeList<ComentarioLike>()

                supabase.from("publicacion_comentarios").update({
                    set("likes_count", countRes.size)
                }) {
                    filter { eq("id", comentarioId) }
                }
                Result.success(false)
            } else {
                val nuevoLike = ComentarioLikeInsert(
                    comentarioId = comentarioId,
                    usuarioId = usuarioId
                )
                supabase.from("comentario_likes").insert(nuevoLike)

                val countRes = supabase.from("comentario_likes").select {
                    filter { eq("comentario_id", comentarioId) }
                }.decodeList<ComentarioLike>()

                supabase.from("publicacion_comentarios").update({
                    set("likes_count", countRes.size)
                }) {
                    filter { eq("id", comentarioId) }
                }
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en toggleLikeComentario: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- OBTENER LOS ÚLTIMOS 5 AUTORES DE PUBLICACIONES CON SU AVATAR Y NOMBRE CORTO (MÁX 10 CARACTERES) ---
    suspend fun getMiembrosComunidad(academiaId: String, usuarioActualId: String): Result<List<MiembroComunidad>> {
        return try {
            // 1. Consultar publicaciones de la academia ordenadas por fecha reciente
            val publicaciones = supabase.from("publicaciones")
                .select {
                    filter {
                        eq("academia_id", academiaId)
                    }
                }
                .decodeList<Publicacion>()
                .sortedByDescending { it.createdAt }

            // 2. Extraer hasta 5 IDs únicos de los últimos autores
            val autorIdsUnicos = publicaciones
                .map { it.autorId }
                .distinct()
                .take(5)

            val autorIdsFinales = if (autorIdsUnicos.isEmpty() && usuarioActualId.isNotBlank()) {
                listOf(usuarioActualId)
            } else {
                autorIdsUnicos
            }

            // 3. Mapear cada autor con avatar y primeros 10 caracteres del nombre
            val listaMiembros = autorIdsFinales.map { autorId ->
                val usr = UsuarioRepository.getUser(autorId).getOrNull()
                val jugadorPerfil = UsuarioRepository.getPlayerProfile(autorId).getOrNull()

                val esUsuarioActual = (autorId == usuarioActualId)
                val nombreCompleto = usr?.name?.trim() ?: "Usuario"

                // Formato: "Tú" para el usuario actual, o máximo 10 caracteres del nombre
                val nombreFormateado = if (esUsuarioActual) {
                    "Tú"
                } else {
                    if (nombreCompleto.length > 10) nombreCompleto.take(10) else nombreCompleto
                }

                MiembroComunidad(
                    id = autorId,
                    nombre = nombreFormateado,
                    rol = mapRolToNombre(usr?.role ?: "JUG"),
                    avatarUrl = jugadorPerfil?.fotografiaUrl,
                    esUsuarioActual = esUsuarioActual
                )
            }.sortedByDescending { it.esUsuarioActual }

            Result.success(listaMiembros)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener últimos autores de publicaciones: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- FUNCIONES AUXILIARES DE FECHA Y ROL ---
    fun mapRolToNombre(roleCode: String): String {
        return when (roleCode.uppercase()) {
            "DT" -> "Entrenador"
            "ADM" -> "Admin"
            "PAD" -> "Padre"
            "JUG" -> "Jugador"
            else -> "Miembro"
        }
    }

    private fun primerNombre(fullName: String): String {
        val partes = fullName.trim().split(" ")
        return if (partes.isNotEmpty() && partes[0].isNotBlank()) partes[0] else "Usuario"
    }

    private fun calcularTiempoRelativo(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) return "Recientemente"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(isoTimestamp) ?: return "Recientemente"
            val diffMs = System.currentTimeMillis() - date.time

            val minutos = diffMs / (1000 * 60)
            val horas = minutos / 60
            val dias = horas / 24

            when {
                minutos < 1 -> "Ahora mismo"
                minutos < 60 -> "Hace ${minutos}m"
                horas < 24 -> "Hace ${horas}h"
                else -> "Hace ${dias}d"
            }
        } catch (e: Exception) {
            "Hace un momento"
        }
    }

    fun formatearFechaHoraCompleta(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) return "Recientemente"
        return try {
            val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdfInput.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdfInput.parse(isoTimestamp) ?: return "Recientemente"

            val sdfOutput = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            sdfOutput.format(date)
        } catch (e: Exception) {
            "Recientemente"
        }
    }
}
