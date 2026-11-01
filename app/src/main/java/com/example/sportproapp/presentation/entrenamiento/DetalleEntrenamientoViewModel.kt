package com.example.sportproapp.presentation.entrenamiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.Ejercicio
import com.example.sportproapp.data.remote.supabase.EntrenamientoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetalleEntrenamientoUiState {
    object Loading : DetalleEntrenamientoUiState
    data class Success(
        val ejercicios: List<Ejercicio>,
        val duracionTotalMin: Int
    ) : DetalleEntrenamientoUiState
    data class Error(val message: String) : DetalleEntrenamientoUiState
}

class DetalleEntrenamientoViewModel(
    private val repository: EntrenamientoRepository = EntrenamientoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleEntrenamientoUiState>(DetalleEntrenamientoUiState.Loading)
    val uiState: StateFlow<DetalleEntrenamientoUiState> = _uiState.asStateFlow()

    fun cargarDetalle(entrenamientoId: String) {
        _uiState.value = DetalleEntrenamientoUiState.Loading
        viewModelScope.launch {
            val result = repository.getEjerciciosPorEntrenamiento(entrenamientoId)
            result.fold(
                onSuccess = { lista ->
                    val duracionTotal = lista.sumOf { it.duracionMin }
                    _uiState.value = DetalleEntrenamientoUiState.Success(
                        ejercicios = lista,
                        duracionTotalMin = duracionTotal
                    )
                },
                onFailure = { ex ->
                    _uiState.value = DetalleEntrenamientoUiState.Error(
                        ex.message ?: "Error al cargar el detalle del entrenamiento"
                    )
                }
            )
        }
    }
}