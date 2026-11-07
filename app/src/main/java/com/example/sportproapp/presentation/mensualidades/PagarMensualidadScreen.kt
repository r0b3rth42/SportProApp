package com.example.sportproapp.presentation.mensualidades

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportproapp.data.model.EstadoMensualidad
import com.example.sportproapp.data.model.MetodoPago
import com.example.sportproapp.data.model.estadoVisual
import com.example.sportproapp.data.model.formatearFecha
import com.example.sportproapp.data.model.formatearSoles
import com.example.sportproapp.data.model.nombrePeriodo
import com.example.sportproapp.presentation.auth.*

// 4.03 Pagar mensualidad (pago simulado: no se cobra dinero real ni se piden datos de tarjeta)
@Composable
fun PagarMensualidadScreen(
    navController: NavController,
    mensualidadId: String,
    viewModel: PagarMensualidadViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val pagoState by viewModel.pagoState.collectAsState()
    val metodoSeleccionado by viewModel.metodo.collectAsState()

    LaunchedEffect(mensualidadId) {
        viewModel.cargarMensualidad(mensualidadId)
    }

    // Al confirmar se reemplaza esta pantalla por el comprobante (4.04)
    LaunchedEffect(pagoState) {
        when (val state = pagoState) {
            is PagoUiState.Exito -> {
                Toast.makeText(context, "¡Pago registrado!", Toast.LENGTH_SHORT).show()
                navController.navigate("comprobante-pago/${state.mensualidadId}") {
                    popUpTo("pagar-mensualidad/{mensualidadId}") { inclusive = true }
                }
            }
            is PagoUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetPagoState()
            }
            else -> Unit
        }
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
            when (val state = uiState) {
                is DetalleMensualidadUiState.Loading -> {
                    MensualidadHeader(titulo = "Pagar mensualidad", onBack = { navController.popBackStack() })
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryLime)
                    }
                }

                is DetalleMensualidadUiState.Error -> {
                    MensualidadHeader(titulo = "Pagar mensualidad", onBack = { navController.popBackStack() })
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }
                }

                is DetalleMensualidadUiState.Success -> {
                    val cuota = state.detalle.mensualidad
                    val estado = cuota.estadoVisual()
                    val procesando = pagoState is PagoUiState.Procesando

                    MensualidadHeader(
                        titulo = "Pagar mensualidad",
                        subtitulo = nombrePeriodo(cuota.periodo),
                        onBack = { navController.popBackStack() }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // --- 1. MONTO Y DATOS DE LA CUOTA ---
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CardBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "MONTO A PAGAR", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    EstadoMensualidadBadge(estado = estado)
                                }
                                Text(
                                    text = formatearSoles(cuota.monto),
                                    color = colorEstadoMensualidad(estado),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                HorizontalDivider(color = CardBorderColor, thickness = 1.dp)
                                FilaDato("Jugador", state.detalle.nombreJugador)
                                FilaDato("Equipo", state.detalle.nombreEquipo)
                                FilaDato("Periodo", nombrePeriodo(cuota.periodo))
                                FilaDato("Vence", formatearFecha(cuota.fechaVencimiento))
                            }
                        }

                        if (cuota.estado == EstadoMensualidad.PAGADO) {
                            // La cuota ya se pagó (por ejemplo, desde el celular del apoderado)
                            BannerAviso(Icons.Default.Check, "Esta mensualidad ya fue pagada.", PagadoGreen)
                            Button(
                                onClick = {
                                    navController.navigate("comprobante-pago/${cuota.id}") {
                                        popUpTo("pagar-mensualidad/{mensualidadId}") { inclusive = true }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLime, contentColor = DarkBackground)
                            ) {
                                Text("Ver comprobante", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            BannerAviso(
                                Icons.Outlined.Science,
                                "Pago simulado: no se cobra dinero real ni se piden datos de tarjeta. Solo se registra la mensualidad como pagada.",
                                InfoBlue
                            )

                            // --- 2. MÉTODO DE PAGO ---
                            Text(text = "ELIGE CÓMO PAGAR", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            MetodoPago.entries.forEach { metodo ->
                                OpcionMetodoPago(
                                    metodo = metodo,
                                    icon = iconoMetodo(metodo),
                                    seleccionado = metodo == metodoSeleccionado,
                                    onClick = { viewModel.seleccionarMetodo(metodo) }
                                )
                            }

                            // --- 3. ACCIONES ---
                            Button(
                                onClick = { viewModel.confirmarPago() },
                                enabled = !procesando,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLime, contentColor = DarkBackground)
                            ) {
                                if (procesando) {
                                    CircularProgressIndicator(color = DarkBackground, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Confirmar pago simulado", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { navController.popBackStack() },
                                enabled = !procesando,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderColor)
                            ) {
                                Text("Cancelar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

private fun iconoMetodo(metodo: MetodoPago): ImageVector = when (metodo) {
    MetodoPago.YAPE_PLIN -> Icons.Outlined.PhoneAndroid
    MetodoPago.TARJETA -> Icons.Outlined.CreditCard
    MetodoPago.TRANSFERENCIA -> Icons.Outlined.AccountBalance
}

@Composable
private fun OpcionMetodoPago(
    metodo: MetodoPago,
    icon: ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (seleccionado) PrimaryLime else CardBorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (seleccionado) PrimaryLime else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = metodo.label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "Simulado", color = TextMuted, fontSize = 11.sp)
        }
        RadioButton(
            selected = seleccionado,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = PrimaryLime, unselectedColor = CardBorderColor)
        )
    }
}
