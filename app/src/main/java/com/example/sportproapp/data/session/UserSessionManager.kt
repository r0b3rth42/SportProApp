package com.example.sportproapp.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserSessionManager {

    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    // Carga los datos en caché
    fun setSession(session: UserSession) {
        _userSession.value = session
    }

    // Obtener datos síncronamente desde cualquier parte de la app
    fun getCurrentUser(): UserSession? = _userSession.value

    // Obtener academiaId directamente si existe
    fun getAcademiaId(): String? = _userSession.value?.academiaId

    // Limpiar sesión al cerrar sesión (Logout)
    fun clearSession() {
        _userSession.value = null
    }
}