package com.example.sportproapp.presentation.home

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.presentation.auth.*

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerHomeScreen(
    navController: NavController,
    userName: String = "Carlos Ortiz",
    userRole: String = "JUG" // "DT", "JUG", "PAD", "ADM"
) {
    var selectedBottomTab by remember { mutableIntStateOf(0) }


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // --- 1. TARJETA DE RESUMEN PRINCIPAL (BANNER) ---
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

            // --- 2. ACCIONES RÁPIDAS (SEGÚN EL ROL) ---
            item {
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

            // --- 3. PRÓXIMOS EVENTOS Y CONVOCATORIAS ---
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

            item { Spacer(modifier = Modifier.height(16.dp)) }
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

// --- FUNCIONES AUXILIARES PARA ADAPTAR EL CONTENIDO SEGÚN ROL ---
fun getRoleBadgeText(role: String): String {
    return when (role) {
        "DT" -> "Director Técnico / Entrenador"
        "JUG" -> "Jugador Fichado"
        "PAD" -> "Apoderado / Tutor"
        "ADM" -> "Administrador de Academia"
        else -> "Usuario"
    }
}

// --- 1. ACCIONES RÁPIDAS (Solo Módulos de Entrenamientos y Partidos) ---
fun getRoleQuickActions(role: String): List<QuickAction> {
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
        else -> listOf( // JUG (Jugador): Módulos solicitados
            QuickAction("Entrenamientos", Icons.Outlined.FitnessCenter, "trainings_screen"),
            QuickAction("Partidos", Icons.Outlined.SportsSoccer, "matches_screen")
        )
    }
}