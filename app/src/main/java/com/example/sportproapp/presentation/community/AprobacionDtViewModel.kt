package com.example.sportproapp.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.PublicacionConAutor
import com.example.sportproapp.data.remote.supabase.PublicacionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AprobacionUiState {
    object Loading : AprobacionUiState
    data class Success(
        val publicacionConAutor: PublicacionConAutor,
        val textoCronica: String,
        val notasInternas: String
    ) : AprobacionUiState
    data class Error(val message: String) : AprobacionUiState
    object Finished : AprobacionUiState
}

class AprobacionDtViewModel(
    private val repository: PublicacionRepository = PublicacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AprobacionUiState>(AprobacionUiState.Loading)
    val uiState: StateFlow<AprobacionUiState> = _uiState.asStateFlow()

    private var currentPublicacionId: String = ""
    private var usuarioActualId: String = ""

    fun cargarPublicacion(publicacionId: String) {
        currentPublicacionId = publicacionId
        usuarioActualId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        viewModelScope.launch {
            _uiState.value = AprobacionUiState.Loading
            val result = repository.getPublicacionPorId(publicacionId, usuarioActualId)

            if (result.isSuccess) {
                val pub = result.getOrThrow()
                _uiState.value = AprobacionUiState.Success(
                    publicacionConAutor = pub,
                    textoCronica = pub.publicacion.contenido,
                    notasInternas = pub.publicacion.notasInternas ?: ""
                )
            } else {
                _uiState.value = AprobacionUiState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: "Error al cargar la publicación"
                )
            }
        }
    }

    fun updateTextoCronica(newText: String) {
        val currentState = _uiState.value
        if (currentState is AprobacionUiState.Success) {
            _uiState.value = currentState.copy(textoCronica = newText)
        }
    }

    fun updateNotasInternas(newNotes: String) {
        val currentState = _uiState.value
        if (currentState is AprobacionUiState.Success) {
            _uiState.value = currentState.copy(notasInternas = newNotes)
        }
    }

    fun aprobarYPublicar() {
        val currentState = _uiState.value as? AprobacionUiState.Success ?: return
        viewModelScope.launch {
            val res = repository.aprobarYPublicar(
                publicacionId = currentPublicacionId,
                textoOficial = currentState.textoCronica,
                notasInternas = currentState.notasInternas
            )
            if (res.isSuccess) {
                _uiState.value = AprobacionUiState.Finished
            } else {
                _uiState.value = AprobacionUiState.Error("No se pudo aprobar la publicación")
            }
        }
    }

    fun solicitarCambios() {
        val currentState = _uiState.value as? AprobacionUiState.Success ?: return
        viewModelScope.launch {
            val res = repository.solicitarCambios(
                publicacionId = currentPublicacionId,
                textoOficial = currentState.textoCronica,
                notasInternas = currentState.notasInternas
            )
            if (res.isSuccess) {
                _uiState.value = AprobacionUiState.Finished
            } else {
                _uiState.value = AprobacionUiState.Error("No se pudieron solicitar cambios")
            }
        }
    }

    fun rechazar() {
        val currentState = _uiState.value as? AprobacionUiState.Success ?: return
        viewModelScope.launch {
            val res = repository.rechazarPublicacion(
                publicacionId = currentPublicacionId,
                notasInternas = currentState.notasInternas
            )
            if (res.isSuccess) {
                _uiState.value = AprobacionUiState.Finished
            } else {
                _uiState.value = AprobacionUiState.Error("No se pudo rechazar la publicación")
            }
        }
    }
}
