package com.example.sportproapp.presentation.entrenamiento

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportproapp.data.model.Equipo
import com.example.sportproapp.data.remote.supabase.EquipoRepository
import com.example.sportproapp.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "EquiposEntrenamiento"

// ViewModel para listar equipos
sealed interface EquiposUiState {
    object Loading : EquiposUiState
    data class Success(val equipos: List<Equipo>) : EquiposUiState
    data class Error(val message: String) : EquiposUiState
}

class EquiposEntrenamientoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<EquiposUiState>(EquiposUiState.Loading)
    val uiState: StateFlow<EquiposUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "ViewModel inicializado")
        fetchEquipos()
    }

    fun fetchEquipos() {
        Log.d(TAG, "fetchEquipos: Iniciando carga de equipos...")
        _uiState.value = EquiposUiState.Loading

        viewModelScope.launch {
            val currentUser = UserSessionManager.getCurrentUser()
            val academiaId = currentUser?.academiaId

            if (academiaId == null) {
                Log.e(TAG, "fetchEquipos: Cancelado porque academiaId es null. Usuario actual: $currentUser")
                _uiState.value = EquiposUiState.Error("No se encontró una academia asociada al usuario")
                return@launch
            }

            Log.d(TAG, "fetchEquipos: Consultando equipos en Supabase para academiaId=$academiaId")
            val result = EquipoRepository.getEquiposByAcademy(academiaId)

            result.fold(
                onSuccess = { lista ->
                    Log.i(TAG, "fetchEquipos: Éxito (${lista.size} equipos obtenidos) -> ${lista.map { "${it.id}:${it.nombre}" }}")
                    _uiState.value = EquiposUiState.Success(lista)
                },
                onFailure = { ex ->
                    Log.e(TAG, "fetchEquipos: Falló la consulta para academiaId=$academiaId", ex)
                    _uiState.value = EquiposUiState.Error(ex.message ?: "Error al cargar equipos")
                }
            )
        }
    }
}

// Pantalla principal de selección de equipo (Sin botón flotante)
@Composable
fun EquiposEntrenamientoScreen(
    navController: NavController,
    viewModel: EquiposEntrenamientoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Trazabilidad de ciclo de vida y transiciones de estado sin spamear en recomposiciones
    LaunchedEffect(Unit) {
        Log.d(TAG, "Screen: EquiposEntrenamientoScreen montada")
    }

    LaunchedEffect(uiState) {
        Log.d(TAG, "Screen: Estado UI actualizado -> ${uiState::class.simpleName}")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Entrenamientos", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = "Selecciona un equipo para ver sus sesiones", color = TextGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(24.dp))
            }

            when (val state = uiState) {
                is EquiposUiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryGreen)
                        }
                    }
                }
                is EquiposUiState.Error -> {
                    item {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = {
                            Log.d(TAG, "Screen: Botón 'Reintentar' presionado")
                            viewModel.fetchEquipos()
                        }) {
                            Text("Reintentar")
                        }
                    }
                }
                is EquiposUiState.Success -> {
                    if (state.equipos.isEmpty()) {
                        item {
                            Log.w(TAG, "Screen: Lista de equipos vacía para la academia actual")
                            Text("No hay equipos registrados", color = TextGray)
                        }
                    } else {
                        items(state.equipos) { equipo ->
                            TarjetaEquipoItem(equipo = equipo) {
                                val rutaDestino = "entrenamientos_equipo/${equipo.id}/${equipo.nombre}"
                                Log.i(TAG, "Screen: Equipo seleccionado [id=${equipo.id}, nombre='${equipo.nombre}']. Navegando a -> $rutaDestino")
                                navController.navigate(rutaDestino)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@Composable
fun TarjetaEquipoItem(equipo: Equipo, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val equipoColor = remember(equipo.stripeColorHex) {
                    try {
                        Color(android.graphics.Color.parseColor(equipo.stripeColorHex))
                    } catch (e: Exception) {
                        Log.w(TAG, "TarjetaEquipoItem: Color hex inválido '${equipo.stripeColorHex}' en equipo '${equipo.nombre}', usando PrimaryGreen", e)
                        PrimaryGreen
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(equipoColor.copy(alpha = 0.2f))
                        .border(1.5.dp, equipoColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = equipoColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = equipo.nombre,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Categoría ${equipo.categoria}",
                        color = TextGray,
                        fontSize = 13.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver entrenamientos",
                tint = TextGray
            )
        }
    }
}