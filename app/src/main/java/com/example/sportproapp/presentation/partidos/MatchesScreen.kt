package com.example.sportproapp.presentation.partidos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportproapp.data.model.DesafioPartido
import com.example.sportproapp.data.model.EstadoDesafio
import com.example.sportproapp.data.model.Partido
import com.example.sportproapp.presentation.auth.*
import com.example.sportproapp.presentation.matches.MatchesUiState
import com.example.sportproapp.presentation.matches.MatchesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    navController: NavController,
    viewModel: MatchesViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isProcessingAction by viewModel.isProcessingAction.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // Pestaña 0: Próximos Partidos por defecto
    val tabs = listOf("Próximos Partidos", "Desafíos Recibidos")

    // Recargar datos al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    Scaffold(
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // --- 1. CABECERA Y TÍTULO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Partidos & Desafíos",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- 2. SELECTOR PESTAÑA DE NAVEGACIÓN ---
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = InputBackgroundColor,
                contentColor = PrimaryLime,
                indicator = {},
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(
                                color = if (isSelected) PrimaryLime else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedTabIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) DarkBackground else TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. CONTENIDO SEGÚN LA PESTAÑA ---
            when (val state = uiState) {
                is MatchesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryLime)
                    }
                }

                is MatchesUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }
                }

                is MatchesUiState.Success -> {
                    if (selectedTabIndex == 0) {
                        // --- PESTAÑA 0: PRÓXIMOS PARTIDOS AMISTOSOS ---
                        val partidos = state.partidosProgramados

                        if (partidos.isEmpty()) {
                            MensajeSinResultados("No tienes partidos amistosos programados.")
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(partidos) { partido ->
                                    ItemPartidoProgramado(partido = partido)
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    } else {
                        // --- PESTAÑA 1: DESAFÍOS RECIBIDOS ---
                        val desafios = state.desafiosRecibidos

                        if (desafios.isEmpty()) {
                            MensajeSinResultados("No has recibido desafíos de otras academias aún.")
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(desafios) { desafio ->
                                    ItemDesafioRecibido(
                                        desafio = desafio,
                                        isProcessing = isProcessingAction,
                                        onAceptar = {
                                            desafio.id?.let { id ->
                                                viewModel.aceptarDesafio(id, desafio.equipoReceptorId)
                                                Toast.makeText(context, "¡Desafío Aceptado! Partido programado.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onRechazar = {
                                            desafio.id?.let { id ->
                                                viewModel.rechazarDesafio(id)
                                                Toast.makeText(context, "Desafío Rechazado.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTE: TARJETA DE PARTIDO PROGRAMADO ---
@Composable
fun ItemPartidoProgramado(partido: Partido) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Tipo e Insignia de Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF162D24),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                ) {
                    Text(
                        text = partido.tipoPartido,
                        color = PrimaryLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                val (estadoTexto, colorEstado) = when (partido.estado.uppercase()) {
                    "EN_CURSO" -> "EN VIVO" to Color(0xFFFF5555)
                    "FINALIZADO" -> "Finalizado" to TextMuted
                    else -> "Programado" to PrimaryLime
                }

                Text(
                    text = estadoTexto,
                    color = colorEstado,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Información del Encuentro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.SportsSoccer,
                    contentDescription = null,
                    tint = PrimaryLime,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Partido Amistoso Programado",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = CardBorderColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Fecha, Hora y Sede
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Event, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = partido.fecha, color = TextLight, fontSize = 12.sp)

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = partido.hora, color = TextLight, fontSize = 12.sp)
                }

                if (!partido.sedeId.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Place, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Sede", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// --- COMPONENTE: TARJETA DE DESAFÍO RECIBIDO ---
@Composable
fun ItemDesafioRecibido(
    desafio: DesafioPartido,
    isProcessing: Boolean,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    val (estadoTexto, colorBadge) = when (desafio.estado) {
        EstadoDesafio.PENDIENTE -> "PENDIENTE" to Color(0xFFFFB74D)
        EstadoDesafio.ACEPTADO -> "ACEPTADO" to PrimaryLime
        EstadoDesafio.RECHAZADO -> "RECHAZADO" to Color(0xFFFF5555)
        EstadoDesafio.CANCELADO -> "CANCELADO" to TextMuted
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.SportsSoccer,
                        contentDescription = null,
                        tint = PrimaryLime,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RETO AMISTOSO ENTRANTE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = InputBackgroundColor,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                ) {
                    Text(
                        text = estadoTexto,
                        color = colorBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Event, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Fecha: ${desafio.fecha}", color = TextLight, fontSize = 13.sp)

                Spacer(modifier = Modifier.width(16.dp))

                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Hora: ${desafio.hora}", color = TextLight, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Place, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Sede propuesta", color = TextLight, fontSize = 13.sp)
            }

            if (!desafio.mensaje.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${desafio.mensaje}\"",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }

            if (desafio.estado == EstadoDesafio.PENDIENTE) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRechazar,
                        enabled = !isProcessing,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2C1616),
                            contentColor = Color(0xFFFF5555)
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onAceptar,
                        enabled = !isProcessing,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryLime,
                            contentColor = DarkBackground
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aceptar reto", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MensajeSinResultados(mensaje: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = mensaje, color = TextMuted, fontSize = 14.sp)
    }
}