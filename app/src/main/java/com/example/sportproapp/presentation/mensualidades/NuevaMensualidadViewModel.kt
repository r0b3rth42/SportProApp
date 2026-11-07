package com.example.sportproapp.presentation.mensualidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.Equipo
import com.example.sportproapp.data.remote.supabase.EquipoRepository
import com.example.sportproapp.data.remote.supabase.MensualidadRepository
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import com.example.sportproapp.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NuevaMensualidadUiState {
    object Idle : NuevaMensualidadUiState
    object Loading : NuevaMensualidadUiState
    data class Success(val creadas: Int) : NuevaMensualidadUiState
    data class Error(val message: String) : NuevaMensualidadUiState
}

// 4.05 Nueva mensualidad: el administrador crea la cuota del mes para un equipo
class NuevaMensualidadViewModel(
    private val repository: MensualidadRepository = MensualidadRepository,
    private val equipoRepository: EquipoRepository = EquipoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NuevaMensualidadUiState>(NuevaMensualidadUiState.Idle)
    val uiState: StateFlow<NuevaMensualidadUiState> = _uiState.asStateFlow()

    private val _equipos = MutableStateFlow<List<Equipo>>(emptyList())
    val equipos: StateFlow<List<Equipo>> = _equipos.asStateFlow()

    private val _isLoadingEquipos = MutableStateFlow(true)
    val isLoadingEquipos: StateFlow<Boolean> = _isLoadingEquipos.asStateFlow()

    private val _equipoSeleccionado = MutableStateFlow<Equipo?>(null)
    val equipoSeleccionado: StateFlow<Equipo?> = _equipoSeleccionado.asStateFlow()

    // null mientras se consulta
    private val _cantidadJugadores = MutableStateFlow<Int?>(null)
    val cantidadJugadores: StateFlow<Int?> = _cantidadJugadores.asStateFlow()

    init {
        cargarEquipos()
    }

    fun cargarEquipos() {
        val academiaId = UserSessionManager.getAcademiaId()
        if (academiaId.isNullOrEmpty()) {
            _isLoadingEquipos.value = false
            return
        }

        _isLoadingEquipos.value = true
        viewModelScope.launch {
            equipoRepository.getEquiposByAcademy(academiaId).fold(
                onSuccess = { lista ->
                    _equipos.value = lista
                    _isLoadingEquipos.value = false
                    lista.firstOrNull()?.let { seleccionarEquipo(it) }
                },
                onFailure = {
                    _isLoadingEquipos.value = false
                }
            )
        }
    }

    fun seleccionarEquipo(equipo: Equipo) {
        _equipoSeleccionado.value = equipo
        _cantidadJugadores.value = null
        viewModelScope.launch {
            val jugadores = UsuarioRepository.getJugadoresConEquipo(equipo.id).getOrDefault(emptyList())
            _cantidadJugadores.value = jugadores.size
        }
    }

    fun crearMensualidades(periodo: String, montoTexto: String, fechaVencimiento: String) {
        val academiaId = UserSessionManager.getAcademiaId()
        val equipo = _equipoSeleccionado.value
        val monto = montoTexto.replace(",", ".").trim().toDoubleOrNull()

        when {
            academiaId.isNullOrEmpty() -> {
                _uiState.value = NuevaMensualidadUiState.Error("No se encontró academia asociada en la sesión.")
                return
            }
            equipo == null -> {
                _uiState.value = NuevaMensualidadUiState.Error("Selecciona un equipo")
                return
            }
            monto == null || monto <= 0 -> {
                _uiState.value = NuevaMensualidadUiState.Error("Ingresa un monto válido")
                return
            }
            fechaVencimiento.isBlank() -> {
                _uiState.value = NuevaMensualidadUiState.Error("Selecciona la fecha de vencimiento")
                return
            }
            _cantidadJugadores.value == 0 -> {
                _uiState.value = NuevaMensualidadUiState.Error("El equipo no tiene jugadores asignados")
                return
            }
        }

        _uiState.value = NuevaMensualidadUiState.Loading
        viewModelScope.launch {
            repository.crearMensualidades(
                academiaId = academiaId!!,
                equipoId = equipo!!.id,
                periodo = periodo,
                monto = monto!!,
                fechaVencimiento = fechaVencimiento,
                creadoPor = UserSessionManager.getCurrentUser()?.uid
            ).fold(
                onSuccess = { creadas -> _uiState.value = NuevaMensualidadUiState.Success(creadas) },
                onFailure = { ex ->
                    _uiState.value = NuevaMensualidadUiState.Error(ex.message ?: "Error al crear las mensualidades")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = NuevaMensualidadUiState.Idle
    }
}
