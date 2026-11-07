package com.example.sportproapp.presentation.entrenamiento

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel para listar equipos
sealed interface EquiposUiState {
    object Loading : EquiposUiState
    data class Success(val equipos: List<Equipo>) : EquiposUiState
    data class Error(val message: String) : EquiposUiState
}

class EquiposEntrenamientoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<EquiposUiState>(EquiposUiState.Loading)
    val uiState: StateFlow<EquiposUiState> = _uiState.asStateFlow()

    init { fetchEquipos() }

    fun fetchEquipos() {
        _uiState.value = EquiposUiState.Loading
        viewModelScope.launch {
            val result = EquipoRepository.getEquipos()
            result.fold(
                onSuccess = { lista -> _uiState.value = EquiposUiState.Success(lista) },
                onFailure = { ex -> _uiState.value = EquiposUiState.Error(ex.message ?: "Error al cargar equipos") }
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
                        Button(onClick = { viewModel.fetchEquipos() }) { Text("Reintentar") }
                    }
                }
                is EquiposUiState.Success -> {
                    if (state.equipos.isEmpty()) {
                        item {
                            Text("No hay equipos registrados", color = TextGray)
                        }
                    } else {
                        items(state.equipos) { equipo ->
                            TarjetaEquipoItem(equipo = equipo) {
                                navController.navigate("entrenamientos_equipo/${equipo.id}/${equipo.nombre}")
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
                    try { Color(android.graphics.Color.parseColor(equipo.stripeColorHex)) }
                    catch (e: Exception) { PrimaryGreen }
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