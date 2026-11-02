package com.example.sportproapp.presentation.entrenamiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.Asistencia
import com.example.sportproapp.data.model.EstadoAsistencia
import com.example.sportproapp.data.model.JugadorAsistenciaUI
import com.example.sportproapp.data.remote.supabase.AsistenciaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AsistenciaUiState {
    object Loading : AsistenciaUiState
    data class Success(val lista: List<JugadorAsistenciaUI>) : AsistenciaUiState
    data class Error(val message: String) : AsistenciaUiState
}

class AsistenciaViewModel(
    private val repository: AsistenciaRepository = AsistenciaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AsistenciaUiState>(AsistenciaUiState.Loading)
    val uiState: StateFlow<AsistenciaUiState> = _uiState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun cargarAsistencias(entrenamientoId: String, equipoId: String) {
        _uiState.value = AsistenciaUiState.Loading
        viewModelScope.launch {
            val result = repository.getJugadoresYAsistencias(entrenamientoId, equipoId)
            result.fold(
                onSuccess = { (jugadoresConUsuario, asistencias) ->
                    val mapaAsistencias = asistencias.associateBy { it.jugadorId }

                    val listaUI = jugadoresConUsuario.map { item ->
                        val jugador = item.perfil
                        val usuario = item.usuario
                        val asistenciaGuardada = mapaAsistencias[jugador.id]

                        JugadorAsistenciaUI(
                            jugadorId = jugador.id ?: "",
                            nombreCompleto = "${usuario?.name ?: ""} ${usuario?.name ?: ""}".trim().ifEmpty { "Jugador" },
                            dorsal = jugador.posicion?.toString() ?: "S/N",
                            fotoUrl = jugador?.fotografiaUrl,
                            estado = asistenciaGuardada?.estado ?: EstadoAsistencia.PRESENTE
                        )
                    }

                    _uiState.value = AsistenciaUiState.Success(listaUI)
                },
                onFailure = { ex ->
                    _uiState.value = AsistenciaUiState.Error(
                        ex.message ?: "Error al cargar plantilla de jugadores"
                    )
                }
            )
        }
    }

    fun cambiarEstadoJugador(jugadorId: String, nuevoEstado: EstadoAsistencia) {
        val currentState = _uiState.value
        if (currentState is AsistenciaUiState.Success) {
            val nuevaLista = currentState.lista.map { item ->
                if (item.jugadorId == jugadorId) item.copy(estado = nuevoEstado) else item
            }
            _uiState.value = AsistenciaUiState.Success(nuevaLista)
        }
    }

    fun marcarTodosPresentes(marcar: Boolean) {
        val currentState = _uiState.value
        if (currentState is AsistenciaUiState.Success) {
            val estadoObjetivo = if (marcar) EstadoAsistencia.PRESENTE else EstadoAsistencia.AUSENTE
            val nuevaLista = currentState.lista.map { it.copy(estado = estadoObjetivo) }
            _uiState.value = AsistenciaUiState.Success(nuevaLista)
        }
    }

    fun guardarAsistencias(entrenamientoId: String, onExito: () -> Unit) {
        val currentState = _uiState.value
        if (currentState !is AsistenciaUiState.Success) return

        _isSaving.value = true
        viewModelScope.launch {
            val registros = currentState.lista.map { uiItem ->
                Asistencia(
                    entrenamientoId = entrenamientoId,
                    jugadorId = uiItem.jugadorId,
                    estado = uiItem.estado
                )
            }

            val result = repository.guardarAsistencias(registros)
            _isSaving.value = false
            result.fold(
                onSuccess = { onExito() },
                onFailure = { /* Manejo de error opcional */ }
            )
        }
    }
}