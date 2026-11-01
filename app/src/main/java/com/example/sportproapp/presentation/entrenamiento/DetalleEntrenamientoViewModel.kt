package com.example.sportproapp.presentation.entrenamiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.BloqueEntrenamiento
import com.example.sportproapp.data.model.Ejercicio
import com.example.sportproapp.data.remote.supabase.EntrenamientoRepository
import com.example.sportproapp.data.session.UserSessionManager
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

    // Lista de ejercicios previamente creados en la academia
    private val _ejerciciosAcademia = MutableStateFlow<List<Ejercicio>>(emptyList())
    val ejerciciosAcademia: StateFlow<List<Ejercicio>> = _ejerciciosAcademia.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

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

    fun cargarEjerciciosAcademia() {
        val academiaId = UserSessionManager.getAcademiaId() ?: return
        viewModelScope.launch {
            val result = repository.getEjerciciosPorAcademia(academiaId)
            result.onSuccess { lista ->
                _ejerciciosAcademia.value = lista
            }
        }
    }

    fun asociarEjercicioExistente(
        entrenamientoId: String,
        ejercicioId: String,
        onSuccess: () -> Unit
    ) {
        _isSaving.value = true
        viewModelScope.launch {
            val result = repository.asociarEjercicioAEntrenamiento(entrenamientoId, ejercicioId)
            _isSaving.value = false
            result.fold(
                onSuccess = {
                    cargarDetalle(entrenamientoId)
                    onSuccess()
                },
                onFailure = { /* Manejo de error opcional */ }
            )
        }
    }

    fun crearYAsociarNuevoEjercicio(
        entrenamientoId: String,
        bloque: BloqueEntrenamiento,
        titulo: String,
        descripcion: String,
        duracionMin: Int,
        onSuccess: () -> Unit
    ) {
        val academiaId = UserSessionManager.getAcademiaId() ?: return
        _isSaving.value = true

        viewModelScope.launch {
            val nuevoEjercicio = Ejercicio(
                academiaId = academiaId,
                bloque = bloque,
                titulo = titulo,
                descripcion = descripcion.ifBlank { null },
                duracionMin = duracionMin
            )

            val result = repository.crearYAsociarEjercicio(entrenamientoId, nuevoEjercicio)
            _isSaving.value = false
            result.fold(
                onSuccess = {
                    cargarDetalle(entrenamientoId)
                    onSuccess()
                },
                onFailure = { /* Manejo de error opcional */ }
            )
        }
    }

    fun quitarEjercicio(
        entrenamientoId: String,
        ejercicioId: String
    ) {
        viewModelScope.launch {
            val result = repository.desasociarEjercicioDeEntrenamiento(entrenamientoId, ejercicioId)
            result.fold(
                onSuccess = {
                    // Recargamos el detalle para recalcular duraciones y actualizar la lista
                    cargarDetalle(entrenamientoId)
                },
                onFailure = { /* Manejo de error opcional */ }
            )
        }
    }
}