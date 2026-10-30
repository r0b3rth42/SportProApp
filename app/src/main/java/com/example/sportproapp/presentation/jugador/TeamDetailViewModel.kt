package com.example.sportproapp.presentation.jugador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.JugadorConUsuario
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estados para los jugadores del equipo
sealed interface TeamPlayersUiState {
    object Loading : TeamPlayersUiState
    data class Success(val players: List<JugadorConUsuario>) : TeamPlayersUiState
    data class Error(val message: String) : TeamPlayersUiState
}

// Estados para el diálogo de jugadores sin equipo
sealed interface UnassignedPlayersUiState {
    object Loading : UnassignedPlayersUiState
    data class Success(val players: List<JugadorConUsuario>) : UnassignedPlayersUiState
    data class Error(val message: String) : UnassignedPlayersUiState
}

class TeamDetailViewModel(
    private val usuarioRepository: UsuarioRepository = UsuarioRepository
) : ViewModel() {

    private val _playersUiState = MutableStateFlow<TeamPlayersUiState>(TeamPlayersUiState.Loading)
    val playersUiState: StateFlow<TeamPlayersUiState> = _playersUiState.asStateFlow()

    private val _unassignedUiState = MutableStateFlow<UnassignedPlayersUiState>(UnassignedPlayersUiState.Loading)
    val unassignedUiState: StateFlow<UnassignedPlayersUiState> = _unassignedUiState.asStateFlow()

    // Cargar jugadores que pertenecen a este equipo (puedes crear el método en tu repo equivalente a getMisRepresentados pero por equipo_id)
    fun fetchTeamPlayers(teamId: String) {
        viewModelScope.launch {
            _playersUiState.value = TeamPlayersUiState.Loading
            try {
                val result = usuarioRepository.getJugadoresConEquipoConUsuario(teamId)

               if(result.isSuccess) {
                   _playersUiState.value = TeamPlayersUiState.Success(result.getOrDefault(emptyList()))
               }

            } catch (e: Exception) {
                _playersUiState.value = TeamPlayersUiState.Error(e.localizedMessage ?: "Error al cargar jugadores")
            }
        }
    }

    // Cargar los jugadores libres usando tu repositorio real
    fun fetchUnassignedPlayers() {
        viewModelScope.launch {
            _unassignedUiState.value = UnassignedPlayersUiState.Loading
            val result = usuarioRepository.getJugadoresSinEquipoConUsuario()

            if (result.isSuccess) {
                _unassignedUiState.value = UnassignedPlayersUiState.Success(result.getOrDefault(emptyList()))
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Error al cargar jugadores libres"
                _unassignedUiState.value = UnassignedPlayersUiState.Error(errorMsg)
            }
        }
    }

    // Asignar jugador actualizando su equipo_id en Supabase
    fun assignPlayerToTeam(playerId: String, teamId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = usuarioRepository.assignPlayerToTeam(playerId, teamId)
                if(result.isSuccess) {
                    onComplete()
                    fetchTeamPlayers(teamId) // Recargar la lista del equipo
                } else {
                    // opcional
                }

            } catch (e: Exception) {
                // Manejar error de asignación
            }
        }
    }
}