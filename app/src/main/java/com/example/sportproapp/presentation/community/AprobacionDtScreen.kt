package com.example.sportproapp.presentation.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Warning
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
import com.example.sportproapp.presentation.auth.CardBorderColor
import com.example.sportproapp.presentation.auth.DarkBackground
import com.example.sportproapp.presentation.auth.DarkCardBackground
import com.example.sportproapp.presentation.auth.InputBackgroundColor
import com.example.sportproapp.presentation.auth.PrimaryLime
import com.example.sportproapp.presentation.auth.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AprobacionDtScreen(
    navController: NavController,
    publicacionId: String,
    viewModel: AprobacionDtViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(publicacionId) {
        viewModel.cargarPublicacion(publicacionId)
    }

    LaunchedEffect(uiState) {
        if (uiState is AprobacionUiState.Finished) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Aprobación del Director Técnico",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Revisión final de crónica oficial",
                            color = TextMuted,
                            fontSize = 12.sp
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        when (val state = uiState) {
            is AprobacionUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryLime)
                }
            }

            is AprobacionUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = Color.Red, fontSize = 14.sp)
                }
            }

            is AprobacionUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- 1. BANNER PENDIENTE DE APROBACIÓN ---
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFFFC107), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF26200A))
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
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pendiente de aprobación",
                                        color = Color(0xFFFFC107),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Por: ${state.publicacionConAutor.autorNombre}",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // --- 2. TEXTO DE CRÓNICA OFICIAL ---
                    item {
                        Column {
                            Text(
                                text = "TEXTO DE CRÓNICA OFICIAL",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = state.textoCronica,
                                onValueChange = { viewModel.updateTextoCronica(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 150.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkCardBackground,
                                    unfocusedContainerColor = DarkCardBackground,
                                    focusedBorderColor = PrimaryLime,
                                    unfocusedBorderColor = CardBorderColor,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }

                    // --- 3. NOTAS O SUGERENCIAS INTERNAS ---
                    item {
                        Column {
                            Text(
                                text = "Notas o sugerencias internas:",
                                color = TextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = state.notasInternas,
                                onValueChange = { viewModel.updateNotasInternas(it) },
                                placeholder = {
                                    Text(
                                        text = "Ej. \"Ajustar el minuto del primer gol antes de publicar...\"",
                                        color = TextMuted.copy(alpha = 0.6f),
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 80.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = InputBackgroundColor,
                                    unfocusedContainerColor = InputBackgroundColor,
                                    focusedBorderColor = PrimaryLime,
                                    unfocusedBorderColor = CardBorderColor,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }

                    // --- 4. HISTORIAL DE VERSIONES ---
                    item {
                        Column {
                            Text(
                                text = "HISTORIAL DE VERSIONES",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = state.publicacionConAutor.publicacion.versionInfo
                                            ?: "Versión 1",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (state.publicacionConAutor.fechaHoraFormateada.isNotBlank())
                                            state.publicacionConAutor.fechaHoraFormateada
                                        else
                                            state.publicacionConAutor.tiempoRelativo,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // --- 5. BOTONES DE ACCIÓN ---
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            // Aprobar y Publicar
                            Button(
                                onClick = { viewModel.aprobarYPublicar() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryLime,
                                    contentColor = DarkBackground
                                )
                            ) {
                                Text(
                                    text = "Aprobar y Publicar en Muro",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Fila: Solicitar cambios & Rechazar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.solicitarCambios() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFC107))
                                ) {
                                    Text(
                                        text = "Solicitar cambios",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.rechazar() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                                ) {
                                    Text(
                                        text = "Rechazar",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }

            else -> {}
        }
    }
}
