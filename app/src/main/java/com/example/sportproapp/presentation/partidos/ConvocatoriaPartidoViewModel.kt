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

data class JugadorConvocatoriaUI(
    val jugadorId: String,
    val nombre: String,
    val posicion: String,
    val fotografiaUrl: String?,
    val isSelected: Boolean
)

sealed interface ConvocatoriaUiState {
    object Loading : ConvocatoriaUiState
    data class Success(
        val partido: PartidoConDetalles?,
        val jugadores: List<JugadorConvocatoriaUI>,
        val jugadoresFiltrados: List<JugadorConvocatoriaUI>,
        val filtroPosicion: String
    ) : ConvocatoriaUiState
    data class Error(val message: String) : ConvocatoriaUiState
}

class ConvocatoriaPartidoViewModel(
    private val desafioRepository: DesafioPartidoRepository = DesafioPartidoRepository,
    private val usuarioRepository: UsuarioRepository = UsuarioRepository,
    private val convocatoriaRepository: ConvocatoriaRepository = ConvocatoriaRepository
) : ViewModel() {

    private companion object {
        private const val TAG = "ConvocatoriaViewModel"
    }

    private val _uiState = MutableStateFlow<ConvocatoriaUiState>(ConvocatoriaUiState.Loading)
    val uiState: StateFlow<ConvocatoriaUiState> = _uiState.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private var allJugadores = listOf<JugadorConvocatoriaUI>()
    private var currentPartido: PartidoConDetalles? = null
    private var currentFiltro = "Todos"

    fun cargarDatos(partidoId: Long, equipoId: String) {
        _uiState.value = ConvocatoriaUiState.Loading
        viewModelScope.launch {
            // 1. Cargar detalles del partido
            val resPartidos = desafioRepository.getPartidosConDetallesPorAcademia("")
            val partidosList = resPartidos.getOrDefault(emptyList())
            currentPartido = partidosList.firstOrNull { it.id == partidoId }

            // 2. Cargar convocatorias existentes para este partido y equipo
            val resConvocatorias = convocatoriaRepository.getConvocatorias(partidoId, equipoId)
            val convocadasIds = resConvocatorias.getOrDefault(emptyList()).map { it.jugadorId }.toSet()
            val hasExistingConvocatorias = convocadasIds.isNotEmpty()

            // 3. Cargar jugadores del equipo
            val resJugadores = usuarioRepository.getJugadoresConEquipoConUsuario(equipoId)
            resJugadores.fold(
                onSuccess = { listaConUsuario ->
                    allJugadores = listaConUsuario.map { item ->
                        val jId = item.perfil.id
                        val selected = if (hasExistingConvocatorias) convocadasIds.contains(jId) else true
                        JugadorConvocatoriaUI(
                            jugadorId = jId,
                            nombre = item.usuario.name.ifBlank { "Jugador" },
                            posicion = item.perfil.posicion.ifBlank { "Sin posición" },
                            fotografiaUrl = item.perfil.fotografiaUrl,
                            isSelected = selected
                        )
                    }
                    actualizarEstadoUI()
                },
                onFailure = { ex ->
                    Log.e(TAG, "Error al cargar jugadores para convocatoria", ex)
                    val resGeneral = usuarioRepository.getJugadoresConEquipoConUsuario("")
                    val fallbackList = resGeneral.getOrDefault(emptyList())
                    allJugadores = fallbackList.map { item ->
                        val jId = item.perfil.id
                        val selected = if (hasExistingConvocatorias) convocadasIds.contains(jId) else true
                        JugadorConvocatoriaUI(
                            jugadorId = jId,
                            nombre = item.usuario.name.ifBlank { "Jugador" },
                            posicion = item.perfil.posicion.ifBlank { "Sin posición" },
                            fotografiaUrl = item.perfil.fotografiaUrl,
                            isSelected = selected
                        )
                    }
                    actualizarEstadoUI()
                }
            )
        }
    }

    fun setFiltroPosicion(filtro: String) {
        currentFiltro = filtro
        actualizarEstadoUI()
    }

    fun toggleSeleccion(jugadorId: String) {
        allJugadores = allJugadores.map {
            if (it.jugadorId == jugadorId) it.copy(isSelected = !it.isSelected) else it
        }
        actualizarEstadoUI()
    }

    private fun actualizarEstadoUI() {
        val filtrados = if (currentFiltro == "Todos") {
            allJugadores
        } else {
            allJugadores.filter { it.posicion.contains(currentFiltro, ignoreCase = true) }
        }
        _uiState.value = ConvocatoriaUiState.Success(
            partido = currentPartido,
            jugadores = allJugadores,
            jugadoresFiltrados = filtrados,
            filtroPosicion = currentFiltro
        )
    }

    fun enviarConvocatoria(partidoId: Long, equipoId: String, onExito: () -> Unit) {
        _isSending.value = true
        viewModelScope.launch {
            val seleccionados = allJugadores.filter { it.isSelected }
            val convocatoriasList = seleccionados.map { jugador ->
                Convocatoria(
                    partidoId = partidoId,
                    equipoId = equipoId,
                    jugadorId = jugador.jugadorId,
                    esTitular = false,
                    posicion = jugador.posicion,
                    estado = "CONVOCADO"
                )
            }

            Log.d(TAG, "Guardando ${convocatoriasList.size} convocatorias para partido $partidoId y equipo $equipoId")
            val result = convocatoriaRepository.guardarConvocatorias(convocatoriasList)
            _isSending.value = false

            result.fold(
                onSuccess = {
                    Log.d(TAG, "Convocatorias guardadas exitosamente en Supabase")
                    onExito()
                },
                onFailure = { ex ->
                    Log.e(TAG, "Error al guardar convocatorias", ex)
                }
            )
        }
    }
}
