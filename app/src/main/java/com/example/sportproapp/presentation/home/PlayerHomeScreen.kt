package com.example.sportproapp.presentation.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.data.model.JugadorPerfil
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import com.example.sportproapp.presentation.auth.*
import com.google.firebase.auth.FirebaseAuth

// --- MODELOS DE DATOS PARA EL DASHBOARD ---
data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val route: String
)

data class UpcomingEvent(
    val title: String,
    val category: String,
    val time: String,
    val date: String,
    val location: String
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerHomeScreen(
    navController: NavController,
    userName: String = "Carlos Ortiz",
    userRole: String? = "JUG" // "DT", "JUG", "PAD", "ADM"
) {
    // Estado para consultar los datos del perfil del jugador en Supabase
    var playerProfile by remember { mutableStateOf<JugadorPerfil?>(null) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Cargar perfil si el usuario actual es un jugador
    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null && userRole == "JUG") {
            val result = UsuarioRepository.getPlayerProfile(uid)
            playerProfile = result.getOrNull()
        }
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
            // --- 0. CABECERA SUPERIOR (SALUDO, FOTO Y NOTIFICACIONES) ---
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
                        // Avatar Circular
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(InputBackgroundColor)
                                .border(1.5.dp, PrimaryLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Perfil",
                                tint = TextLight,
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
                                    text = getRoleBadgeText(userRole),
                                    color = PrimaryLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Botón Notificaciones
                    IconButton(
                        onClick = { /* Ir a notificaciones */ },
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

            // --- 1. TARJETA DE VINCULACIÓN DE APODERADO (SI ES MENOR Y PENDIENTE) ---
            playerProfile?.let { perfil ->
                if (perfil.estadoVinculacion == "PENDIENTE" && !perfil.codigoVinculacion.isNullOrEmpty()) {
                    item {
                        val esMenorEdadManual = remember(perfil.fechaNacimiento) {
                            try {
                                val fechaNac = java.time.LocalDate.parse(perfil.fechaNacimiento)
                                val hoy = java.time.LocalDate.now()
                                java.time.Period.between(fechaNac, hoy).years < 18
                            } catch (e: Exception) {
                                false
                            }
                        }

                        if (esMenorEdadManual) {
                            PendingParentLinkCard(
                                codigoVinculacion = perfil.codigoVinculacion,
                                onCardClick = { navController.navigate("link_parent_code") }
                            )
                        }
                    }
                }
            }

            // --- 2. BANNER DE PRÓXIMO ENTRENAMIENTO ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Próximo Entrenamiento",
                                color = PrimaryLime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Categoría Sub-17",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hoy, 4:30 PM", color = TextLight, fontSize = 13.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Place, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancha Principal N° 2", color = TextLight, fontSize = 13.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(PrimaryLime, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                tint = DarkBackground,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // --- 3. ACCIONES RÁPIDAS ---
            item {
                Column {
                    Text(
                        text = "Acciones rápidas",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val actions = getRoleQuickActions(userRole)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(actions) { action ->
                            QuickActionItem(action = action) {
                                navController.navigate(action.route)
                            }
                        }
                    }
                }
            }

            // --- 4. PRÓXIMOS EVENTOS Y CONVOCATORIAS ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agenda y Convocatorias",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { /* Ver todo */ }) {
                        Text("Ver todo", color = PrimaryLime, fontSize = 13.sp)
                    }
                }
            }

            val mockEvents = listOf(
                UpcomingEvent("Partido vs Academia Central", "Torneo Local", "10:00 AM", "Sáb, 12 Oct", "Estadio Municipal"),
                UpcomingEvent("Entrenamiento Táctico", "Sub-17", "04:30 PM", "Mar, 15 Oct", "Cancha N° 1"),
                UpcomingEvent("Reunión de Padres / Apoderados", "Institucional", "07:00 PM", "Jue, 17 Oct", "Aula Magna")
            )

            items(mockEvents) { event ->
                EventCard(event = event)
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// --- COMPONENTE: TARJETA DE VINCULACIÓN DE APODERADO PENDIENTE ---
@Composable
fun PendingParentLinkCard(
    codigoVinculacion: String,
    onCardClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, PrimaryLime, RoundedCornerShape(20.dp))
            .then(if (onCardClick != null) Modifier.clickable { onCardClick() } else Modifier),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF162D24), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FamilyRestroom,
                            contentDescription = null,
                            tint = PrimaryLime,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Vincular con tu Apoderado",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toca para ver el código en pantalla completa",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = PrimaryLime,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Caja del Código
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InputBackgroundColor, RoundedCornerShape(12.dp))
                    .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = codigoVinculacion,
                    color = PrimaryLime,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp
                )
            }

            // Botones Copiar / Compartir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Código SportPro", codigoVinculacion)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLight)
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "¡Hola! Mi código de vinculación para SportPro es: $codigoVinculacion"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Compartir mediante"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryLime),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryLime)
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compartir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- COMPONENTE: ITEM DE ACCIÓN RÁPIDA ---
@Composable
fun QuickActionItem(action: QuickAction, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(86.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(InputBackgroundColor, shape = RoundedCornerShape(16.dp))
                .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                tint = PrimaryLime,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = action.title,
            color = TextLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2
        )
    }
}

// --- COMPONENTE: TARJETA DE EVENTO ---
@Composable
fun EventCard(event: UpcomingEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.category, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(text = event.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Place, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = event.location, color = TextLight, fontSize = 12.sp)
                }
            }

            Surface(
                color = InputBackgroundColor,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = event.date, color = PrimaryLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = event.time, color = TextLight, fontSize = 11.sp)
                }
            }
        }
    }
}

// --- FUNCIONES AUXILIARES DE ROL ---
fun getRoleBadgeText(role: String?): String {
    return when (role) {
        "DT" -> "Director Técnico"
        "JUG" -> "Jugador Fichado"
        "PAD" -> "Apoderado / Tutor"
        "ADM" -> "Administrador"
        else -> "Usuario"
    }
}

fun getRoleQuickActions(role: String?): List<QuickAction> {
    return when (role) {
        "DT" -> listOf(
            QuickAction("Tomar Asistencia", Icons.Outlined.Checklist, "attendance"),
            QuickAction("Crear Convocatoria", Icons.Outlined.GroupAdd, "create_match")
        )
        "PAD" -> listOf(
            QuickAction("Mis Representados", Icons.Outlined.FamilyRestroom, "my_minors"),
            QuickAction("Pagos y Cuotas", Icons.Outlined.Payments, "payments")
        )
        "ADM" -> listOf(
            QuickAction("Cobranzas", Icons.Outlined.PointOfSale, "admin_payments"),
            QuickAction("Gestión Usuarios", Icons.Outlined.AdminPanelSettings, "manage_users")
        )
        else -> listOf( // JUG (Jugador)
            QuickAction("Código Padre", Icons.Outlined.FamilyRestroom, "link_parent_code"),
            QuickAction("Entrenamientos", Icons.Outlined.FitnessCenter, "trainings_screen"),
            QuickAction("Partidos", Icons.Outlined.SportsSoccer, "matches_screen")
        )
    }
}