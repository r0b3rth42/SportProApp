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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
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
import com.example.sportproapp.data.model.EstadoAsistencia
import com.example.sportproapp.data.model.JugadorAsistenciaUI

private val RedAusente = Color(0xFFFF3333)
private val YellowJustificado = Color(0xFFFFB800)
private val CardItemBg = Color(0xFF0F1E19)
private val ButtonInactiveBg = Color(0xFF162B23)
private val IconInactiveColor = Color(0xFF50685F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaScreen(
    navController: NavController,
    entrenamientoId: String,
    equipoId: String,
    fechaSubtitulo: String = "Mier, 15 de Mayo • Tigres Sub-15",
    viewModel: AsistenciaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    LaunchedEffect(entrenamientoId) {
        viewModel.cargarAsistencias(entrenamientoId, equipoId)
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Asistencia",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Text(
                            text = fechaSubtitulo,
                            color = TextGray,
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
                            .background(CardItemBg)
                            .border(1.dp, CardBorderColor, RoundedCornerShape(10.dp))
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        bottomBar = {
            if (uiState is AsistenciaUiState.Success) {
                val state = uiState as AsistenciaUiState.Success
                val presentes = state.lista.count { it.estado == EstadoAsistencia.PRESENTE }
                val total = state.lista.size

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resumen:",
                            color = TextGray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$presentes / $total Presentes",
                            color = PrimaryGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.guardarAsistencias(entrenamientoId) {
                                navController.popBackStack()
                            }
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black
                            )
                        } else {
                            Text(
                                text = "Guardar asistencia",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is AsistenciaUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            is AsistenciaUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is AsistenciaUiState.Success -> {
                val todosPresentes = state.lista.isNotEmpty() && state.lista.all { it.estado == EstadoAsistencia.PRESENTE }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // CONTROL SWITCH "Marcar todos presente"
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Marcar todos presente",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Switch(
                                checked = todosPresentes,
                                onCheckedChange = { checked ->
                                    viewModel.marcarTodosPresentes(checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = PrimaryGreen,
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = ButtonInactiveBg
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // LISTA DE JUGADORES
                    items(state.lista) { itemUI ->
                        TarjetaJugadorAsistenciaFigma(
                            item = itemUI,
                            onEstadoCambiado = { nuevoEstado ->
                                viewModel.cambiarEstadoJugador(itemUI.jugadorId, nuevoEstado)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
fun TarjetaJugadorAsistenciaFigma(
    item: JugadorAsistenciaUI,
    onEstadoCambiado: (EstadoAsistencia) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardItemBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto de Perfil o Avatar por defecto
            if (!item.fotoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.fotoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(ButtonInactiveBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre y Dorsal
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombreCompleto,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Dorsal #${item.dorsal ?: "S/N"}",
                    color = PrimaryGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botones de Estado: Presente (Verde), Ausente (Rojo), Justificado (Amarillo)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // PRESENTE
                BotonEstadoAsistencia(
                    icono = Icons.Default.Check,
                    esActivo = item.estado == EstadoAsistencia.PRESENTE,
                    colorActivoFondo = PrimaryGreen,
                    colorActivoIcono = Color.Black,
                    onClick = { onEstadoCambiado(EstadoAsistencia.PRESENTE) }
                )

                // AUSENTE
                BotonEstadoAsistencia(
                    icono = Icons.Default.Close,
                    esActivo = item.estado == EstadoAsistencia.AUSENTE,
                    colorActivoFondo = RedAusente,
                    colorActivoIcono = Color.White,
                    onClick = { onEstadoCambiado(EstadoAsistencia.AUSENTE) }
                )

                // JUSTIFICADO
                BotonEstadoAsistencia(
                    icono = Icons.Default.Warning,
                    esActivo = item.estado == EstadoAsistencia.JUSTIFICADO,
                    colorActivoFondo = YellowJustificado,
                    colorActivoIcono = Color.Black,
                    onClick = { onEstadoCambiado(EstadoAsistencia.JUSTIFICADO) }
                )
            }
        }
    }
}

@Composable
fun BotonEstadoAsistencia(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    esActivo: Boolean,
    colorActivoFondo: Color,
    colorActivoIcono: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (esActivo) colorActivoFondo else ButtonInactiveBg)
            .border(
                width = 1.dp,
                color = if (esActivo) Color.Transparent else CardBorderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = if (esActivo) colorActivoIcono else IconInactiveColor,
            modifier = Modifier.size(20.dp)
        )
    }
}