package com.example.sportproapp.presentation.cuenta

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.sportproapp.presentation.mensualidades.MensualidadHeader

val CerrarSesionRed = Color(0xFFFF3B30)

// 2.06 Mi cuenta (se abre con el avatar de los Home) y 2.07 Cerrar sesión
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiCuentaScreen(
    navController: NavController,
    viewModel: MiCuentaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
    }

    LaunchedEffect(Unit) {
        viewModel.cargarCuenta()
    }

    Scaffold(
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MensualidadHeader(
                titulo = "Mi cuenta",
                onBack = { navController.popBackStack() }
            )

            // --- DATOS DEL USUARIO ---
            CuentaCard(modifier = Modifier.padding(vertical = 4.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(InputBackgroundColor)
                            .border(2.dp, PrimaryLime, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = iniciales(uiState.nombre),
                            color = PrimaryLime,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = uiState.nombre.ifBlank { "Usuario" },
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    if (uiState.email.isNotBlank()) {
                        Text(
                            text = uiState.email,
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (uiState.rol.isNotBlank()) {
                        Surface(
                            color = PrimaryLime.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = etiquetaRol(uiState.rol),
                                color = PrimaryLime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // --- OPCIONES SEGÚN EL ROL ---
            when (uiState.rol) {
                "PAD" -> {
                    SeccionLabel("Mi familia")
                    CuentaCard {
                        if (uiState.isLoading) {
                            CargandoFila()
                        } else if (uiState.hijos.isEmpty()) {
                            OpcionCuenta(
                                icono = Icons.Outlined.Groups,
                                titulo = "Mis hijos",
                                subtitulo = "Aún no tienes hijos vinculados"
                            )
                        } else {
                            uiState.hijos.forEach { hijo ->
                                OpcionCuenta(
                                    icono = Icons.Outlined.Groups,
                                    titulo = hijo.nombre,
                                    subtitulo = hijo.equipo ?: "Sin equipo asignado",
                                    onClick = { navController.navigate("player-profile/${hijo.id}") }
                                )
                                HorizontalDivider(color = CardBorderColor)
                            }
                        }
                        if (uiState.hijos.isEmpty()) HorizontalDivider(color = CardBorderColor)
                        OpcionCuenta(
                            icono = Icons.Outlined.Shield,
                            titulo = "Privacidad de mis hijos",
                            subtitulo = "Qué ve la comunidad",
                            proximamente = true
                        )
                    }
                }
                "JUG" -> {
                    SeccionLabel("Mi perfil")
                    CuentaCard {
                        OpcionCuenta(
                            icono = Icons.Outlined.Person,
                            titulo = "Mi ficha",
                            subtitulo = uiState.equipoJugador ?: "Datos deportivos y de emergencia",
                            onClick = { navController.navigate("player-profile/${uiState.uid}") }
                        )
                    }
                }
                "ADM", "DT" -> {
                    SeccionLabel("Mi academia")
                    CuentaCard {
                        OpcionCuenta(
                            icono = Icons.Outlined.Groups,
                            titulo = "Mis equipos",
                            subtitulo = "Equipos y jugadores de la academia",
                            onClick = { navController.navigate("teams") }
                        )
                    }
                }
            }

            // --- CUENTA ---
            SeccionLabel("Cuenta")
            CuentaCard {
                OpcionCuenta(
                    icono = Icons.Outlined.Notifications,
                    titulo = "Notificaciones",
                    subtitulo = "Avisos y recordatorios",
                    onClick = { navController.navigate("notificaciones") }
                )
                HorizontalDivider(color = CardBorderColor)
                OpcionCuenta(
                    icono = Icons.Outlined.Info,
                    titulo = "Acerca de SportPro",
                    subtitulo = "Versión $version"
                )
            }

            // --- BOTÓN CERRAR SESIÓN ---
            OutlinedButton(
                onClick = { mostrarConfirmacion = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CerrarSesionRed.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CerrarSesionRed.copy(alpha = 0.08f),
                    contentColor = CerrarSesionRed
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Cerrar sesión", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- 2.07 CONFIRMACIÓN DE CIERRE DE SESIÓN ---
    if (mostrarConfirmacion) {
        ModalBottomSheet(
            onDismissRequest = { mostrarConfirmacion = false },
            containerColor = DarkCardBackground,
            dragHandle = { BottomSheetDefaults.DragHandle(color = CardBorderColor) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(CerrarSesionRed.copy(alpha = 0.12f))
                        .border(1.dp, CerrarSesionRed.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        tint = CerrarSesionRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "¿Cerrar sesión?",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Dejarás de recibir avisos de SportPro en este dispositivo hasta que vuelvas a iniciar sesión.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {
                        viewModel.cerrarSesion()
                        mostrarConfirmacion = false
                        // Vuelve al login sin dejar pantallas en el historial
                        navController.navigate("login") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CerrarSesionRed,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Cerrar sesión", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { mostrarConfirmacion = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DarkBackground,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Cancelar", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CuentaCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DarkCardBackground,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

@Composable
private fun SeccionLabel(texto: String) {
    Text(
        text = texto.uppercase(),
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun OpcionCuenta(
    icono: ImageVector,
    titulo: String,
    subtitulo: String? = null,
    proximamente: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = if (proximamente) TextMuted else PrimaryLime,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = if (proximamente) TextMuted else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitulo != null) {
                Text(text = subtitulo, color = TextMuted, fontSize = 11.sp)
            }
        }
        if (proximamente) {
            Surface(
                color = InputBackgroundColor,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Text(
                    text = "Próximamente",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CargandoFila() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = PrimaryLime,
            strokeWidth = 2.dp,
            modifier = Modifier.size(22.dp)
        )
    }
}

private fun iniciales(nombre: String): String {
    val letras = nombre.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    return letras.ifBlank { "?" }
}

private fun etiquetaRol(rol: String): String = when (rol) {
    "PAD" -> "PADRE / APODERADO"
    "JUG" -> "JUGADOR"
    "DT" -> "ENTRENADOR"
    "ADM" -> "ADMINISTRADOR"
    else -> rol
}
