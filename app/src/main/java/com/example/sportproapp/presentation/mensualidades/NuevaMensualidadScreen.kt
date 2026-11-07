package com.example.sportproapp.presentation.mensualidades

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportproapp.data.model.formatearFecha
import com.example.sportproapp.data.model.moverPeriodo
import com.example.sportproapp.data.model.periodoActual
import com.example.sportproapp.presentation.auth.*
import java.util.Locale

// 4.05 Nueva mensualidad (botón + de la pantalla 4.02)
@Composable
fun NuevaMensualidadScreen(
    navController: NavController,
    viewModel: NuevaMensualidadViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val equipos by viewModel.equipos.collectAsState()
    val isLoadingEquipos by viewModel.isLoadingEquipos.collectAsState()
    val equipoSeleccionado by viewModel.equipoSeleccionado.collectAsState()
    val cantidadJugadores by viewModel.cantidadJugadores.collectAsState()

    var periodo by remember { mutableStateOf(periodoActual()) }
    var monto by remember { mutableStateOf("") }
    // Por defecto vence el día 10 del periodo elegido
    var fechaVencimiento by remember(periodo) { mutableStateOf(periodo.take(8) + "10") }

    val datePickerDialog = remember(fechaVencimiento) {
        val partes = fechaVencimiento.split("-")
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                fechaVencimiento = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            },
            partes[0].toInt(),
            partes[1].toInt() - 1,
            partes[2].toInt()
        )
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is NuevaMensualidadUiState.Success -> {
                val mensaje = if (state.creadas > 0) {
                    "Se crearon ${state.creadas} mensualidades"
                } else {
                    "Los jugadores ya tenían la mensualidad de ese mes"
                }
                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                navController.popBackStack()
            }
            is NuevaMensualidadUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    val isSaving = uiState is NuevaMensualidadUiState.Loading

    Scaffold(
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            MensualidadHeader(
                titulo = "Nueva mensualidad",
                subtitulo = "Se crea una cuota por jugador",
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- 1. EQUIPO ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EtiquetaCampo("Equipo")
                    when {
                        isLoadingEquipos -> CircularProgressIndicator(color = PrimaryLime, modifier = Modifier.size(24.dp))
                        equipos.isEmpty() -> Text("La academia no tiene equipos registrados.", color = TextMuted, fontSize = 13.sp)
                        else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(equipos) { equipo ->
                                val seleccionado = equipo.id == equipoSeleccionado?.id
                                Surface(
                                    color = if (seleccionado) PrimaryLime else DarkCardBackground,
                                    shape = RoundedCornerShape(50),
                                    border = if (seleccionado) null else BorderStroke(1.dp, CardBorderColor),
                                    modifier = Modifier.clickable { viewModel.seleccionarEquipo(equipo) }
                                ) {
                                    Text(
                                        text = "${equipo.nombre} ${equipo.categoria}".trim(),
                                        color = if (seleccionado) DarkBackground else TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 2. PERIODO ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EtiquetaCampo("Periodo")
                    SelectorPeriodo(
                        periodo = periodo,
                        onAnterior = { periodo = moverPeriodo(periodo, -1) },
                        onSiguiente = { periodo = moverPeriodo(periodo, 1) }
                    )
                }

                // --- 3. MONTO ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EtiquetaCampo("Monto")
                    OutlinedTextField(
                        value = monto,
                        onValueChange = { nuevo -> monto = nuevo.filter { it.isDigit() || it == '.' || it == ',' } },
                        placeholder = { Text("150.00", color = TextMuted) },
                        prefix = { Text("S/ ", color = Color.White) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PrimaryLime,
                            unfocusedBorderColor = CardBorderColor,
                            focusedContainerColor = DarkCardBackground,
                            unfocusedContainerColor = DarkCardBackground,
                            cursorColor = PrimaryLime
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // --- 4. FECHA DE VENCIMIENTO ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EtiquetaCampo("Fecha de vencimiento")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = formatearFecha(fechaVencimiento), color = Color.White, fontSize = 14.sp)
                        }
                    }
                    Text(text = "Pasada esta fecha, la cuota cambia a Atrasado", color = TextMuted, fontSize = 11.sp)
                }

                // --- 5. JUGADORES ALCANZADOS ---
                equipoSeleccionado?.let { equipo ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Group, contentDescription = null, tint = PrimaryLime, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = cantidadJugadores?.let { "$it jugadores" } ?: "Contando jugadores...",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Todos los jugadores de ${equipo.nombre} ${equipo.categoria}".trim(),
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                BannerAviso(
                    Icons.Outlined.NotificationsActive,
                    "Jugadores y apoderados recibirán un aviso con el monto y la fecha de vencimiento.",
                    PrimaryLime
                )

                Button(
                    onClick = { viewModel.crearMensualidades(periodo, monto, fechaVencimiento) },
                    enabled = !isSaving && equipoSeleccionado != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryLime, contentColor = DarkBackground)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = DarkBackground, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear mensualidades", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EtiquetaCampo(texto: String) {
    Text(text = texto.uppercase(), color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
}
