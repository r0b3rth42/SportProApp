package com.example.sportproapp.presentation.entrenamiento

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportproapp.data.model.BloqueEntrenamiento
import com.example.sportproapp.data.model.Ejercicio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEntrenamientoScreen(
    navController: NavController,
    entrenamientoId: String,
    fechaSubtitulo: String = "Mier, 15 de Mayo • Tigres Sub-15",
    onAgregarEjercicioClick: (BloqueEntrenamiento) -> Unit = {},
    viewModel: DetalleEntrenamientoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(entrenamientoId) {
        viewModel.cargarDetalle(entrenamientoId)
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Detalle de Sesión",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = fechaSubtitulo,
                            color = TextGray,
                            fontSize = 13.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is DetalleEntrenamientoUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            is DetalleEntrenamientoUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is DetalleEntrenamientoUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // BANNER DURACIÓN TOTAL
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Duración Total estimada:",
                                        color = PrimaryGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Text(
                                    text = "${state.duracionTotalMin} MIN",
                                    color = PrimaryGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // BLOQUES DE EJERCICIOS
                    BloqueEntrenamiento.values().forEach { bloque ->
                        val ejerciciosBloque = state.ejercicios.filter { it.bloque == bloque }

                        item {
                            SeccionHeader(
                                titulo = bloque.tituloMostrar,
                                onAgregarClick = { onAgregarEjercicioClick(bloque) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (ejerciciosBloque.isEmpty()) {
                            item {
                                Text(
                                    text = "Sin ejercicios asignados",
                                    color = TextGray,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
                                )
                            }
                        } else {
                            items(ejerciciosBloque) { ejercicio ->
                                TarjetaEjercicioItem(ejercicio = ejercicio)
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        item { Spacer(modifier = Modifier.height(12.dp)) }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun SeccionHeader(
    titulo: String,
    onAgregarClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            color = PrimaryGreen,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedButton(
            onClick = onAgregarClick,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = CardBackgroundColor,
                contentColor = TextGray
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(
                text = "+ Agregar",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TarjetaEjercicioItem(ejercicio: Ejercicio) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ejercicio.titulo,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${ejercicio.duracionMin} min",
                        color = PrimaryGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                ejercicio.descripcion?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}