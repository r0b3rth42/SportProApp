package com.example.sportproapp.presentation.home

import android.widget.Toast
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.data.model.JugadorConUsuario
import com.example.sportproapp.data.model.JugadorPerfil
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import com.example.sportproapp.presentation.auth.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentHomeScreen(
    navController: NavController,
    userName: String = "Padre de Familia"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser

    var codigoInput by remember { mutableStateOf("") }
    var isLinking by remember { mutableStateOf(false) }
    var misRepresentados by remember { mutableStateOf<List<JugadorConUsuario>>(emptyList()) }
    var isLoadingList by remember { mutableStateOf(true) }

    // Cargar la lista de hijos/representados vinculados
    val cargarRepresentados = {
        if (currentUser != null) {
            scope.launch {
                isLoadingList = true
                val result = UsuarioRepository.getMisRepresentadosConUsuario(currentUser.uid)
                misRepresentados = result.getOrNull() ?: emptyList()
                isLoadingList = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarRepresentados()
    }

    Scaffold(
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // --- 0. ENCABEZADO DE BIENVENIDA ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(InputBackgroundColor)
                                .border(1.5.dp, PrimaryLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FamilyRestroom,
                                contentDescription = null,
                                tint = PrimaryLime,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "¡Hola, $userName! 👋",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                color = Color(0xFF162D24),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                            ) {
                                Text(
                                    text = "Apoderado / Tutor",
                                    color = PrimaryLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { /* Notificaciones */ },
                        modifier = Modifier
                            .size(42.dp)
                            .background(DarkCardBackground, CircleShape)
                            .border(1.dp, CardBorderColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notificaciones",
                            tint = TextLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // --- 1. TARJETA PARA INGRESAR CÓDIGO DE VINCULACIÓN ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryLime, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.GroupAdd,
                                contentDescription = null,
                                tint = PrimaryLime,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Vincular nuevo hijo / representado",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Ingresa el código de 6 dígitos generado en el celular de tu hijo (ej. SP-8392):",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = codigoInput,
                                onValueChange = { if (it.length <= 8) codigoInput = it.uppercase() },
                                placeholder = { Text("SP-XXXX", color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
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

                            Button(
                                onClick = {
                                    if (codigoInput.isBlank()) {
                                        Toast.makeText(context, "Ingresa un código válido", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (currentUser == null) return@Button

                                    isLinking = true
                                    scope.launch {
                                        val result = UsuarioRepository.vincularJugadorPorCodigo(
                                            idPadre = currentUser.uid,
                                            codigoIngresado = codigoInput
                                        )
                                        isLinking = false

                                        result.fold(
                                            onSuccess = {
                                                codigoInput = ""
                                                Toast.makeText(context, "¡Jugador vinculado con éxito!", Toast.LENGTH_SHORT).show()
                                                cargarRepresentados()
                                            },
                                            onFailure = { ex ->
                                                Toast.makeText(context, ex.message ?: "Error al vincular", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                },
                                enabled = !isLinking,
                                modifier = Modifier.height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryLime,
                                    contentColor = DarkBackground
                                )
                            ) {
                                if (isLinking) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DarkBackground)
                                } else {
                                    Text("Vincular", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // --- 2. LISTA DE MIS REPRESENTADOS ---
            item {
                Text(
                    text = "Mis Representados (${misRepresentados.size})",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isLoadingList) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryLime)
                    }
                }
            } else if (misRepresentados.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = InputBackgroundColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aún no tienes jugadores vinculados.",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(misRepresentados) { jugador ->
                    RepresentedPlayerCard(jugador = jugador) {
                        navController.navigate("player-profile/${jugador.perfil.id}")
                    }
                }
            }

            // --- 3. ACCIONES RÁPIDAS DEL APODERADO ---
            item {
                Column {
                    Text(
                        text = "Acciones del Apoderado",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val actions = listOf(
                        QuickAction("Autorizaciones", Icons.Outlined.FactCheck, "authorizations"),
                        QuickAction("Pagos y Cuotas", Icons.Outlined.Payments, "payments"),
                        QuickAction("Asistencia", Icons.Outlined.CalendarMonth, "attendance_summary")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(actions) { action ->
                            QuickActionItem(action = action) {
                                navController.navigate(action.route)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// --- COMPONENTE: TARJETA DE JUGADOR REPRESENTADO ---
@Composable
fun RepresentedPlayerCard(
    jugador: JugadorConUsuario,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(InputBackgroundColor)
                        .border(1.dp, PrimaryLime, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SportsSoccer,
                        contentDescription = null,
                        tint = PrimaryLime,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Jugador: ${jugador.usuario.name.take(8)}...",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Posición: ${jugador.perfil.posicion} | Altura: ${jugador.perfil.alturaCm} cm",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}