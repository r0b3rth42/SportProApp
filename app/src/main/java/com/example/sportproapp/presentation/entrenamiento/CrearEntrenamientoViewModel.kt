package com.example.sportproapp.presentation.entrenamiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.remote.supabase.EntrenamientoRepository
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
    private val repository: EntrenamientoRepository = EntrenamientoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CrearEntrenamientoUiState>(CrearEntrenamientoUiState.Idle)
    val uiState: StateFlow<CrearEntrenamientoUiState> = _uiState.asStateFlow()

    fun guardarEntrenamiento(
        fecha: String,
        horaIni: String,
        horaFin: String,
        equipoId: String,
        ubicacion: String
    ) {
        if (fecha.isBlank() || horaIni.isBlank() || horaFin.isBlank() || ubicacion.isBlank()) {
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
                sedeId = ubicacion.trim(),
                estado = 2 // 2 = Planificado
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