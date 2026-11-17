package com.example.sportproapp.data.model

import com.example.sportproapp.data.session.UserSession

/**
 * Permisos/Capacidades del sistema para control de acceso RBAC.
 */
enum class AppPermission {
    CREAR_ENTRENAMIENTO,
    TOMAR_ASISTENCIA,
    MODERAR_PUBLICACIONES,
    APROBAR_PUBLICACION,
    CREAR_PARTIDO,
    ENVIAR_DESAFIO,
    GESTIONAR_EQUIPOS,
    GESTIONAR_MENSUALIDADES,
    CREAR_MENSUALIDAD,
    PAGAR_MENSUALIDAD,
    VER_ADMIN_PANEL
}

/**
 * Convierte un String de rol (ej. "ADM", "DT") al Enum [UserRole].
 */
fun String?.toUserRole(): UserRole? {
    if (this.isNullOrBlank()) return null
    return runCatching { UserRole.valueOf(this.uppercase()) }.getOrNull()
}

/**
 * Evalúa si un [UserRole] posee un determinado [AppPermission].
 */
fun UserRole?.hasPermission(permission: AppPermission): Boolean {
    if (this == null) return false

    return when (this) {
        UserRole.ADM -> true // El Administrador posee control total
        UserRole.DT -> when (permission) {
            AppPermission.CREAR_ENTRENAMIENTO,
            AppPermission.TOMAR_ASISTENCIA,
            AppPermission.MODERAR_PUBLICACIONES,
            AppPermission.APROBAR_PUBLICACION,
            AppPermission.CREAR_PARTIDO,
            AppPermission.ENVIAR_DESAFIO,
            AppPermission.GESTIONAR_EQUIPOS -> true
            else -> false
        }
        UserRole.JUG -> when (permission) {
            AppPermission.PAGAR_MENSUALIDAD -> true
            else -> false
        }
        UserRole.PAD -> when (permission) {
            AppPermission.PAGAR_MENSUALIDAD -> true
            else -> false
        }
    }
}

/**
 * Evalúa si la sesión de usuario activa [UserSession] posee un determinado [AppPermission].
 */
fun UserSession?.hasPermission(permission: AppPermission): Boolean {
    val role = this?.role?.toUserRole()
    return role.hasPermission(permission)
}
