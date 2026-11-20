package com.example.sportproapp.presentation.partidos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.EventoPartido
import com.example.sportproapp.data.model.EventoPartidoUI
import com.example.sportproapp.data.model.JugadorConUsuario
import com.example.sportproapp.data.model.PartidoConDetalles
import com.example.sportproapp.data.model.TipoEvento
import com.example.sportproapp.data.remote.supabase.DesafioPartidoRepository
import com.example.sportproapp.data.remote.supabase.EventoPartidoRepository
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class ConsolaEnVivoUiState {
    object Loading : ConsolaEnVivoUiState()
    data class Success(val partido: PartidoConDetalles) : ConsolaEnVivoUiState()
    data class Error(val message: String) : ConsolaEnVivoUiState()
}

class ConsolaEnVivoViewModel : ViewModel() {

    private val TAG = "ConsolaEnVivoViewModel"

    private val _uiState = MutableStateFlow<ConsolaEnVivoUiState>(ConsolaEnVivoUiState.Loading)
    val uiState: StateFlow<ConsolaEnVivoUiState> = _uiState.asStateFlow()

    private val _equipoSeleccionadoId = MutableStateFlow<String>("")
    val equipoSeleccionadoId: StateFlow<String> = _equipoSeleccionadoId.asStateFlow()

    private val _cronometroSegundos = MutableStateFlow<Int>(0)
    val cronometroSegundos: StateFlow<Int> = _cronometroSegundos.asStateFlow()

    private val _isTimerRunning = MutableStateFlow<Boolean>(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _eventos = MutableStateFlow<List<EventoPartidoUI>>(emptyList())
    val eventos: StateFlow<List<EventoPartidoUI>> = _eventos.asStateFlow()

    private val _jugadoresLocal = MutableStateFlow<List<JugadorConUsuario>>(emptyList())
    val jugadoresLocal: StateFlow<List<JugadorConUsuario>> = _jugadoresLocal.asStateFlow()

    private val _jugadoresVisitante = MutableStateFlow<List<JugadorConUsuario>>(emptyList())
    val jugadoresVisitante: StateFlow<List<JugadorConUsuario>> = _jugadoresVisitante.asStateFlow()

    private val _isProcessingAction = MutableStateFlow<Boolean>(false)
    val isProcessingAction: StateFlow<Boolean> = _isProcessingAction.asStateFlow()

    private var timerJob: Job? = null
    private var partidoIdActual: Long = 0L

    fun cargarDatos(partidoId: Long) {
        partidoIdActual = partidoId
        viewModelScope.launch {
            _uiState.value = ConsolaEnVivoUiState.Loading
            try {
                val resultPartido = DesafioPartidoRepository.getPartidoConDetallesById(partidoId)
                resultPartido.fold(
                    onSuccess = { partido ->
                        _uiState.value = ConsolaEnVivoUiState.Success(partido)
                        if (_equipoSeleccionadoId.value.isBlank()) {
                            _equipoSeleccionadoId.value = partido.equipoLocalId
                        }

                        // Cargar jugadores del equipo local
                        if (partido.equipoLocalId.isNotBlank()) {
                            UsuarioRepository.getJugadoresConEquipoConUsuario(partido.equipoLocalId)
                                .onSuccess { lista -> _jugadoresLocal.value = lista }
                        }

                        // Cargar jugadores del equipo visitante
                        if (partido.equipoVisitanteId.isNotBlank()) {
                            UsuarioRepository.getJugadoresConEquipoConUsuario(partido.equipoVisitanteId)
                                .onSuccess { lista -> _jugadoresVisitante.value = lista }
                        }

                        // Cargar eventos del partido
                        cargarEventos()

                        // Iniciar temporizador si no está corriendo aún
                        if (!_isTimerRunning.value && _cronometroSegundos.value == 0) {
                            _cronometroSegundos.value = 67 * 60 + 32 // Minuto inicial simulado 67:32 como en la imagen
                            startTimer()
                        }
                    },
                    onFailure = { ex ->
                        _uiState.value = ConsolaEnVivoUiState.Error(ex.localizedMessage ?: "Error al cargar el partido")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar datos del partido $partidoId", e)
                _uiState.value = ConsolaEnVivoUiState.Error(e.localizedMessage ?: "Error inesperado")
            }
        }
    }

    private suspend fun cargarEventos() {
        if (partidoIdActual == 0L) return
        EventoPartidoRepository.getEventosPartido(partidoIdActual)
            .onSuccess { lista ->
                _eventos.value = lista
            }
    }

    fun seleccionarEquipo(equipoId: String) {
        _equipoSeleccionadoId.value = equipoId
    }

    fun toggleTimer() {
        if (_isTimerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun startTimer() {
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _isTimerRunning.value) {
                delay(1000L)
                _cronometroSegundos.value += 1
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun registrarEvento(
        tipoEvento: TipoEvento,
        jugadorId: String?,
        minuto: Int,
        detalle: String?,
        jugadorSaleId: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val equipoId = _equipoSeleccionadoId.value
        if (equipoId.isBlank()) {
            onError("Por favor selecciona un equipo antes de registrar el suceso")
            return
        }

        viewModelScope.launch {
            _isProcessingAction.value = true
            try {
                val nuevoEvento = EventoPartido(
                    partidoId = partidoIdActual,
                    equipoId = equipoId,
                    jugadorId = jugadorId.takeIf { !it.isNullOrBlank() },
                    tipoEvento = tipoEvento.name,
                    minuto = minuto,
                    detalle = detalle?.takeIf { it.isNotBlank() },
                    jugadorSaleId = jugadorSaleId.takeIf { !it.isNullOrBlank() }
                )

                val result = EventoPartidoRepository.guardarEvento(nuevoEvento)
                result.fold(
                    onSuccess = {
                        cargarEventos()
                        // Refrescar el marcador del partido
                        refrescarPartido()
                        onSuccess()
                    },
                    onFailure = { ex ->
                        onError(ex.localizedMessage ?: "Error al guardar el suceso")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error al registrar evento", e)
                onError(e.localizedMessage ?: "Error al registrar suceso")
            } finally {
                _isProcessingAction.value = false
            }
        }
    }

    fun eliminarEvento(eventoUI: EventoPartidoUI, onFinish: () -> Unit) {
        viewModelScope.launch {
            _isProcessingAction.value = true
            try {
                EventoPartidoRepository.eliminarEvento(
                    eventoId = eventoUI.id,
                    partidoId = eventoUI.partidoId,
                    equipoId = eventoUI.equipoId,
                    tipoEvento = eventoUI.tipoEvento.name
                ).onSuccess {
                    cargarEventos()
                    refrescarPartido()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar evento", e)
            } finally {
                _isProcessingAction.value = false
                onFinish()
            }
        }
    }

    private suspend fun refrescarPartido() {
        if (partidoIdActual == 0L) return
        DesafioPartidoRepository.getPartidoConDetallesById(partidoIdActual)
            .onSuccess { partido ->
                _uiState.value = ConsolaEnVivoUiState.Success(partido)
            }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
