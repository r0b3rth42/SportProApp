package com.example.sportproapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Publicacion(
    val id: String? = null,
    @SerialName("academia_id") val academiaId: String = "",
    @SerialName("equipo_id") val equipoId: String? = null,
    @SerialName("autor_id") val autorId: String = "",
    val titulo: String? = null,
    val contenido: String = "",
    @SerialName("imagen_url") val imagenUrl: String? = null,
    val tipo: String = "GENERAL", // GENERAL, MI_EQUIPO, ANUNCIO, CRONICA
    val estado: String = "PENDIENTE", // PENDIENTE, APROBADO, CAMBIOS_SOLICITADOS, RECHAZADO
    @SerialName("notas_internas") val notasInternas: String? = null,
    @SerialName("version_info") val versionInfo: String? = "Versión 1",
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("comments_count") val commentsCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

// DTO exclusivo para inserción de publicaciones (inicia por defecto en Versión 1)
@Serializable
data class PublicacionInsert(
    @SerialName("academia_id") val academiaId: String,
    @SerialName("equipo_id") val equipoId: String? = null,
    @SerialName("autor_id") val autorId: String,
    val titulo: String? = null,
    val contenido: String,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    val tipo: String = "GENERAL",
    val estado: String = "PENDIENTE",
    @SerialName("notas_internas") val notasInternas: String? = null,
    @SerialName("version_info") val versionInfo: String = "Versión 1"
)

@Serializable
data class PublicacionLike(
    val id: String? = null,
    @SerialName("publicacion_id") val publicacionId: String = "",
    @SerialName("usuario_id") val usuarioId: String = "",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class PublicacionLikeInsert(
    @SerialName("publicacion_id") val publicacionId: String,
    @SerialName("usuario_id") val usuarioId: String
)

@Serializable
data class PublicacionComentario(
    val id: String? = null,
    @SerialName("publicacion_id") val publicacionId: String = "",
    @SerialName("autor_id") val autorId: String = "",
    @SerialName("comentario_padre_id") val comentarioPadreId: String? = null,
    val contenido: String = "",
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class PublicacionComentarioInsert(
    @SerialName("publicacion_id") val publicacionId: String,
    @SerialName("autor_id") val autorId: String,
    @SerialName("comentario_padre_id") val comentarioPadreId: String? = null,
    val contenido: String
)

@Serializable
data class ComentarioLike(
    val id: String? = null,
    @SerialName("comentario_id") val comentarioId: String = "",
    @SerialName("usuario_id") val usuarioId: String = "",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ComentarioLikeInsert(
    @SerialName("comentario_id") val comentarioId: String,
    @SerialName("usuario_id") val usuarioId: String
)

// DTO para la capa de UI que agrupa la publicación con datos formateados del autor y su estado
data class PublicacionConAutor(
    val publicacion: Publicacion,
    val autorNombre: String,
    val autorRol: String, // "Entrenador", "Padre", "Jugador", "Admin"
    val autorAvatarUrl: String? = null,
    val isLikedByCurrentUser: Boolean = false,
    val tiempoRelativo: String = "",
    val fechaHoraFormateada: String = ""
)

// DTO para comentarios con respuestas anidadas
data class ComentarioConAutor(
    val comentario: PublicacionComentario,
    val autorNombre: String,
    val autorRol: String,
    val autorAvatarUrl: String? = null,
    val isLikedByCurrentUser: Boolean = false,
    val tiempoRelativo: String = "",
    val respuestas: List<ComentarioConAutor> = emptyList()
)

// Modelo para los avatares/historias superiores de miembros de la comunidad
data class MiembroComunidad(
    val id: String,
    val nombre: String,
    val rol: String,
    val avatarUrl: String? = null,
    val esUsuarioActual: Boolean = false
)
