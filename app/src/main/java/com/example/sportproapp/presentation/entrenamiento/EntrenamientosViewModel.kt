package com.example.sportproapp.presentation.entrenamiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.remote.supabase.EntrenamientoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EntrenamientosUiState {
    object Loading : EntrenamientosUiState
    data class Success(val entrenamientos: List<Entrenamiento>) : EntrenamientosUiState
    data class Error(val message: String) : EntrenamientosUiState
}

class EntrenamientosViewModel(
    private val repository: EntrenamientoRepository = EntrenamientoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EntrenamientosUiState>(EntrenamientosUiState.Loading)
    val uiState: StateFlow<EntrenamientosUiState> = _uiState.asStateFlow()

    init {
        fetchEntrenamientos()
    }

    fun fetchEntrenamientos() {
        _uiState.value = EntrenamientosUiState.Loading
        viewModelScope.launch {
            val result = repository.getEntrenamientosPorEquipo("f9a01cfa-8aa4-430d-9801-e216f0c5b6e4")
            result.fold(
                onSuccess = { lista ->
                    _uiState.value = EntrenamientosUiState.Success(lista)
                },
                onFailure = { ex ->
                    _uiState.value = EntrenamientosUiState.Error(
                        ex.message ?: "Error al cargar entrenamientos"
                    )
                }
            )
        }
    }
}