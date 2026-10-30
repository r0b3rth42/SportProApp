package com.example.sportproapp.presentation.teams

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.remote.supabase.EquipoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

// Modelo para la UI
data class TeamUiModel(
    val id: String,
    val name: String,
    val category: String,
    val stripeColor: Color,
    val badgeTag: String
)

sealed interface TeamsUiState {
    object Loading : TeamsUiState
    data class Success(val teams: List<TeamUiModel>) : TeamsUiState
    data class Error(val message: String) : TeamsUiState
}

class TeamsViewModel(
    // Inyectamos el repositorio (o usamos el objeto si es un singleton/object)
    private val repository: EquipoRepository = EquipoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeamsUiState>(TeamsUiState.Loading)
    val uiState: StateFlow<TeamsUiState> = _uiState.asStateFlow()

    init {
        fetchTeams()
    }

    fun fetchTeams() {
        viewModelScope.launch {
            _uiState.value = TeamsUiState.Loading

            // Usamos el repositorio que ya creaste, el cual maneja el Result
            val result = repository.getEquipos()

            if (result.isSuccess) {
                val teamModels = result.getOrNull() ?: emptyList()

                // Mapeamos los datos de Supabase al modelo visual de Compose
                val teamsList = teamModels.map { teamModel ->
                    val stripeColor = try {
                        Color(teamModel.stripeColorHex.toColorInt())
                    } catch (e: Exception) {
                        Color(0xFFCCFF00) // Color por defecto
                    }

                    TeamUiModel(
                        id = teamModel.id,
                        name = teamModel.nombre.ifBlank { "Equipo sin nombre" },
                        category = teamModel.categoria.ifBlank { "Sin categoría" },
                        stripeColor = stripeColor,
                        badgeTag = teamModel.status
                    )
                }

                _uiState.value = TeamsUiState.Success(teamsList)
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Error al cargar equipos"
                _uiState.value = TeamsUiState.Error(errorMsg)
            }
        }
    }
}