package com.example.sportproapp.presentation.mensualidades

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.sportproapp.data.model.EstadoVisualMensualidad
import com.example.sportproapp.data.model.MensualidadJugadorUI
import com.example.sportproapp.data.model.estadoVisual
import com.example.sportproapp.data.model.formatearFecha
import com.example.sportproapp.data.model.formatearSoles
import com.example.sportproapp.data.model.nombrePeriodo
import com.example.sportproapp.presentation.auth.*

// 4.02 Mensualidades · administrador y DT
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MensualidadesAdminScreen(
    navController: NavController,
    viewModel: MensualidadesAdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val periodo by viewModel.periodo.collectAsState()
    val esAdmin by viewModel.esAdmin.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Recargar al entrar o al volver de crear mensualidades: así se ven los pagos nuevos
    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            if (esAdmin) {
                FloatingActionButton(
                    onClick = { navController.navigate("mensualidad-create") },
                    containerColor = PrimaryLime,
                    contentColor = DarkBackground,
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva mensualidad", modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            MensualidadHeader(
                titulo = "Mensualidades",
                subtitulo = if (esAdmin) "Toda la academia" else "Tu equipo · solo lectura",
                onBack = { navController.popBackStack() }
            )

            SelectorPeriodo(
                periodo = periodo,
                onAnterior = { viewModel.periodoAnterior() },
                onSiguiente = { viewModel.periodoSiguiente() },
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(14.dp))

            when (val state = uiState) {
                is MensualidadesAdminUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryLime)
                    }
                }

                is MensualidadesAdminUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }
                }

                is MensualidadesAdminUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.cargarDatos(esRefresh = true) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // --- 1. RESUMEN DEL MES ---
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    TarjetaResumen("RECAUDADO", formatearSoles(state.resumen.recaudado), PagadoGreen, Modifier.weight(1f))
                                    TarjetaResumen("PENDIENTES", state.resumen.pendientes.toString(), PendienteYellow, Modifier.weight(1f))
                                    TarjetaResumen("ATRASADOS", state.resumen.atrasados.toString(), AtrasadoRed, Modifier.weight(1f))
                                }
                            }

                            // --- 2. ESTADO DE JUGADORES ---
                            item {
                                Text(
                                    text = "Estado de jugadores",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }

                            if (state.lista.isEmpty()) {
                                item {
                                    MensajeVacioMensualidades(
                                        if (esAdmin) "Sin mensualidades en ${nombrePeriodo(periodo)}. Toca + para crearlas."
                                        else "Sin mensualidades en ${nombrePeriodo(periodo)}."
                                    )
                                }
                            } else {
                                items(state.lista) { item ->
                                    ItemEstadoJugador(
                                        item = item,
                                        onClick = {
                                            if (item.mensualidad.estadoVisual() == EstadoVisualMensualidad.PAGADO) {
                                                navController.navigate("comprobante-pago/${item.mensualidad.id}")
                                            }
                                        }
                                    )
                                }
                            }

                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaResumen(etiqueta: String, valor: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = etiqueta, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = valor, color = color, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun ItemEstadoJugador(item: MensualidadJugadorUI, onClick: () -> Unit) {
    val cuota = item.mensualidad
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarJugador(nombre = item.nombreJugador, fotoUrl = item.fotoUrl)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.nombreJugador, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${formatearSoles(cuota.monto)} • Límite: ${formatearFecha(cuota.fechaVencimiento)}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            EstadoMensualidadBadge(estado = cuota.estadoVisual())
        }
    }
}
