package com.example.sportproapp.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
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

// --- MODELOS DE DATOS ---
data class RecentActivity(
    val id: String,
    val text: String,
    val timeAgo: String,
    val icon: ImageVector,
    val iconTint: Color = PrimaryLime
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    navController: NavController,
    userName: String = "Carlos",
    userTitle: String = "Entrenador • Club Tigres"
) {
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Acción para agregar evento / registrar */ },
                containerColor = PrimaryLime,
                contentColor = DarkBackground,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkCardBackground,
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    Triple("Inicio", Icons.Filled.Home, 0),
                    Triple("Equipos", Icons.Outlined.Group, 1),
                    Triple("Entrenamientos", Icons.Outlined.Cancel, 2),
                    Triple("Partidos", Icons.Outlined.EmojiEvents, 3),
                    Triple("Comunidad", Icons.Outlined.ChatBubbleOutline, 4)
                )

                navItems.forEach { (label, icon, index) ->
                    val isSelected = selectedBottomTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            selectedBottomTab = index
                            if(label == "Inicio") {
                                navController.navigate("adminHome")
                            }
                            if(label == "Equipos") {
                                navController.navigate("teams")
                            }
                            if(label == "Entrenamientos") {

                            }
                            if(label == "Partidos") {

                            }
                            if(label == "Comunidad") {

                            }

                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryLime,
                            selectedTextColor = PrimaryLime,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. CABECERA DE BIENVENIDA ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hola, $userName",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "👋", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userTitle,
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }

                    // Avatar con indicador de perfil
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(DarkCardBackground)
                            .border(2.dp, PrimaryLime, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Perfil",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // --- 2. GRILLA DE TARJETAS 2x2 ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Fila 1: Entrenamiento & VS Leones FC
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.CalendarToday,
                            tag = "ENTRENAMIENTO",
                            mainText = "Hoy, 18:00",
                            subText = "Cancha Principal"
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.EmojiEvents,
                            tag = "VS. LEONES FC",
                            mainText = "Sáb, 15:30",
                            subText = "Liga Sub-15"
                        )
                    }

                    // Fila 2: Jugadores Activos & Pendientes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.Group,
                            tag = "JUGADORES",
                            mainText = "24",
                            subText = "22 Activos hoy",
                            showStatusDot = true
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.AttachMoney,
                            tag = "PENDIENTES",
                            mainText = "3",
                            subText = "Alertas de pago"
                        )
                    }
                }
            }

            // --- 3. SECCIÓN ACTIVIDAD RECIENTE ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Actividad Reciente",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ver todo",
                        color = PrimaryLime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { /* Ver todo */ }
                    )
                }
            }

            // Lista mock de actividades
            val recentActivities = listOf(
                RecentActivity("1", "Santiago Méndez registró su asistencia para hoy.", "Hace 10m", Icons.Outlined.CheckCircle),
                RecentActivity("2", "Mateo López subió comprobante de pago de Mayo.", "Hace 1h", Icons.Outlined.Description),
                RecentActivity("3", "Nuevo partido programado contra Jaguares FC.", "Hace 3h", Icons.Outlined.CalendarToday)
            )

            items(recentActivities) { activity ->
                ActivityCardItem(activity = activity)
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// --- COMPONENTE: TARJETA DE MÉTRICAS (GRILLA 2x2) ---
@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tag: String,
    mainText: String,
    subText: String,
    showStatusDot: Boolean = false
) {
    Card(
        modifier = modifier
            .height(115.dp)
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryLime,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = tag,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    text = mainText,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showStatusDot) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(PrimaryLime, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = subText,
                        color = if (showStatusDot) PrimaryLime else TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// --- COMPONENTE: ITEM DE ACTIVIDAD RECIENTE ---
@Composable
fun ActivityCardItem(activity: RecentActivity) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(InputBackgroundColor, shape = RoundedCornerShape(12.dp))
                    .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activity.icon,
                    contentDescription = null,
                    tint = activity.iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activity.timeAgo,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}