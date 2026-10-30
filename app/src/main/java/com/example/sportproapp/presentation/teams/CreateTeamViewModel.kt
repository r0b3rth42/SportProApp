package com.example.sportproapp.presentation.teams

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.Equipo
import com.example.sportproapp.data.remote.supabase.EquipoRepository
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CreateTeamUiState {
    object Idle : CreateTeamUiState
    object Loading : CreateTeamUiState
    object Success : CreateTeamUiState
    data class Error(val message: String) : CreateTeamUiState
}

class CreateTeamViewModel(
    private val repository: EquipoRepository = EquipoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateTeamUiState>(CreateTeamUiState.Idle)
    val uiState: StateFlow<CreateTeamUiState> = _uiState.asStateFlow()

    fun createTeam(
        teamName: String,
        category: String,
        selectedColor: Color
    ) {
        if (teamName.isBlank()) {
            _uiState.value = CreateTeamUiState.Error("Ingresa el nombre del equipo")
            return
        }

        _uiState.value = CreateTeamUiState.Loading

        viewModelScope.launch {
            val hexColor = String.format("#%06X", (0xFFFFFF and selectedColor.toArgb()))

            val nuevoEquipo = Equipo(
                nombre = teamName.trim(),
                categoria = category.trim(),
                stripeColorHex = hexColor
            )

            val result = repository.saveEquipo(nuevoEquipo)
            result.fold(
                onSuccess = {
                    _uiState.value = CreateTeamUiState.Success
                },
                onFailure = { exception ->
                    _uiState.value = CreateTeamUiState.Error(
                        exception.message ?: "Error al registrar el equipo en Supabase"
                    )
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = CreateTeamUiState.Idle
    }
}