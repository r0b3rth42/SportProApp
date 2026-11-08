package com.example.sportproapp.presentation.mensualidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.JugadorConUsuario
import com.example.sportproapp.data.model.Mensualidad
import com.example.sportproapp.data.model.PagoMensualidad
import com.example.sportproapp.data.remote.supabase.MensualidadRepository
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import com.example.sportproapp.data.session.UserSessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MensualidadesUiState {
    object Loading : MensualidadesUiState
    data class Success(
        val esApoderado: Boolean,
        val hijos: List<JugadorConUsuario>,
        val jugadorSeleccionadoId: String?,
        val mensualidades: List<Mensualidad>,
        val pagos: Map<String, PagoMensualidad>
    ) : MensualidadesUiState
    data class Error(val message: String) : MensualidadesUiState
}

// 4.01 Mensualidades del jugador. El apoderado ve las de sus hijos vinculados (RP-02).
class MensualidadesViewModel(
    private val repository: MensualidadRepository = MensualidadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MensualidadesUiState>(MensualidadesUiState.Loading)
    val uiState: StateFlow<MensualidadesUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var esApoderado = false
    private var hijos: List<JugadorConUsuario> = emptyList()
    private var jugadorSeleccionadoId: String? = null

    fun cargarDatos(esRefresh: Boolean = false) {
        val session = UserSessionManager.getCurrentUser()
        val uid = session?.uid ?: FirebaseAuth.getInstance().currentUser?.uid

        if (uid.isNullOrBlank()) {
            _uiState.value = MensualidadesUiState.Error("No se encontró la sesión del usuario.")
            return
        }

        esApoderado = session?.role == "PAD"
        if (esRefresh) _isRefreshing.value = true
        else if (_uiState.value !is MensualidadesUiState.Success) _uiState.value = MensualidadesUiState.Loading

        viewModelScope.launch {
            if (esApoderado) {
                hijos = UsuarioRepository.getMisRepresentadosConUsuario(uid).getOrDefault(emptyList())
                if (hijos.none { it.perfil.id == jugadorSeleccionadoId }) {
                    jugadorSeleccionadoId = hijos.firstOrNull()?.perfil?.id
                }
            } else {
                jugadorSeleccionadoId = uid
            }
            cargarCuotas()
            _isRefreshing.value = false
        }
    }

    fun seleccionarHijo(jugadorId: String) {
        if (jugadorId == jugadorSeleccionadoId) return
        jugadorSeleccionadoId = jugadorId
        _uiState.value = MensualidadesUiState.Loading
        viewModelScope.launch { cargarCuotas() }
    }

    private suspend fun cargarCuotas() {
        val jugadorId = jugadorSeleccionadoId
        if (jugadorId == null) {
            _uiState.value = MensualidadesUiState.Success(esApoderado, hijos, null, emptyList(), emptyMap())
            return
        }

        repository.getMensualidadesPorJugador(jugadorId).fold(
            onSuccess = { cuotas ->
                val pagos = repository.getPagosPorMensualidades(cuotas.mapNotNull { it.id })
                    .getOrDefault(emptyMap())
                _uiState.value = MensualidadesUiState.Success(
                    esApoderado = esApoderado,
                    hijos = hijos,
                    jugadorSeleccionadoId = jugadorId,
                    mensualidades = cuotas,
                    pagos = pagos
                )
            },
            onFailure = { ex ->
                _uiState.value = MensualidadesUiState.Error(ex.message ?: "Error al cargar las mensualidades")
            }
        )
    }
}
