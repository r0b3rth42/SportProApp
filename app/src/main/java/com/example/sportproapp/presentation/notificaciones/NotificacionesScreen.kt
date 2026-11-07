package com.example.sportproapp.presentation.notificaciones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
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
import com.example.sportproapp.data.model.Notificacion
import com.example.sportproapp.data.model.formatearFechaHora
import com.example.sportproapp.presentation.auth.*
import com.example.sportproapp.presentation.mensualidades.MensajeVacioMensualidades
import com.example.sportproapp.presentation.mensualidades.MensualidadHeader

// 2.05 Notificaciones
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    navController: NavController,
    viewModel: NotificacionesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarNotificaciones()
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
            val hayNoLeidas = (uiState as? NotificacionesUiState.Success)
                ?.notificaciones?.any { !it.leida } == true

            MensualidadHeader(
                titulo = "Notificaciones",
                onBack = { navController.popBackStack() },
                accion = {
                    if (hayNoLeidas) {
                        TextButton(onClick = { viewModel.marcarTodasComoLeidas() }) {
                            Text("Marcar leídas", color = PrimaryLime, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )

            when (val state = uiState) {
                is NotificacionesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryLime)
                    }
                }

                is NotificacionesUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }
                }

                is NotificacionesUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.cargarNotificaciones(esRefresh = true) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (state.notificaciones.isEmpty()) {
                                item { MensajeVacioMensualidades("No tienes notificaciones.") }
                            } else {
                                items(state.notificaciones) { notificacion ->
                                    ItemNotificacion(
                                        notificacion = notificacion,
                                        onClick = {
                                            viewModel.marcarComoLeida(notificacion)
                                            if (notificacion.tipo == "MENSUALIDAD") {
                                                navController.navigate("payments")
                                            }
                                        }
                                    )
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
private fun ItemNotificacion(notificacion: Notificacion, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.leida) DarkCardBackground else InputBackgroundColor
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (notificacion.leida) CardBorderColor else PrimaryLime.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(PrimaryLime.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (notificacion.tipo == "MENSUALIDAD") Icons.Outlined.Payments else Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = PrimaryLime,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notificacion.titulo,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notificacion.leida) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(PrimaryLime, CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = notificacion.mensaje, color = TextLight, fontSize = 12.sp, lineHeight = 17.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = formatearFechaHora(notificacion.createdAt), color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}
