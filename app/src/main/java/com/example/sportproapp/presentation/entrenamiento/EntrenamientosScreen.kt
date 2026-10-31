package com.example.sportproapp.presentation.entrenamiento

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

val BackgroundColor = Color(0xFF09120F)
val CardBackgroundColor = Color(0xFF0F1E19)
val PrimaryGreen = Color(0xFFCCFF00)
val TextGray = Color(0xFF8A9E97)
val CardBorderColor = Color(0xFF1B2E28)

@Composable
fun EntrenamientosScreen(
    navController: NavController,
    onAddClick: () -> Unit,
    viewModel: EntrenamientosViewModel = viewModel()
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
            // Header: Título y Subtítulo
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Entrenamientos",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Planificación semanal",
                    color = TextGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Calendario Semanal
            item {
                SelectorDiasSemana()
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Subtítulo
            item {
                Text(
                    text = "Sesiones registradas",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Renderizado condicional según UiState desde Supabase
            when (val state = uiState) {
                is EntrenamientosUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryGreen)
                        }
                    }
                }
                is EntrenamientosUiState.Error -> {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.fetchEntrenamientos() },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                ) {
                                    Text("Reintentar", color = Color.Black)
                                }
                            }
                        }
                    }
                }
                is EntrenamientosUiState.Success -> {
                    if (state.entrenamientos.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay entrenamientos programados.",
                                    color = TextGray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(state.entrenamientos) { entrenamiento ->
                            TarjetaEntrenamiento(entrenamiento)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(90.dp)) }
        }

        // Botón Floating Action (+)
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = PrimaryGreen,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 20.dp)
                .size(60.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar Entrenamiento",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun SelectorDiasSemana() {
    val dias = listOf(
        "L" to "13",
        "M" to "14",
        "M" to "15",
        "J" to "16",
        "V" to "17",
        "S" to "18",
        "D" to "19"
    )
    val diaSeleccionado = "15"

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(dias) { (letra, numero) ->
            val esSeleccionado = numero == diaSeleccionado
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .size(width = 46.dp, height = 64.dp)
                    .background(
                        color = if (esSeleccionado) PrimaryGreen else CardBackgroundColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (esSeleccionado) Color.Transparent else CardBorderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Text(
                    text = letra,
                    color = if (esSeleccionado) Color.Black else TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = numero,
                    color = if (esSeleccionado) Color.Black else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TarjetaEntrenamiento(entrenamiento: Entrenamiento) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Horario y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${entrenamiento.horaIni} - ${entrenamiento.horaFin}",
                        color = PrimaryGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .border(1.dp, PrimaryGreen, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = entrenamiento.estado.toString(),
                        color = when (entrenamiento.estado) {
                            1 -> PrimaryGreen
                            2 -> TextGray
                            3 -> Color(0xFF00FF66)
                            else -> { Color(0xFF00FF66)}
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CardBorderColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Equipo
            Text(
                text = entrenamiento.equipoId,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Ubicación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = entrenamiento.sedeId,
                        color = TextGray,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = entrenamiento.fecha,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}