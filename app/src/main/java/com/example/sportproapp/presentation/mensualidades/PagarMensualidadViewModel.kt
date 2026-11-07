package com.example.sportproapp.presentation.mensualidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.model.DetalleMensualidad
import com.example.sportproapp.data.model.MetodoPago
import com.example.sportproapp.data.remote.supabase.MensualidadRepository
import com.example.sportproapp.data.session.UserSessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado de la carga de la cuota y también del detalle del comprobante (4.04)
sealed interface DetalleMensualidadUiState {
    object Loading : DetalleMensualidadUiState
    data class Success(val detalle: DetalleMensualidad) : DetalleMensualidadUiState
    data class Error(val message: String) : DetalleMensualidadUiState
}

sealed interface PagoUiState {
    object Idle : PagoUiState
    object Procesando : PagoUiState
    data class Exito(val mensualidadId: String) : PagoUiState
    data class Error(val message: String) : PagoUiState
}

// 4.03 Pagar mensualidad (pago simulado, US-07)
class PagarMensualidadViewModel(
    private val repository: MensualidadRepository = MensualidadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleMensualidadUiState>(DetalleMensualidadUiState.Loading)
    val uiState: StateFlow<DetalleMensualidadUiState> = _uiState.asStateFlow()

    private val _pagoState = MutableStateFlow<PagoUiState>(PagoUiState.Idle)
    val pagoState: StateFlow<PagoUiState> = _pagoState.asStateFlow()

    private val _metodo = MutableStateFlow(MetodoPago.YAPE_PLIN)
    val metodo: StateFlow<MetodoPago> = _metodo.asStateFlow()

    fun cargarMensualidad(mensualidadId: String) {
        _uiState.value = DetalleMensualidadUiState.Loading
        viewModelScope.launch {
            repository.getDetalleMensualidad(mensualidadId).fold(
                onSuccess = { _uiState.value = DetalleMensualidadUiState.Success(it) },
                onFailure = { ex ->
                    _uiState.value = DetalleMensualidadUiState.Error(ex.message ?: "No se pudo cargar la mensualidad")
                }
            )
        }
    }

    fun seleccionarMetodo(metodo: MetodoPago) {
        _metodo.value = metodo
    }

    fun confirmarPago() {
        val detalle = (_uiState.value as? DetalleMensualidadUiState.Success)?.detalle ?: return
        val uid = UserSessionManager.getCurrentUser()?.uid ?: FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            _pagoState.value = PagoUiState.Error("No se encontró la sesión del usuario.")
            return
        }

        _pagoState.value = PagoUiState.Procesando
        viewModelScope.launch {
            repository.registrarPagoSimulado(detalle.mensualidad, uid, _metodo.value).fold(
                onSuccess = { pago -> _pagoState.value = PagoUiState.Exito(pago.mensualidadId) },
                onFailure = { ex -> _pagoState.value = PagoUiState.Error(ex.message ?: "No se pudo registrar el pago") }
            )
        }
    }

    fun resetPagoState() {
        _pagoState.value = PagoUiState.Idle
    }
}
