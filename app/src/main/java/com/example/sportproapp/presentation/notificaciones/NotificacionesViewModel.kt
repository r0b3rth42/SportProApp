package com.example.sportproapp.presentation.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.Notificacion
import com.example.sportproapp.data.remote.supabase.NotificacionRepository
import com.example.sportproapp.data.session.UserSessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NotificacionesUiState {
    object Loading : NotificacionesUiState
    data class Success(val notificaciones: List<Notificacion>) : NotificacionesUiState
    data class Error(val message: String) : NotificacionesUiState
}

// 2.05 Notificaciones dentro de la app
class NotificacionesViewModel(
    private val repository: NotificacionRepository = NotificacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificacionesUiState>(NotificacionesUiState.Loading)
    val uiState: StateFlow<NotificacionesUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private fun usuarioId(): String? =
        UserSessionManager.getCurrentUser()?.uid ?: FirebaseAuth.getInstance().currentUser?.uid

    fun cargarNotificaciones(esRefresh: Boolean = false) {
        val uid = usuarioId()
        if (uid.isNullOrBlank()) {
            _uiState.value = NotificacionesUiState.Error("No se encontró la sesión del usuario.")
            return
        }

        if (esRefresh) _isRefreshing.value = true
        else if (_uiState.value !is NotificacionesUiState.Success) _uiState.value = NotificacionesUiState.Loading

        viewModelScope.launch {
            repository.getNotificaciones(uid).fold(
                onSuccess = { _uiState.value = NotificacionesUiState.Success(it) },
                onFailure = { ex ->
                    _uiState.value = NotificacionesUiState.Error(ex.message ?: "Error al cargar las notificaciones")
                }
            )
            _isRefreshing.value = false
        }
    }

    fun marcarComoLeida(notificacion: Notificacion) {
        val id = notificacion.id ?: return
        if (notificacion.leida) return
        actualizarLocal { lista -> lista.map { if (it.id == id) it.copy(leida = true) else it } }
        viewModelScope.launch { repository.marcarComoLeida(id) }
    }

    fun marcarTodasComoLeidas() {
        val uid = usuarioId() ?: return
        actualizarLocal { lista -> lista.map { it.copy(leida = true) } }
        viewModelScope.launch { repository.marcarTodasComoLeidas(uid) }
    }

    private fun actualizarLocal(cambio: (List<Notificacion>) -> List<Notificacion>) {
        val state = _uiState.value as? NotificacionesUiState.Success ?: return
        _uiState.value = NotificacionesUiState.Success(cambio(state.notificaciones))
    }
}
