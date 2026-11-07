package com.example.sportproapp.presentation.mensualidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.EstadoVisualMensualidad
import com.example.sportproapp.data.model.MensualidadJugadorUI
import com.example.sportproapp.data.model.estadoVisual
import com.example.sportproapp.data.model.moverPeriodo
import com.example.sportproapp.data.model.periodoActual
import com.example.sportproapp.data.remote.supabase.MensualidadRepository
import com.example.sportproapp.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResumenMensualidades(
    val recaudado: Double = 0.0,
    val pendientes: Int = 0,
    val atrasados: Int = 0
)

sealed interface MensualidadesAdminUiState {
    object Loading : MensualidadesAdminUiState
    data class Success(
        val lista: List<MensualidadJugadorUI>,
        val resumen: ResumenMensualidades
    ) : MensualidadesAdminUiState
    data class Error(val message: String) : MensualidadesAdminUiState
}

// 4.02 Mensualidades · administrador (toda la academia) y DT (solo lectura, su equipo)
class MensualidadesAdminViewModel(
    private val repository: MensualidadRepository = MensualidadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MensualidadesAdminUiState>(MensualidadesAdminUiState.Loading)
    val uiState: StateFlow<MensualidadesAdminUiState> = _uiState.asStateFlow()

    private val _periodo = MutableStateFlow(periodoActual())
    val periodo: StateFlow<String> = _periodo.asStateFlow()

    private val _esAdmin = MutableStateFlow(false)
    val esAdmin: StateFlow<Boolean> = _esAdmin.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun cargarDatos(esRefresh: Boolean = false) {
        val session = UserSessionManager.getCurrentUser()
        if (session == null) {
            _uiState.value = MensualidadesAdminUiState.Error("No se encontró la sesión del usuario.")
            return
        }
        _esAdmin.value = session.role == "ADM"

        if (esRefresh) _isRefreshing.value = true
        else _uiState.value = MensualidadesAdminUiState.Loading

        viewModelScope.launch {
            var academiaId: String? = null
            var equipoId: String? = null

            when (session.role) {
                "ADM" -> {
                    academiaId = session.academiaId
                    if (academiaId.isNullOrBlank()) {
                        mostrarError("No se encontró academia asociada en la sesión.")
                        return@launch
                    }
                }
                "DT" -> {
                    equipoId = repository.getEquipoDelEntrenador(session.uid).getOrNull()
                    if (equipoId.isNullOrBlank()) {
                        mostrarError("No tienes un equipo asignado.")
                        return@launch
                    }
                }
                else -> {
                    mostrarError("No tienes acceso a esta sección.")
                    return@launch
                }
            }

            repository.getMensualidadesPorPeriodo(_periodo.value, academiaId, equipoId).fold(
                onSuccess = { lista ->
                    val estados = lista.map { it.mensualidad.estadoVisual() }
                    val resumen = ResumenMensualidades(
                        recaudado = lista
                            .filter { it.mensualidad.estadoVisual() == EstadoVisualMensualidad.PAGADO }
                            .sumOf { it.mensualidad.monto },
                        pendientes = estados.count { it == EstadoVisualMensualidad.PENDIENTE },
                        atrasados = estados.count { it == EstadoVisualMensualidad.ATRASADO }
                    )
                    _uiState.value = MensualidadesAdminUiState.Success(lista, resumen)
                },
                onFailure = { ex ->
                    _uiState.value = MensualidadesAdminUiState.Error(ex.message ?: "Error al cargar las mensualidades")
                }
            )
            _isRefreshing.value = false
        }
    }

    fun periodoAnterior() {
        _periodo.value = moverPeriodo(_periodo.value, -1)
        cargarDatos()
    }

    fun periodoSiguiente() {
        _periodo.value = moverPeriodo(_periodo.value, 1)
        cargarDatos()
    }

    private fun mostrarError(mensaje: String) {
        _uiState.value = MensualidadesAdminUiState.Error(mensaje)
        _isRefreshing.value = false
    }
}
