package com.example.sportproapp.presentation.entrenamiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.Sede
import com.example.sportproapp.data.remote.supabase.EntrenamientoRepository
import com.example.sportproapp.data.remote.supabase.SedeRepository
import com.example.sportproapp.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CrearEntrenamientoUiState {
    object Idle : CrearEntrenamientoUiState
    object Loading : CrearEntrenamientoUiState
    object Success : CrearEntrenamientoUiState
    data class Error(val message: String) : CrearEntrenamientoUiState
}

class CrearEntrenamientoViewModel(
    private val repository: EntrenamientoRepository = EntrenamientoRepository,
    private val sedeRepository: SedeRepository = SedeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CrearEntrenamientoUiState>(CrearEntrenamientoUiState.Idle)
    val uiState: StateFlow<CrearEntrenamientoUiState> = _uiState.asStateFlow()

    private val _sedes = MutableStateFlow<List<Sede>>(emptyList())
    val sedes: StateFlow<List<Sede>> = _sedes.asStateFlow()

    private val _isLoadingSedes = MutableStateFlow(true)
    val isLoadingSedes: StateFlow<Boolean> = _isLoadingSedes.asStateFlow()

    init {
        cargarSedes()
    }

    fun cargarSedes() {
        val academiaId = UserSessionManager.getAcademiaId()
        if (academiaId.isNullOrEmpty()) {
            _isLoadingSedes.value = false
            return
        }

        _isLoadingSedes.value = true
        viewModelScope.launch {
            val result = sedeRepository.getSedes(academiaId)
            result.fold(
                onSuccess = { lista ->
                    _sedes.value = lista
                    _isLoadingSedes.value = false
                },
                onFailure = {
                    _isLoadingSedes.value = false
                }
            )
        }
    }

    fun guardarEntrenamiento(
        fecha: String,
        horaIni: String,
        horaFin: String,
        equipoId: String,
        sedeId: String
    ) {
        if (fecha.isBlank() || horaIni.isBlank() || horaFin.isBlank() || sedeId.isBlank()) {
            _uiState.value = CrearEntrenamientoUiState.Error("Completa todos los campos obligatorios")
            return
        }

        _uiState.value = CrearEntrenamientoUiState.Loading

        viewModelScope.launch {
            val nuevoEntrenamiento = Entrenamiento(
                fecha = fecha.trim(),
                horaIni = horaIni.trim(),
                horaFin = horaFin.trim(),
                equipoId = equipoId,
                sedeId = sedeId.trim()
            )

            val result = repository.saveEntrenamiento(nuevoEntrenamiento)
            result.fold(
                onSuccess = {
                    _uiState.value = CrearEntrenamientoUiState.Success
                },
                onFailure = { ex ->
                    _uiState.value = CrearEntrenamientoUiState.Error(
                        ex.message ?: "Error al guardar el entrenamiento en Supabase"
                    )
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = CrearEntrenamientoUiState.Idle
    }
}