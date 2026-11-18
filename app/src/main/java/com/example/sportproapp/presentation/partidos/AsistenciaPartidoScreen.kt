package com.example.sportproapp.presentation.partidos

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sportproapp.presentation.auth.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaPartidoScreen(
    navController: NavController,
    partidoId: Long,
    equipoId: String,
    viewModel: AsistenciaPartidoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(partidoId, equipoId) {
        viewModel.cargarEstadoConvocatorias(partidoId, equipoId)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Asistencia Partido",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        val subtitle = when (val state = uiState) {
                            is AsistenciaPartidoUiState.Success -> state.equipoNombre
                            else -> "Cargando..."
                        }
                        Text(
                            text = subtitle,
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 8.dp)
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkCardBackground)
                            .border(1.dp, CardBorderColor, RoundedCornerShape(10.dp))
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PrimaryLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            when (val state = uiState) {
                is AsistenciaPartidoUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryLime)
                    }
                }
                is AsistenciaPartidoUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is AsistenciaPartidoUiState.Success -> {
                    val partido = state.partido

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // --- 1. TARJETA RESUMEN DEL PARTIDO ---
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, CardBorderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "PARTIDO ${partido?.tipoPartido ?: "AMISTOSO"}",
                                        color = PrimaryLime,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "vs. ${partido?.equipoVisitanteNombre ?: "Equipo Rival"}".trim(),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = CardBorderColor, thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Outlined.Event, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = partido?.fecha ?: "Sábado, 18 de Mayo", color = TextLight, fontSize = 13.sp)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "${partido?.hora ?: "15:30"} (Estar 14:30 en vestuarios)", color = TextLight, fontSize = 13.sp)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Outlined.Place, contentDescription = null, tint = PrimaryLime, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "Sede: ${partido?.sedeNombre?.ifBlank { "Campo Municipal" }}", color = TextLight, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // --- 2. TÍTULO SECCIÓN ESTADO DEL EQUIPO ---
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Estado del equipo",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // --- 3. LISTA DE ESTADOS DE CONVOCATORIA DE JUGADORES ---
                        if (state.jugadoresEstado.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 30.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No hay jugadores convocados aún.", color = TextMuted)
                                }
                            }
                        } else {
                            items(state.jugadoresEstado) { jugador ->
                                ItemJugadorEstadoConvocatoria(jugador = jugador)
                            }
                        }

                        item { Spacer(modifier = Modifier.height(30.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemJugadorEstadoConvocatoria(jugador: JugadorConvocatoriaEstadoUI) {
    val (textoEstado, colorEstado, bgColor) = when (jugador.estado) {
        "CONFIRMADO" -> Triple("Confirmó", PrimaryLime, Color(0xFF162D24))
        "DUDA" -> Triple("Duda", Color(0xFFFFB800), Color(0xFF2C2410))
        "BAJA", "NO_PUEDE" -> Triple("No puede", Color(0xFFFF3333), Color(0xFF2C1616))
        else -> Triple("Convocado", TextMuted, InputBackgroundColor)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar opcional
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(InputBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (!jugador.fotografiaUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = jugador.fotografiaUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = jugador.nombre,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = jugador.posicion,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Insignia de Estado
            Surface(
                color = bgColor,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, colorEstado)
            ) {
                Text(
                    text = textoEstado,
                    color = colorEstado,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
