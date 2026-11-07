package com.example.sportproapp.presentation.community

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.MiembroComunidad
import com.example.sportproapp.data.model.PublicacionConAutor
import com.example.sportproapp.data.remote.supabase.PublicacionRepository
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MuroUiState {
    object Loading : MuroUiState
    data class Success(
        val publicaciones: List<PublicacionConAutor>,
        val publicacionesPendientes: List<PublicacionConAutor> = emptyList(),
        val misCambiosSolicitados: List<PublicacionConAutor> = emptyList(),
        val miembros: List<MiembroComunidad>,
        val filtroSeleccionado: String,
        val pendientesCount: Int = 0,
        val esAdmin: Boolean = false,
        val academiaNombre: String = "Club Tigres",
        val errorMensaje: String? = null
    ) : MuroUiState
    data class Error(val message: String) : MuroUiState
}

class MuroComunidadViewModel(
    private val repository: PublicacionRepository = PublicacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MuroUiState>(MuroUiState.Loading)
    val uiState: StateFlow<MuroUiState> = _uiState.asStateFlow()

    private var currentFiltro: String = "Todos"
    private var academiaId: String = ""
    private var usuarioActualId: String = ""
    private var userRole: String = "JUG"

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        usuarioActualId = currentUser?.uid ?: ""

        if (usuarioActualId.isBlank()) {
            _uiState.value = MuroUiState.Error("Usuario no autenticado")
            return
        }

        viewModelScope.launch {
            _uiState.value = MuroUiState.Loading

            val usuario = UsuarioRepository.getUser(usuarioActualId).getOrNull()
            userRole = usuario?.role ?: "JUG"

            academiaId = repository.obtenerAcademiaIdDelUsuario(usuarioActualId)
            Log.d("MuroViewModel", "Academia ID resuelta: $academiaId, Rol: $userRole")

            cargarFeed()
        }
    }

    fun seleccionarFiltro(filtro: String) {
        currentFiltro = filtro
        cargarFeed()
    }

    fun cargarFeed() {
        viewModelScope.launch {
            if (academiaId.isBlank()) {
                academiaId = repository.obtenerAcademiaIdDelUsuario(usuarioActualId)
            }

            val esAdmin = (userRole == "ADM")

            // Si es Admin, obtiene la lista de publicaciones pendientes de aprobación
            val pendientesLista = if (esAdmin) {
                repository.getPublicacionesPendientes(academiaId).getOrNull() ?: emptyList()
            } else emptyList()

            // Obtiene publicaciones propias donde el Administrador solicitó cambios
            val misCambios = repository.getMisPublicacionesConCambiosSolicitados(academiaId, usuarioActualId)
                .getOrNull() ?: emptyList()

            // El muro del feed ("Todos", "Mi equipo", "Anuncios") obtiene ESTRICTAMENTE publicaciones APROBADAS
            val publicacionesFeed = if (currentFiltro.startsWith("Pendientes")) {
                pendientesLista
            } else {
                val res = repository.getPublicacionesAprobadas(
                    academiaId = academiaId,
                    usuarioActualId = usuarioActualId,
                    filtro = currentFiltro
                )
                res.getOrNull() ?: emptyList()
            }

            val miembrosRes = repository.getMiembrosComunidad(academiaId, usuarioActualId)
            val nombreAcademia = if (academiaId.isNotBlank()) repository.obtenerNombreAcademia(academiaId) else "Club"

            _uiState.value = MuroUiState.Success(
                publicaciones = publicacionesFeed,
                publicacionesPendientes = pendientesLista,
                misCambiosSolicitados = misCambios,
                miembros = miembrosRes.getOrNull() ?: emptyList(),
                filtroSeleccionado = currentFiltro,
                pendientesCount = pendientesLista.size,
                esAdmin = esAdmin,
                academiaNombre = nombreAcademia
            )
        }
    }

    fun toggleLike(publicacionId: String) {
        viewModelScope.launch {
            repository.toggleLikePublicacion(publicacionId, usuarioActualId)
            cargarFeed()
        }
    }

    fun crearPublicacion(contenido: String, tipo: String) {
        if (contenido.isBlank()) return
        viewModelScope.launch {
            if (academiaId.isBlank()) {
                academiaId = repository.obtenerAcademiaIdDelUsuario(usuarioActualId)
            }

            val res = repository.crearPublicacion(
                academiaId = academiaId,
                autorId = usuarioActualId,
                contenido = contenido.trim(),
                tipo = tipo
            )

            if (res.isSuccess) {
                Log.d("MuroViewModel", "Publicación enviada a aprobación correctamente")
                cargarFeed()
            } else {
                val errMsg = res.exceptionOrNull()?.message ?: "Error al crear publicación"
                Log.e("MuroViewModel", "Error al crear publicación: $errMsg")
                val currentState = _uiState.value as? MuroUiState.Success
                if (currentState != null) {
                    _uiState.value = currentState.copy(errorMensaje = "No se pudo guardar: $errMsg")
                } else {
                    _uiState.value = MuroUiState.Error(errMsg)
                }
            }
        }
    }

    fun editarYReenviarPublicacion(publicacionId: String, nuevoContenido: String) {
        if (nuevoContenido.isBlank()) return
        viewModelScope.launch {
            val res = repository.editarYReenviarPublicacion(publicacionId, nuevoContenido.trim())
            if (res.isSuccess) {
                Log.d("MuroViewModel", "Publicación corregida y re-enviada")
                cargarFeed()
            } else {
                Log.e("MuroViewModel", "Error al corregir publicación: ${res.exceptionOrNull()?.message}")
            }
        }
    }
}
