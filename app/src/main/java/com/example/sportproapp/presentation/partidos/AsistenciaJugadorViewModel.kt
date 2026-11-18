package com.example.sportproapp.presentation.partidos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.Convocatoria
import com.example.sportproapp.data.model.PartidoConDetalles
import com.example.sportproapp.data.remote.supabase.ConvocatoriaRepository
import com.example.sportproapp.data.remote.supabase.DesafioPartidoRepository
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import com.example.sportproapp.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AsistenciaJugadorUiState {
    object Loading : AsistenciaJugadorUiState
    data class Success(
        val partido: PartidoConDetalles?,
        val equipoNombre: String,
        val estadoSeleccionado: String, // "CONFIRMADO", "BAJA", "DUDA", "CONVOCADO"
        val comentario: String,
        val jugadoresEstado: List<JugadorConvocatoriaEstadoUI>
    ) : AsistenciaJugadorUiState
    data class Error(val message: String) : AsistenciaJugadorUiState
}

class AsistenciaJugadorViewModel(
    private val desafioRepository: DesafioPartidoRepository = DesafioPartidoRepository,
    private val convocatoriaRepository: ConvocatoriaRepository = ConvocatoriaRepository,
    private val usuarioRepository: UsuarioRepository = UsuarioRepository
) : ViewModel() {

    private companion object {
        private const val TAG = "AsistenciaJugadorVM"
    }

    private val _uiState = MutableStateFlow<AsistenciaJugadorUiState>(AsistenciaJugadorUiState.Loading)
    val uiState: StateFlow<AsistenciaJugadorUiState> = _uiState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun cargarDatos(partidoId: Long, equipoId: String) {
        _uiState.value = AsistenciaJugadorUiState.Loading
        viewModelScope.launch {
            val currentUser = UserSessionManager.getCurrentUser()
            val jugadorId = currentUser?.uid ?: ""

            // 1. Cargar partido
            val resPartidos = desafioRepository.getPartidosConDetallesPorAcademia("")
            val partidosList = resPartidos.getOrDefault(emptyList())
            val partido = partidosList.firstOrNull { it.id == partidoId }

            // 2. Cargar convocatorias del partido
            val resConvocatorias = convocatoriaRepository.getConvocatoriasParaPartido(partidoId)
            val convocatoriasList: List<Convocatoria> = resConvocatorias.getOrDefault(emptyList())

            // Encontrar la respuesta actual del jugador actual
            val miConvocatoria = convocatoriasList.firstOrNull { it.jugadorId == jugadorId }
            val estadoActual = miConvocatoria?.estado?.uppercase() ?: "CONFIRMADO"

            // 3. Mapear estado del equipo
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

            _uiState.value = AsistenciaJugadorUiState.Success(
                partido = partido,
                equipoNombre = partido?.equipoLocalNombre ?: "Club Tigres Sub-15",
                estadoSeleccionado = estadoActual,
                comentario = "",
                jugadoresEstado = listaEstadoUI
            )
        }
    }

    fun seleccionarEstado(nuevoEstado: String) {
        val current = _uiState.value
        if (current is AsistenciaJugadorUiState.Success) {
            _uiState.value = current.copy(estadoSeleccionado = nuevoEstado)
        }
    }

    fun actualizarComentario(comentario: String) {
        val current = _uiState.value
        if (current is AsistenciaJugadorUiState.Success) {
            _uiState.value = current.copy(comentario = comentario)
        }
    }

    fun guardarRespuesta(partidoId: Long, equipoId: String, onExito: () -> Unit) {
        val current = _uiState.value
        if (current !is AsistenciaJugadorUiState.Success) return

        val currentUser = UserSessionManager.getCurrentUser()
        val jugadorId = currentUser?.uid ?: return

        _isSaving.value = true
        viewModelScope.launch {
            val convocatoria = Convocatoria(
                partidoId = partidoId,
                equipoId = equipoId,
                jugadorId = jugadorId,
                estado = current.estadoSeleccionado,
                esTitular = false
            )

            val result = convocatoriaRepository.guardarConvocatorias(listOf(convocatoria))
            _isSaving.value = false

            result.fold(
                onSuccess = {
                    Log.d(TAG, "Respuesta de convocatoria guardada: ${current.estadoSeleccionado}")
                    onExito()
                },
                onFailure = { ex ->
                    Log.e(TAG, "Error al guardar respuesta de convocatoria", ex)
                }
            )
        }
    }
}
