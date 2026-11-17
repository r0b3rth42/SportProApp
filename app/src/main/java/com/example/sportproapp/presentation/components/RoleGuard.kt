package com.example.sportproapp.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.example.sportproapp.data.model.AppPermission
import com.example.sportproapp.data.model.UserRole
import com.example.sportproapp.data.model.hasPermission
import com.example.sportproapp.data.model.toUserRole
import com.example.sportproapp.data.session.UserSession

/**
 * CompositionLocal para acceder a la sesión activa del usuario en la UI de Compose.
 */
val LocalUserSession = compositionLocalOf<UserSession?> { null }

/**
 * CompositionLocal para acceder al rol activo del usuario en la UI de Compose.
 */
val LocalUserRole = compositionLocalOf<UserRole?> { null }

/**
 * Encapsula la visibilidad de componentes en Compose evaluando si el usuario actual posee un [AppPermission].
 */
@Composable
fun RequirePermission(
    permission: AppPermission,
    userSession: UserSession? = LocalUserSession.current,
    content: @Composable () -> Unit
) {
    if (userSession.hasPermission(permission)) {
        content()
    }
}

/**
 * Encapsula la visibilidad de componentes en Compose evaluando si el rol del usuario se encuentra en [allowedRoles].
 */
@Composable
fun RoleSection(
    allowedRoles: List<UserRole>,
    userRole: UserRole? = LocalUserRole.current ?: LocalUserSession.current?.role?.toUserRole(),
    content: @Composable () -> Unit
) {
    if (userRole != null && userRole in allowedRoles) {
        content()
    }
}
