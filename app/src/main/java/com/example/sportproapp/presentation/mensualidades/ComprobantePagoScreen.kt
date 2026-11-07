package com.example.sportproapp.presentation.mensualidades

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportproapp.data.model.formatearFechaHora
import com.example.sportproapp.data.model.formatearSoles
import com.example.sportproapp.data.model.nombrePeriodo
import com.example.sportproapp.presentation.auth.*

// 4.04 Comprobante de pago
@Composable
fun ComprobantePagoScreen(
    navController: NavController,
    mensualidadId: String,
    viewModel: ComprobantePagoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(mensualidadId) {
        viewModel.cargarComprobante(mensualidadId)
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
                    MensualidadHeader(titulo = "Comprobante", onBack = { navController.popBackStack() })
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryLime)
                    }
                }

                is DetalleMensualidadUiState.Error -> {
                    MensualidadHeader(titulo = "Comprobante", onBack = { navController.popBackStack() })
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }
                }

                is DetalleMensualidadUiState.Success -> {
                    val detalle = state.detalle
                    val cuota = detalle.mensualidad
                    val pago = detalle.pago

                    MensualidadHeader(
                        titulo = "Comprobante",
                        subtitulo = nombrePeriodo(cuota.periodo),
                        onBack = { navController.popBackStack() }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(PagadoGreen.copy(alpha = 0.12f), CircleShape)
                                .border(1.dp, PagadoGreen.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PagadoGreen, modifier = Modifier.size(40.dp))
                        }

                        Text(
                            text = "¡Pago registrado!",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "La mensualidad de ${nombrePeriodo(cuota.periodo)} quedó como pagada. El administrador ya la ve actualizada.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )

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
                                    Text(
                                        text = formatearSoles(pago?.monto ?: cuota.monto),
                                        color = PagadoGreen,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Surface(color = PagadoGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = "COMPLETADO",
                                            color = PagadoGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(color = CardBorderColor, thickness = 1.dp)
                                FilaDato("Jugador", detalle.nombreJugador)
                                FilaDato("Equipo", detalle.nombreEquipo)
                                FilaDato("Periodo", nombrePeriodo(cuota.periodo))
                                FilaDato("Método", "${pago?.metodo?.label ?: "-"} (simulado)")
                                FilaDato("Fecha", formatearFechaHora(pago?.fechaPago))
                                FilaDato("N.º de operación", pago?.numeroOperacion ?: "-", PrimaryLime)
                                FilaDato("Pagado por", textoPagador(detalle.nombrePagador, detalle.rolPagador))
                            }
                        }

                        BannerAviso(
                            Icons.Outlined.Info,
                            "Comprobante de un pago simulado, sin validez tributaria.",
                            InfoBlue
                        )

                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryLime, contentColor = DarkBackground)
                        ) {
                            Text("Volver a mensualidades", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

private fun textoPagador(nombre: String?, rol: String?): String {
    if (nombre == null) return "-"
    val rolTexto = when (rol) {
        "PAD" -> "apoderado"
        "JUG" -> "jugador"
        else -> null
    }
    return if (rolTexto != null) "$nombre ($rolTexto)" else nombre
}
