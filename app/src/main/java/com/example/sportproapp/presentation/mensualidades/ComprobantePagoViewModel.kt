package com.example.sportproapp.presentation.mensualidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportproapp.data.remote.supabase.MensualidadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 4.04 Comprobante de pago
class ComprobantePagoViewModel(
    private val repository: MensualidadRepository = MensualidadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleMensualidadUiState>(DetalleMensualidadUiState.Loading)
    val uiState: StateFlow<DetalleMensualidadUiState> = _uiState.asStateFlow()

    fun cargarComprobante(mensualidadId: String) {
        _uiState.value = DetalleMensualidadUiState.Loading
        viewModelScope.launch {
            repository.getDetalleMensualidad(mensualidadId).fold(
                onSuccess = { detalle ->
                    _uiState.value = if (detalle.pago == null) {
                        DetalleMensualidadUiState.Error("Esta mensualidad aún no tiene un pago registrado.")
                    } else {
                        DetalleMensualidadUiState.Success(detalle)
                    }
                },
                onFailure = { ex ->
                    _uiState.value = DetalleMensualidadUiState.Error(ex.message ?: "No se pudo cargar el comprobante")
                }
            )
        }
    }
}
