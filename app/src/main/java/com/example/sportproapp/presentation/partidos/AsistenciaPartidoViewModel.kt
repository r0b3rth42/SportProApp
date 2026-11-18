package com.example.sportproapp.presentation.partidos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.Convocatoria
import com.example.sportproapp.data.model.PartidoConDetalles
import com.example.sportproapp.data.remote.supabase.ConvocatoriaRepository
import com.example.sportproapp.data.remote.supabase.DesafioPartidoRepository
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JugadorConvocatoriaEstadoUI(
    val jugadorId: String,
    val nombre: String,
    val posicion: String,
    val fotografiaUrl: String?,
    val estado: String // "CONVOCADO", "CONFIRMADO", "DUDA", "BAJA"
)

sealed interface AsistenciaPartidoUiState {
    object Loading : AsistenciaPartidoUiState
    data class Success(
        val partido: PartidoConDetalles?,
        val equipoNombre: String,
        val jugadoresEstado: List<JugadorConvocatoriaEstadoUI>
    ) : AsistenciaPartidoUiState
    data class Error(val message: String) : AsistenciaPartidoUiState
}

class AsistenciaPartidoViewModel(
    private val desafioRepository: DesafioPartidoRepository = DesafioPartidoRepository,
    private val convocatoriaRepository: ConvocatoriaRepository = ConvocatoriaRepository,
    private val usuarioRepository: UsuarioRepository = UsuarioRepository
) : ViewModel() {

    private companion object {
        private const val TAG = "AsistenciaPartidoVM"
    }

    private val _uiState = MutableStateFlow<AsistenciaPartidoUiState>(AsistenciaPartidoUiState.Loading)
    val uiState: StateFlow<AsistenciaPartidoUiState> = _uiState.asStateFlow()

    fun cargarEstadoConvocatorias(partidoId: Long, equipoId: String) {
        _uiState.value = AsistenciaPartidoUiState.Loading
        viewModelScope.launch {
            // 1. Cargar detalles del partido
            val resPartidos = desafioRepository.getPartidosConDetallesPorAcademia("")
            val partidosList = resPartidos.getOrDefault(emptyList())
            val partido = partidosList.firstOrNull { it.id == partidoId }

            // 2. Cargar convocatorias para el partido
            val resConvocatorias = convocatoriaRepository.getConvocatoriasParaPartido(partidoId)
            val convocatoriasList: List<Convocatoria> = resConvocatorias.getOrDefault(emptyList())

            // 3. Mapear cada convocatoria al estado del jugador obteniendo sus datos de usuario y perfil
            val listaEstadoUI = mutableListOf<JugadorConvocatoriaEstadoUI>()
            for (conv in convocatoriasList) {
                if (equipoId.isBlank() || conv.equipoId == equipoId) {
                    val usuarioRes = usuarioRepository.getUser(conv.jugadorId).getOrNull()
                    val perfilRes = usuarioRepository.getPlayerProfile(conv.jugadorId).getOrNull()

                    val posStr = perfilRes?.posicion?.ifBlank { null } ?: conv.posicion ?: "Sin posición"
                    listaEstadoUI.add(
                        JugadorConvocatoriaEstadoUI(
                            jugadorId = conv.jugadorId,
                            nombre = usuarioRes?.name?.ifBlank { null } ?: "Jugador",
                            posicion = posStr,
                            fotografiaUrl = perfilRes?.fotografiaUrl,
                            estado = conv.estado.uppercase()
                        )
                    )
                }
            }

            _uiState.value = AsistenciaPartidoUiState.Success(
                partido = partido,
                equipoNombre = partido?.equipoLocalNombre ?: "Club Tigres Sub-15",
                jugadoresEstado = listaEstadoUI
            )
        }
    }
}
