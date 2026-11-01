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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportproapp.data.remote.supabase.EntrenamientoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EntrenamientosPorEquipoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<EntrenamientosUiState>(EntrenamientosUiState.Loading)
    val uiState: StateFlow<EntrenamientosUiState> = _uiState.asStateFlow()

    fun cargarEntrenamientos(equipoId: String) {
        _uiState.value = EntrenamientosUiState.Loading
        viewModelScope.launch {
            val result = EntrenamientoRepository.getEntrenamientosPorEquipo(equipoId)
            result.fold(
                onSuccess = { lista -> _uiState.value = EntrenamientosUiState.Success(lista) },
                onFailure = { ex -> _uiState.value = EntrenamientosUiState.Error(ex.message ?: "Error al cargar entrenamientos") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrenamientosPorEquipoScreen(
    navController: NavController,
    equipoId: String,
    equipoNombre: String,
    onAddClick: (equipoId: String) -> Unit = { id -> navController.navigate("entrenamiento-create/$id") },
    onEntrenamientoClick: (entrenamientoId: String) -> Unit = { id -> navController.navigate("detalle_entrenamiento/$id") },
    viewModel: EntrenamientosPorEquipoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(equipoId) {
        viewModel.cargarEntrenamientos(equipoId)
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = equipoNombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "Sesiones de entrenamiento", color = TextGray, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddClick(equipoId) },
                containerColor = PrimaryGreen,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Entrenamiento",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SelectorDiasSemana()
                Spacer(modifier = Modifier.height(20.dp))
            }

            when (val state = uiState) {
                is EntrenamientosUiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryGreen)
                        }
                    }
                }
                is EntrenamientosUiState.Error -> {
                    item {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is EntrenamientosUiState.Success -> {
                    if (state.entrenamientos.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No hay entrenamientos programados para este equipo.", color = TextGray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        items(state.entrenamientos) { entrenamiento ->
                            TarjetaEntrenamientoItem(
                                entrenamiento = entrenamiento,
                                equipoNombre = equipoNombre,
                                onClick = {
                                    entrenamiento.id?.let { id -> onEntrenamientoClick(id) }
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(90.dp)) }
        }
    }
}

@Composable
fun TarjetaEntrenamientoItem(
    entrenamiento: Entrenamiento,
    equipoNombre: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${entrenamiento.horaIni} - ${entrenamiento.horaFin}",
                        color = PrimaryGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val (textoEstado, colorEstado) = when (entrenamiento.estado) {
                    1 -> "En Curso" to PrimaryGreen
                    2 -> "Planificado" to TextGray
                    3 -> "Completado" to Color(0xFF00FF66)
                    else -> "Programado" to PrimaryGreen
                }

                Box(
                    modifier = Modifier
                        .border(1.dp, colorEstado, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = textoEstado, color = colorEstado, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CardBorderColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Text(text = equipoNombre, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = entrenamiento.sedeId, color = TextGray, fontSize = 13.sp)
                }

                Text(text = entrenamiento.fecha, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}