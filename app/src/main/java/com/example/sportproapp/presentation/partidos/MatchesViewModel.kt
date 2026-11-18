package com.example.sportproapp.presentation.partidos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.DesafioPartido
import com.example.sportproapp.data.model.EstadoDesafio
import com.example.sportproapp.data.model.PartidoConDetalles
import com.example.sportproapp.data.remote.supabase.ConvocatoriaRepository
import com.example.sportproapp.data.remote.supabase.DesafioPartidoRepository
import com.example.sportproapp.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MatchesUiState {
    object Loading : MatchesUiState
    data class Success(
        val partidosProgramados: List<PartidoConDetalles>,
        val desafiosRecibidos: List<DesafioPartido>
    ) : MatchesUiState
    data class Error(val message: String) : MatchesUiState
}

class MatchesViewModel(
    private val desafioRepository: DesafioPartidoRepository = DesafioPartidoRepository,
    private val convocatoriaRepository: ConvocatoriaRepository = ConvocatoriaRepository
) : ViewModel() {

    private companion object {
        private const val TAG = "MatchesViewModel"
    }

    private val _uiState = MutableStateFlow<MatchesUiState>(MatchesUiState.Loading)
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    private val _isProcessingAction = MutableStateFlow(false)
    val isProcessingAction: StateFlow<Boolean> = _isProcessingAction.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        val currentUser = UserSessionManager.getCurrentUser()
        val academiaId = currentUser?.academiaId ?: ""
        val userId = currentUser?.uid ?: ""
        val userRole = currentUser?.role?.uppercase() ?: ""

        if (academiaId.isBlank()) {
            _uiState.value = MatchesUiState.Error("No se encontró academia asociada en la sesión.")
            return
        }

        _uiState.value = MatchesUiState.Loading

        viewModelScope.launch {
            val resDesafios = desafioRepository.getDesafiosRecibidos(academiaId)
            val resPartidos = desafioRepository.getPartidosConDetallesPorAcademia(academiaId)

            var listaPartidos = resPartidos.getOrDefault(emptyList())
            val listaDesafios = resDesafios.getOrDefault(emptyList())

            // Si el usuario es un jugador (JUG), filtramos para mostrar únicamente los partidos en los que ha sido convocado
            if (userRole == "JUG" && userId.isNotBlank()) {
                val resConvocatorias = convocatoriaRepository.getConvocatoriasParaJugador(userId)
                val partidosConvocadosIds = resConvocatorias.getOrDefault(emptyList()).map { it.partidoId }.toSet()
                listaPartidos = listaPartidos.filter { it.id in partidosConvocadosIds }
            }

            _uiState.value = MatchesUiState.Success(
                partidosProgramados = listaPartidos,
                desafiosRecibidos = listaDesafios
            )
        }
    }

    fun aceptarDesafio(desafioId: Long, equipoReceptorId: String?) {
        _isProcessingAction.value = true
        viewModelScope.launch {
            desafioRepository.responderDesafio(
                desafioId = desafioId,
                nuevoEstado = EstadoDesafio.ACEPTADO,
                equipoReceptorId = equipoReceptorId
            ).fold(
                onSuccess = {
                    Log.d(TAG, "Desafío $desafioId aceptado. Recargando lista...")
                    _isProcessingAction.value = false
                    cargarDatos() // Recarga automáticamente para mostrar el nuevo partido programado
                },
                onFailure = { ex ->
                    Log.e(TAG, "Error al aceptar desafío", ex)
                    _isProcessingAction.value = false
                }
            )
        }
    }

    fun rechazarDesafio(desafioId: Long) {
        _isProcessingAction.value = true
        viewModelScope.launch {
            desafioRepository.responderDesafio(
                desafioId = desafioId,
                nuevoEstado = EstadoDesafio.RECHAZADO
            ).fold(
                onSuccess = {
                    _isProcessingAction.value = false
                    cargarDatos()
                },
                onFailure = { ex ->
                    Log.e(TAG, "Error al rechazar desafío", ex)
                    _isProcessingAction.value = false
                }
            )
        }
    }
}
