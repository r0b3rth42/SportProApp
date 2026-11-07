package com.example.sportproapp.presentation.mensualidades

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportproapp.data.model.EstadoMensualidad
import com.example.sportproapp.data.model.EstadoVisualMensualidad
import com.example.sportproapp.data.model.Mensualidad
import com.example.sportproapp.data.model.PagoMensualidad
import com.example.sportproapp.data.model.estadoVisual
import com.example.sportproapp.data.model.formatearFecha
import com.example.sportproapp.data.model.formatearFechaLarga
import com.example.sportproapp.data.model.formatearSoles
import com.example.sportproapp.data.model.nombrePeriodo
import com.example.sportproapp.presentation.auth.*

// 4.01 Mensualidades · jugador y apoderado
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MensualidadesScreen(
    navController: NavController,
    viewModel: MensualidadesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Recargar al entrar o al volver de pagar
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
            MensualidadHeader(
                titulo = "Mensualidades",
                subtitulo = "Pagos y cuotas",
                onBack = { navController.popBackStack() }
            )

            when (val state = uiState) {
                is MensualidadesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryLime)
                    }
                }

                is MensualidadesUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }
                }

                is MensualidadesUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.cargarDatos(esRefresh = true) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // --- SELECTOR DE HIJO (solo apoderado) ---
                            if (state.esApoderado && state.hijos.size > 1) {
                                item {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(state.hijos) { hijo ->
                                            ChipHijo(
                                                nombre = hijo.usuario.name,
                                                seleccionado = hijo.perfil.id == state.jugadorSeleccionadoId,
                                                onClick = { viewModel.seleccionarHijo(hijo.perfil.id) }
                                            )
                                        }
                                    }
                                }
                            }

                            if (state.esApoderado && state.hijos.isEmpty()) {
                                item { MensajeVacioMensualidades("Aún no tienes jugadores vinculados.") }
                            } else {
                                // --- 1. CUOTA VIGENTE ---
                                item {
                                    val vigente = state.mensualidades
                                        .filter { it.estado == EstadoMensualidad.PENDIENTE }
                                        .minByOrNull { it.periodo }
                                    TarjetaCuotaVigente(
                                        cuota = vigente,
                                        onPagar = { id -> navController.navigate("pagar-mensualidad/$id") }
                                    )
                                }

                                // --- 2. HISTORIAL ---
                                item {
                                    Text(
                                        text = "Historial de pagos",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                if (state.mensualidades.isEmpty()) {
                                    item { MensajeVacioMensualidades("Aún no hay mensualidades registradas.") }
                                } else {
                                    items(state.mensualidades) { cuota ->
                                        ItemHistorialMensualidad(
                                            cuota = cuota,
                                            pago = state.pagos[cuota.id],
                                            onPagar = { navController.navigate("pagar-mensualidad/${cuota.id}") },
                                            onVerComprobante = { navController.navigate("comprobante-pago/${cuota.id}") }
                                        )
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipHijo(nombre: String, seleccionado: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (seleccionado) PrimaryLime else DarkCardBackground,
        shape = RoundedCornerShape(50),
        border = if (seleccionado) null else BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = nombre,
            color = if (seleccionado) DarkBackground else TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

// --- COMPONENTE: CUOTA PENDIENTE MÁS ANTIGUA ---
@Composable
private fun TarjetaCuotaVigente(cuota: Mensualidad?, onPagar: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (cuota == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PagadoGreen.copy(alpha = 0.12f), CircleShape)
                            .border(1.dp, PagadoGreen.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PagadoGreen, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "¡Estás al día!", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "No tienes mensualidades pendientes.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                val estado = cuota.estadoVisual()
                val color = colorEstadoMensualidad(estado)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Payments, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                    EstadoMensualidadBadge(estado = estado)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = formatearSoles(cuota.monto), color = color, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    text = "${nombrePeriodo(cuota.periodo)} · Vence el ${formatearFechaLarga(cuota.fechaVencimiento)}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { cuota.id?.let(onPagar) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryLime, contentColor = DarkBackground)
                ) {
                    Text("Pagar ahora", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- COMPONENTE: CUOTA DEL HISTORIAL ---
@Composable
private fun ItemHistorialMensualidad(
    cuota: Mensualidad,
    pago: PagoMensualidad?,
    onPagar: () -> Unit,
    onVerComprobante: () -> Unit
) {
    val estado = cuota.estadoVisual()
    val pagada = estado == EstadoVisualMensualidad.PAGADO

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
                EstadoMensualidadBadge(estado = estado, textoPagado = "Completado")
                Text(text = formatearSoles(cuota.monto), color = PrimaryLime, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = nombrePeriodo(cuota.periodo), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                text = if (pagada && pago?.fechaPago != null) {
                    "Pagado el • ${formatearFecha(pago.fechaPago)}"
                } else {
                    "Vence • ${formatearFecha(cuota.fechaVencimiento)}"
                },
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CardBorderColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (pagada) "Ref: Op #${pago?.numeroOperacion ?: "-"}" else "Pago simulado",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                OutlinedButton(
                    onClick = if (pagada) onVerComprobante else onPagar,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryLime),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (pagada) "Ver comprobante" else "Pagar ahora",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
