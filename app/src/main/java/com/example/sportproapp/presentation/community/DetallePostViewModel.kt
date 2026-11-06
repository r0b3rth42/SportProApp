package com.example.sportproapp.presentation.community

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.ComentarioConAutor
import com.example.sportproapp.data.model.PublicacionConAutor
import com.example.sportproapp.data.remote.supabase.PublicacionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetallePostUiState {
    object Loading : DetallePostUiState
    data class Success(
        val publicacionConAutor: PublicacionConAutor,
        val comentarios: List<ComentarioConAutor>,
        val textoComentario: String = "",
        val responderAId: String? = null,
        val responderANombre: String? = null
    ) : DetallePostUiState
    data class Error(val message: String) : DetallePostUiState
}

class DetallePostViewModel(
    private val repository: PublicacionRepository = PublicacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetallePostUiState>(DetallePostUiState.Loading)
    val uiState: StateFlow<DetallePostUiState> = _uiState.asStateFlow()

    private var currentPublicacionId: String = ""
    private var usuarioActualId: String = ""

    fun cargarDetalle(publicacionId: String) {
        currentPublicacionId = publicacionId
        usuarioActualId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        viewModelScope.launch {
            _uiState.value = DetallePostUiState.Loading
            recargarComentarios()
        }
    }

    fun recargarComentarios() {
        viewModelScope.launch {
            val pubRes = repository.getPublicacionPorId(currentPublicacionId, usuarioActualId)
            val comRes = repository.getComentariosConAutor(currentPublicacionId, usuarioActualId)

            if (pubRes.isSuccess && comRes.isSuccess) {
                val currentState = _uiState.value as? DetallePostUiState.Success
                _uiState.value = DetallePostUiState.Success(
                    publicacionConAutor = pubRes.getOrThrow(),
                    comentarios = comRes.getOrThrow(),
                    textoComentario = currentState?.textoComentario ?: "",
                    responderAId = currentState?.responderAId,
                    responderANombre = currentState?.responderANombre
                )
            } else {
                _uiState.value = DetallePostUiState.Error("Error al cargar detalles de la publicación")
            }
        }
    }

    fun updateTextoComentario(text: String) {
        val currentState = _uiState.value as? DetallePostUiState.Success ?: return
        _uiState.value = currentState.copy(textoComentario = text)
    }

    fun seleccionarRespuesta(comentarioId: String, autorNombre: String) {
        val currentState = _uiState.value as? DetallePostUiState.Success ?: return
        _uiState.value = currentState.copy(
            responderAId = comentarioId,
            responderANombre = autorNombre
        )
    }

    fun cancelarRespuesta() {
        val currentState = _uiState.value as? DetallePostUiState.Success ?: return
        _uiState.value = currentState.copy(
            responderAId = null,
            responderANombre = null
        )
    }

    fun enviarComentario() {
        val currentState = _uiState.value as? DetallePostUiState.Success ?: return
        val texto = currentState.textoComentario.trim()
        if (texto.isBlank()) return

        viewModelScope.launch {
            val res = repository.agregarComentario(
                publicacionId = currentPublicacionId,
                autorId = usuarioActualId,
                contenido = texto,
                comentarioPadreId = currentState.responderAId
            )

            if (res.isSuccess) {
                _uiState.value = currentState.copy(
                    textoComentario = "",
                    responderAId = null,
                    responderANombre = null
                )
                recargarComentarios()
            } else {
                Log.e("DetallePostVM", "Error al agregar comentario: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun toggleLikePost() {
        viewModelScope.launch {
            repository.toggleLikePublicacion(currentPublicacionId, usuarioActualId)
            recargarComentarios()
        }
    }

    fun toggleLikeComentario(comentarioId: String) {
        viewModelScope.launch {
            repository.toggleLikeComentario(comentarioId, usuarioActualId)
            recargarComentarios()
        }
    }
}
