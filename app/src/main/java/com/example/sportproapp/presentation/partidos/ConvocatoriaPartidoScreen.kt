package com.example.sportproapp.presentation.partidos

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sportproapp.presentation.auth.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvocatoriaPartidoScreen(
    navController: NavController,
    partidoId: Long,
    equipoId: String,
    viewModel: ConvocatoriaPartidoViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isSending by viewModel.isSending.collectAsState()

    LaunchedEffect(partidoId, equipoId) {
        viewModel.cargarDatos(partidoId, equipoId)
    }

    val filtros = listOf("Todos", "Portero", "Defensa", "Mediocampista", "Delantero")

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Convocatoria",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Text(
                            text = "Arma la lista para el partido",
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
            if (uiState is ConvocatoriaUiState.Success) {
                val state = uiState as ConvocatoriaUiState.Success
                val seleccionados = state.jugadores.count { it.isSelected }
                val total = state.jugadores.size

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Convocados:",
                            color = TextMuted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$seleccionados / $total Seleccionados",
                            color = PrimaryLime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.enviarConvocatoria(partidoId, equipoId) {
                                Toast.makeText(context, "¡Convocatoria enviada y guardada exitosamente!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        },
                        enabled = !isSending,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryLime,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DarkBackground)
                        } else {
                            Text(
                                text = "Enviar Convocatoria",
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
                is ConvocatoriaUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryLime)
                    }
                }
                is ConvocatoriaUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is ConvocatoriaUiState.Success -> {
                    val partido = state.partido

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // --- 1. TARJETA CABECERA DEL PARTIDO ---
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, CardBorderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Equipos
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = partido?.equipoLocalNombre ?: "Equipo Local",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Start
                                        )
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                                .background(InputBackgroundColor, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "VS",
                                                color = PrimaryLime,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = partido?.equipoVisitanteNombre ?: "Equipo Rival",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.End
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = CardBorderColor, thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Fecha y Sede
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Outlined.Event, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = partido?.fecha ?: "Hoy", color = TextLight, fontSize = 12.sp)

                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = partido?.hora ?: "15:30", color = TextLight, fontSize = 12.sp)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Outlined.Place, contentDescription = null, tint = PrimaryLime, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Sede: ${partido?.sedeNombre?.ifBlank { "Estadio Municipal" }}",
                                                color = PrimaryLime,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // --- 2. CHIPS DE FILTRO POR POSICIÓN ---
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filtros.forEach { filtro ->
                                    val isSelected = state.filtroPosicion == filtro
                                    Surface(
                                        color = if (isSelected) PrimaryLime else DarkCardBackground,
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, if (isSelected) PrimaryLime else CardBorderColor),
                                        modifier = Modifier.clickable { viewModel.setFiltroPosicion(filtro) }
                                    ) {
                                        Text(
                                            text = filtro,
                                            color = if (isSelected) DarkBackground else TextLight,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // --- 3. LISTA DE JUGADORES ---
                        if (state.jugadoresFiltrados.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No hay jugadores en esta categoría.", color = TextMuted)
                                }
                            }
                        } else {
                            items(state.jugadoresFiltrados) { jugador ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, CardBorderColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleSeleccion(jugador.jugadorId) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Avatar
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(CircleShape)
                                                    .background(InputBackgroundColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (!jugador.fotografiaUrl.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = jugador.fotografiaUrl,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = TextMuted,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }

                                            // Nombre y posición
                                            Column {
                                                Text(
                                                    text = jugador.nombre,
                                                    color = Color.White,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = jugador.posicion,
                                                    color = TextMuted,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }

                                        // Checkbox personalizado / indicador
                                        Checkbox(
                                            checked = jugador.isSelected,
                                            onCheckedChange = { viewModel.toggleSeleccion(jugador.jugadorId) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = PrimaryLime,
                                                uncheckedColor = CardBorderColor,
                                                checkmarkColor = DarkBackground
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(90.dp)) }
                    }
                }
            }
        }
    }
}
