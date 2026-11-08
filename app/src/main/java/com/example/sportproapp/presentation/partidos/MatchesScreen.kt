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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportproapp.data.model.DesafioPartido
import com.example.sportproapp.data.model.EstadoDesafio
import com.example.sportproapp.data.model.PartidoConDetalles
import com.example.sportproapp.presentation.auth.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    navController: NavController,
    viewModel: MatchesViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isProcessingAction by viewModel.isProcessingAction.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Próximos Partidos, 1: Desafíos
    val tabs = listOf("Próximos Partidos", "Desafíos Recibidos")

    // Diálogo de detalles y creación
    var partidoParaDetalle by remember { mutableStateOf<PartidoConDetalles?>(null) }
    var mostrarMenuCreacion by remember { mutableStateOf(false) }

    // Recargar datos al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // --- 1. CABECERA Y TÍTULO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Partidos & Desafíos",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Programa o consulta los encuentros deportivos",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }

            // --- 2. SELECTOR DE PESTAÑA ---
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

            // --- 3. CONTENIDO SEGÚN PESTAÑA ---
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
                        val partidos = state.partidosProgramados

                        if (partidos.isEmpty()) {
                            MensajeSinResultados("No tienes partidos programados aún.")
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(partidos) { partido ->
                                    ItemPartidoProgramado(
                                        partido = partido,
                                        onClick = { partidoParaDetalle = partido }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(90.dp)) }
                            }
                        }
                    } else {
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
                                item { Spacer(modifier = Modifier.height(90.dp)) }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. BOTÓN FLOTANTE (+) PARA CREAR PARTIDO O DESAFÍO ---
        FloatingActionButton(
            onClick = { mostrarMenuCreacion = true },
            containerColor = PrimaryLime,
            contentColor = DarkBackground,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(60.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Crear Partido",
                modifier = Modifier.size(32.dp)
            )
        }
    }

    // --- DIÁLOGO MENÚ DE CREACIÓN (PARTIDO O DESAFÍO) ---
    if (mostrarMenuCreacion) {
        AlertDialog(
            onDismissRequest = { mostrarMenuCreacion = false },
            containerColor = DarkCardBackground,
            title = {
                Text("¿Qué deseas crear?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            mostrarMenuCreacion = false
                            navController.navigate("crear-partido")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryLime, contentColor = DarkBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Outlined.SportsSoccer, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear Partido Directo", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            mostrarMenuCreacion = false
                            navController.navigate("matches-create-challenge")
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryLime),
                        border = BorderStroke(1.dp, PrimaryLime),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar Desafío a Rival", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarMenuCreacion = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    // --- DIÁLOGO DE DETALLES DEL PARTIDO ---
    partidoParaDetalle?.let { partido ->
        DetallePartidoDialog(
            partido = partido,
            onDismiss = { partidoParaDetalle = null }
        )
    }
}

// --- COMPONENTE: TARJETA DE PARTIDO PROGRAMADO ---
@Composable
fun ItemPartidoProgramado(
    partido: PartidoConDetalles,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                    border = BorderStroke(1.dp, CardBorderColor)
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

            // Encuentro: Equipo Local VS Equipo Visitante
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Local
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = partido.equipoLocalNombre,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (partido.equipoLocalCategoria.isNotBlank()) {
                        Text(
                            text = partido.equipoLocalCategoria,
                            color = TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // VS o Marcador
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .background(InputBackgroundColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (partido.golesLocal != null && partido.golesVisitante != null) {
                        Text(
                            text = "${partido.golesLocal} - ${partido.golesVisitante}",
                            color = PrimaryLime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "VS",
                            color = PrimaryLime,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Visitante
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = partido.equipoVisitanteNombre,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (partido.equipoVisitanteCategoria.isNotBlank()) {
                        Text(
                            text = partido.equipoVisitanteCategoria,
                            color = TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Place, contentDescription = null, tint = PrimaryLime, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = partido.sedeNombre.ifBlank { "Sede Principal" },
                        color = TextLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// --- DIÁLOGO DE DETALLES DEL PARTIDO ---
@Composable
fun DetallePartidoDialog(
    partido: PartidoConDetalles,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCardBackground,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryLime,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Detalles del Partido",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Insignias de Tipo y Estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF162D24),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Text(
                            text = "TIPO: ${partido.tipoPartido}",
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(color = CardBorderColor, thickness = 1.dp)

                // Encuentro deportivo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InputBackgroundColor, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("LOCAL", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = partido.equipoLocalNombre,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (partido.equipoLocalCategoria.isNotBlank()) {
                            Text(partido.equipoLocalCategoria, color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .background(DarkBackground, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        val resultadoText = if (partido.golesLocal != null && partido.golesVisitante != null) {
                            "${partido.golesLocal} - ${partido.golesVisitante}"
                        } else "VS"

                        Text(
                            text = resultadoText,
                            color = PrimaryLime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("VISITANTE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = partido.equipoVisitanteNombre,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (partido.equipoVisitanteCategoria.isNotBlank()) {
                            Text(partido.equipoVisitanteCategoria, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }

                // Datos de Fecha, Hora y Sede
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Event, contentDescription = null, tint = PrimaryLime, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Fecha: ${partido.fecha}", color = Color.White, fontSize = 14.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = PrimaryLime, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Hora: ${partido.hora}", color = Color.White, fontSize = 14.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Place, contentDescription = null, tint = PrimaryLime, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sede: ${partido.sedeNombre.ifBlank { "Sede Principal" }}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (partido.sedeDireccion.isNotBlank()) {
                        Text(
                            text = "Dirección: ${partido.sedeDireccion}",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 26.dp)
                        )
                    }

                    if (!partido.mensajeDesafio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Nota/Mensaje del Desafío:",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "\"${partido.mensajeDesafio}\"",
                            color = TextLight,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLime, contentColor = DarkBackground),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar", fontWeight = FontWeight.Bold)
            }
        }
    )
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
        border = BorderStroke(1.dp, CardBorderColor),
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
                    border = BorderStroke(1.dp, CardBorderColor)
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