package com.example.sportproapp.presentation.partidos

import android.widget.Toast
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
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportproapp.presentation.auth.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaJugadorScreen(
    navController: NavController,
    partidoId: Long,
    equipoId: String,
    viewModel: AsistenciaJugadorViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    LaunchedEffect(partidoId, equipoId) {
        viewModel.cargarDatos(partidoId, equipoId)
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
                            is AsistenciaJugadorUiState.Success -> state.equipoNombre
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
        },
        bottomBar = {
            if (uiState is AsistenciaJugadorUiState.Success) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.guardarRespuesta(partidoId, equipoId) {
                                Toast.makeText(context, "¡Respuesta de asistencia guardada!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryLime,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DarkBackground)
                        } else {
                            Text(
                                text = "Guardar Respuesta",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            when (val state = uiState) {
                is AsistenciaJugadorUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryLime)
                    }
                }
                is AsistenciaJugadorUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is AsistenciaJugadorUiState.Success -> {
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

                        // --- 2. PREGUNTA: ¿ESTÁS DISPONIBLE? ---
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¿Estás disponible?",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Opciones de disponibilidad
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Opción 1: Confirmar
                                OpcionDisponibilidadCard(
                                    titulo = "Sí, confirmo asistencia",
                                    colorPunto = PrimaryLime,
                                    isSelected = state.estadoSeleccionado == "CONFIRMADO",
                                    onClick = { viewModel.seleccionarEstado("CONFIRMADO") }
                                )

                                // Opción 2: No puede
                                OpcionDisponibilidadCard(
                                    titulo = "No puedo asistir",
                                    colorPunto = Color(0xFFFF3333),
                                    isSelected = state.estadoSeleccionado == "BAJA" || state.estadoSeleccionado == "NO_PUEDE",
                                    onClick = { viewModel.seleccionarEstado("BAJA") }
                                )
                            }
                        }

                        // --- 4. ESTADO DEL EQUIPO ---
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Estado del equipo",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (state.jugadoresEstado.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No hay registros en el equipo aún.", color = TextMuted)
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
fun OpcionDisponibilidadCard(
    titulo: String,
    colorPunto: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) colorPunto else CardBorderColor
    val backgroundColor = if (isSelected) colorPunto.copy(alpha = 0.08f) else DarkCardBackground

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(colorPunto, CircleShape)
            )
            Text(
                text = titulo,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
