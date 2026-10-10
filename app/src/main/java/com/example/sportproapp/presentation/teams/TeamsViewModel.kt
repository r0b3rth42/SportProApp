package com.example.sportproapp.presentation.teams

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Estado de la UI para la lista de equipos
sealed interface TeamsUiState {
    object Loading : TeamsUiState
    data class Success(val teams: List<Team>) : TeamsUiState
    data class Error(val message: String) : TeamsUiState
}

class TeamsViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeamsUiState>(TeamsUiState.Loading)
    val uiState: StateFlow<TeamsUiState> = _uiState.asStateFlow()

    init {
        fetchTeams()
    }

    fun fetchTeams() {
        viewModelScope.launch {
            _uiState.value = TeamsUiState.Loading
            try {
                // Consulta a la colección "teams" en Firestore
                val snapshot = firestore.collection("teams").get().await()

                val teamsList = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: "Equipo sin nombre"
                    val playerCount = doc.getLong("playerCount")?.toInt() ?: 0
                    val coachName = doc.getString("coachName") ?: "Sin entrenador"
                    val hexColor = doc.getString("stripeColorHex") ?: "#CCFF00"
                    val hasSubdivision = doc.getBoolean("hasSubdivision") ?: true

                    // Convertir el Hexadecimal almacenado a Color de Compose
                    val stripeColor = try {
                        Color(android.graphics.Color.parseColor(hexColor))
                    } catch (e: Exception) {
                        Color(0xFFCCFF00) // Color por defecto (PrimaryLime)
                    }

                    Team(
                        id = id,
                        name = name,
                        playerCount = playerCount,
                        coachName = coachName,
                        stripeColor = stripeColor,
                        badgeTag = if (hasSubdivision) "+ sub-división" else "Plantel Único"
                    )
                }

                _uiState.value = TeamsUiState.Success(teamsList)
            } catch (e: Exception) {
                _uiState.value = TeamsUiState.Error(e.localizedMessage ?: "Error al cargar equipos")
            }
        }
    }
}