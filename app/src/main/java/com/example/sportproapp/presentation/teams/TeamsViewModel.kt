package com.example.sportproapp.presentation.teams

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.AppPermission
import com.example.sportproapp.data.model.hasPermission
import com.example.sportproapp.data.remote.supabase.EquipoRepository
import com.example.sportproapp.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    private val repository: EquipoRepository = EquipoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeamsUiState>(TeamsUiState.Loading)
    val uiState: StateFlow<TeamsUiState> = _uiState.asStateFlow()

    // Estado reactivo para determinar si el usuario actual posee permisos de gestión de equipos
    private val _canManageTeams = MutableStateFlow(false)
    val canManageTeams: StateFlow<Boolean> = _canManageTeams.asStateFlow()

    // Alias para retrocompatibilidad
    val isAdmin: StateFlow<Boolean> get() = _canManageTeams.asStateFlow()

    init {
        verificarPermisosUsuario()
        fetchTeams()
    }

    private fun verificarPermisosUsuario() {
        viewModelScope.launch {
            val session = UserSessionManager.getCurrentUser()
            _canManageTeams.value = session.hasPermission(AppPermission.GESTIONAR_EQUIPOS)
        }
    }

    fun fetchTeams() {
        viewModelScope.launch {
            _uiState.value = TeamsUiState.Loading

            val user = UserSessionManager.getCurrentUser()

            val result = repository.getEquiposByAcademy(user?.academiaId.toString())

            if (result.isSuccess) {
                val teamModels = result.getOrNull() ?: emptyList()

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
