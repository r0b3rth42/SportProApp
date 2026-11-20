package com.example.sportproapp.presentation.partidos

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportproapp.data.model.EventoPartidoUI
import com.example.sportproapp.data.model.JugadorConUsuario
import com.example.sportproapp.data.model.PartidoConDetalles
import com.example.sportproapp.data.model.TipoEvento
import com.example.sportproapp.presentation.auth.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsolaEnVivoScreen(
    navController: NavController,
    partidoId: Long,
    viewModel: ConsolaEnVivoViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val equipoSeleccionadoId by viewModel.equipoSeleccionadoId.collectAsState()
    val cronometroSegundos by viewModel.cronometroSegundos.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val eventos by viewModel.eventos.collectAsState()
    val jugadoresLocal by viewModel.jugadoresLocal.collectAsState()
    val jugadoresVisitante by viewModel.jugadoresVisitante.collectAsState()
    val isProcessingAction by viewModel.isProcessingAction.collectAsState()

    var tipoEventoParaDialog by remember { mutableStateOf<TipoEvento?>(null) }
    var eventoParaEliminar by remember { mutableStateOf<EventoPartidoUI?>(null) }

    LaunchedEffect(partidoId) {
        viewModel.cargarDatos(partidoId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        when (val state = uiState) {
            is ConsolaEnVivoUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryLime)
                }
            }

            is ConsolaEnVivoUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.cargarDatos(partidoId) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryLime, contentColor = DarkBackground)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            is ConsolaEnVivoUiState.Success -> {
                val partido = state.partido
                val esLocalSeleccionado = equipoSeleccionadoId == partido.equipoLocalId
                val jugadoresEquipoActual = if (esLocalSeleccionado) jugadoresLocal else jugadoresVisitante

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    // --- 1. CABECERA SUPERIOR ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(InputBackgroundColor, RoundedCornerShape(12.dp))
                                .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
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

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Consola en Vivo",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Grabación de incidencias",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // --- 2. TARJETA CRONÓMETRO Y MARCADOR ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Tiempo en Formato 67:32 y Botón Play/Pausa
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val mins = cronometroSegundos / 60
                                val segs = cronometroSegundos % 60
                                val tiempoFormateado = String.format(Locale.getDefault(), "%02d:%02d", mins, segs)

                                Text(
                                    text = tiempoFormateado,
                                    color = PrimaryLime,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                IconButton(
                                    onClick = { viewModel.toggleTimer() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isTimerRunning) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                        contentDescription = "Controlar tiempo",
                                        tint = PrimaryLime,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Marcador: Tigres 2 - 1 Atlas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = partido.equipoLocalNombre,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )

                                Text(
                                    text = "  ${partido.golesLocal ?: 0} - ${partido.golesVisitante ?: 0}  ",
                                    color = PrimaryLime,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Text(
                                    text = partido.equipoVisitanteNombre,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- 3. SELECTOR DE EQUIPO A REGISTRAR SUCESO ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(InputBackgroundColor, RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                            .padding(3.dp)
                    ) {
                        val localSelected = equipoSeleccionadoId == partido.equipoLocalId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    color = if (localSelected) PrimaryLime else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.seleccionarEquipo(partido.equipoLocalId) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${partido.equipoLocalNombre} (Local)",
                                color = if (localSelected) DarkBackground else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val visitanteSelected = equipoSeleccionadoId == partido.equipoVisitanteId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    color = if (visitanteSelected) PrimaryLime else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.seleccionarEquipo(partido.equipoVisitanteId) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${partido.equipoVisitanteNombre} (Visitante)",
                                color = if (visitanteSelected) DarkBackground else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- 4. SECCIÓN REGISTRAR SUCESO (CUADRÍCULA 4x2) ---
                    Text(
                        text = "Registrar Suceso",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val opcionesSucesos = listOf(
                        OpcionSucesoItem(TipoEvento.GOL, "Gol", Icons.Default.AddCircle, isYellowCard = false, isRedCard = false),
                        OpcionSucesoItem(TipoEvento.ASISTENCIA, "Asistencia", Icons.Default.FileUpload, isYellowCard = false, isRedCard = false),
                        OpcionSucesoItem(TipoEvento.TARJETA_AMARILLA, "T. Amarilla", null, isYellowCard = true, isRedCard = false),
                        OpcionSucesoItem(TipoEvento.TARJETA_ROJA, "T. Roja", null, isYellowCard = false, isRedCard = true),
                        OpcionSucesoItem(TipoEvento.CAMBIO, "Cambio", Icons.Default.SwapHoriz, isYellowCard = false, isRedCard = false),
                        OpcionSucesoItem(TipoEvento.FALTA, "Falta", Icons.Default.ErrorOutline, isYellowCard = false, isRedCard = false),
                        OpcionSucesoItem(TipoEvento.TIRO, "Tiro", Icons.Default.ShowChart, isYellowCard = false, isRedCard = false),
                        OpcionSucesoItem(TipoEvento.CORNER, "Corner", Icons.AutoMirrored.Filled.ArrowForward, isYellowCard = false, isRedCard = false)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp)
                    ) {
                        items(opcionesSucesos) { opcion ->
                            BotonAccionSuceso(
                                item = opcion,
                                onClick = { tipoEventoParaDialog = opcion.tipo }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- 5. SECCIÓN SUCESOS RECIENTES ---
                    Text(
                        text = "Sucesos recientes",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (eventos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aún no hay sucesos registrados para este partido.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(eventos) { evento ->
                                ItemSucesoReciente(
                                    evento = evento,
                                    onEliminar = { eventoParaEliminar = evento }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(30.dp)) }
                        }
                    }
                }

                // --- DIÁLOGO DE REGISTRO DE EVENTO ---
                tipoEventoParaDialog?.let { tipo ->
                    val minsMinutoActual = cronometroSegundos / 60
                    val equipoNombre = if (esLocalSeleccionado) partido.equipoLocalNombre else partido.equipoVisitanteNombre

                    DialogoRegistroSuceso(
                        tipoEvento = tipo,
                        equipoNombre = equipoNombre,
                        minutoInicial = minsMinutoActual,
                        jugadores = jugadoresEquipoActual,
                        isProcessing = isProcessingAction,
                        onDismiss = { tipoEventoParaDialog = null },
                        onGuardar = { jugadorId, minuto, detalle, jugadorSaleId ->
                            viewModel.registrarEvento(
                                tipoEvento = tipo,
                                jugadorId = jugadorId,
                                minuto = minuto,
                                detalle = detalle,
                                jugadorSaleId = jugadorSaleId,
                                onSuccess = {
                                    tipoEventoParaDialog = null
                                    Toast.makeText(context, "¡${tipo.label} registrado!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                }

                // --- DIÁLOGO DE CONFIRMACIÓN DE ELIMINACIÓN ---
                eventoParaEliminar?.let { evt ->
                    AlertDialog(
                        onDismissRequest = { eventoParaEliminar = null },
                        containerColor = DarkCardBackground,
                        title = { Text("Eliminar Suceso", color = Color.White, fontWeight = FontWeight.Bold) },
                        text = { Text("¿Deseas eliminar este suceso registrado (${evt.tipoEvento.label} en min ${evt.minuto}')?", color = TextLight) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.eliminarEvento(evt) {
                                        eventoParaEliminar = null
                                        Toast.makeText(context, "Suceso eliminado", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5555), contentColor = Color.White)
                            ) {
                                Text("Eliminar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { eventoParaEliminar = null }) {
                                Text("Cancelar", color = TextMuted)
                            }
                        }
                    )
                }
            }
        }
    }
}

// --- CLASE DE MODELO INTERNO PARA LOS 8 BOTONES DE ACCIÓN ---
data class OpcionSucesoItem(
    val tipo: TipoEvento,
    val titulo: String,
    val icon: ImageVector?,
    val isYellowCard: Boolean = false,
    val isRedCard: Boolean = false
)

// --- COMPONENTE: BOTÓN DE ACCIÓN DE SUCESO (Gol, Card, etc.) ---
@Composable
fun BotonAccionSuceso(
    item: OpcionSucesoItem,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (item.isYellowCard) {
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 22.dp)
                        .background(Color(0xFFFFD700), RoundedCornerShape(3.dp))
                )
            } else if (item.isRedCard) {
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 22.dp)
                        .background(Color(0xFFFF4444), RoundedCornerShape(3.dp))
                )
            } else if (item.icon != null) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.titulo,
                    tint = PrimaryLime,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.titulo,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- COMPONENTE: TARJETA DE SUCESO RECIENTE EN LA LISTA ---
@Composable
fun ItemSucesoReciente(
    evento: EventoPartidoUI,
    onEliminar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Distintivo de Minuto verde (ej. 62')
            Box(
                modifier = Modifier
                    .background(Color(0xFF162D24), RoundedCornerShape(10.dp))
                    .border(1.dp, CardBorderColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${evento.minuto}'",
                    color = PrimaryLime,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del evento
            Column(modifier = Modifier.weight(1f)) {
                val textoJugador = evento.jugadorNombre?.let { " • $it" } ?: ""
                val textoDorsal = evento.jugadorDorsal?.let { " ($it)" } ?: ""
                val tituloEvento = "${evento.tipoEvento.label}$textoJugador$textoDorsal"

                Text(
                    text = tituloEvento,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                val descripcionDetalle = if (evento.tipoEvento == TipoEvento.CAMBIO) {
                    val saleText = evento.jugadorSaleNombre?.let { "Sale: $it. " } ?: ""
                    "$saleText${evento.detalle ?: ""}".trim()
                } else {
                    evento.detalle ?: "Incidencia registrada para ${evento.equipoNombre}."
                }

                if (descripcionDetalle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = descripcionDetalle,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// --- DIÁLOGO REGISTRO DE SUCESO MODAL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoRegistroSuceso(
    tipoEvento: TipoEvento,
    equipoNombre: String,
    minutoInicial: Int,
    jugadores: List<JugadorConUsuario>,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (jugadorId: String?, minuto: Int, detalle: String?, jugadorSaleId: String?) -> Unit
) {
    var minutoText by remember { mutableStateOf(minutoInicial.toString()) }
    var detalleText by remember { mutableStateOf("") }

    var jugadorEntraId by remember { mutableStateOf<String?>(null) }
    var jugadorEntraNombre by remember { mutableStateOf("Seleccionar jugador (opcional)") }
    var expandedJugadorEntra by remember { mutableStateOf(false) }

    var jugadorSaleId by remember { mutableStateOf<String?>(null) }
    var jugadorSaleNombre by remember { mutableStateOf("Seleccionar jugador que sale (opcional)") }
    var expandedJugadorSale by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCardBackground,
        title = {
            Column {
                Text(
                    text = "Registrar ${tipoEvento.label}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Equipo: $equipoNombre",
                    color = PrimaryLime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Minuto
                OutlinedTextField(
                    value = minutoText,
                    onValueChange = { minutoText = it },
                    label = { Text("Minuto", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryLime,
                        unfocusedBorderColor = CardBorderColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Selección de Jugador (o Jugador que Entra)
                ExposedDropdownMenuBox(
                    expanded = expandedJugadorEntra,
                    onExpandedChange = { expandedJugadorEntra = !expandedJugadorEntra }
                ) {
                    OutlinedTextField(
                        value = jugadorEntraNombre,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (tipoEvento == TipoEvento.CAMBIO) "Jugador que Entra" else "Jugador", color = TextMuted) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJugadorEntra) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLime,
                            unfocusedBorderColor = CardBorderColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedJugadorEntra,
                        onDismissRequest = { expandedJugadorEntra = false },
                        modifier = Modifier.background(InputBackgroundColor)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin especificar", color = TextMuted) },
                            onClick = {
                                jugadorEntraId = null
                                jugadorEntraNombre = "Sin especificar"
                                expandedJugadorEntra = false
                            }
                        )
                        jugadores.forEach { jug ->
                            DropdownMenuItem(
                                text = { Text(jug.usuario.name, color = Color.White) },
                                onClick = {
                                    jugadorEntraId = jug.usuario.id
                                    jugadorEntraNombre = jug.usuario.name
                                    expandedJugadorEntra = false
                                }
                            )
                        }
                    }
                }

                // Selección de Jugador que Sale (Solo para CAMBIO)
                if (tipoEvento == TipoEvento.CAMBIO) {
                    ExposedDropdownMenuBox(
                        expanded = expandedJugadorSale,
                        onExpandedChange = { expandedJugadorSale = !expandedJugadorSale }
                    ) {
                        OutlinedTextField(
                            value = jugadorSaleNombre,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Jugador que Sale", color = TextMuted) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJugadorSale) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryLime,
                                unfocusedBorderColor = CardBorderColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedJugadorSale,
                            onDismissRequest = { expandedJugadorSale = false },
                            modifier = Modifier.background(InputBackgroundColor)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sin especificar", color = TextMuted) },
                                onClick = {
                                    jugadorSaleId = null
                                    jugadorSaleNombre = "Sin especificar"
                                    expandedJugadorSale = false
                                }
                            )
                            jugadores.forEach { jug ->
                                DropdownMenuItem(
                                    text = { Text(jug.usuario.name, color = Color.White) },
                                    onClick = {
                                        jugadorSaleId = jug.usuario.id
                                        jugadorSaleNombre = jug.usuario.name
                                        expandedJugadorSale = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Detalle / Nota opcional
                OutlinedTextField(
                    value = detalleText,
                    onValueChange = { detalleText = it },
                    label = { Text("Detalle / Nota (opcional)", color = TextMuted) },
                    placeholder = { Text("Ej. Remate cruzado tras centro...", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryLime,
                        unfocusedBorderColor = CardBorderColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val min = minutoText.toIntOrNull() ?: minutoInicial
                    onGuardar(jugadorEntraId, min, detalleText, jugadorSaleId)
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLime, contentColor = DarkBackground),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Suceso", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextMuted)
            }
        }
    )
}
