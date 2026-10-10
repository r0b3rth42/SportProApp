package com.example.sportproapp.presentation.jugador

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.presentation.auth.*

// --- MODELOS DE DATOS ---
data class Player(
    val id: String,
    val name: String,
    val position: String,
    val number: Int,
    val status: PlayerStatus
)

enum class PlayerStatus(val text: String, val color: Color) {
    ACTIVE("Activo", Color(0xFF4CAF50)),      // Verde
    INJURED("Lesionado", Color(0xFFF44336))   // Rojo
}

@Composable
fun TeamDetailScreen(
    navController: NavController,
    teamName: String = "Sin asignar",
    category: String = "Sin asignar",
    playerCount: Int = 18,
    season: String = "Temp. 2026"
) {
    var selectedTab by remember { mutableIntStateOf(0) } // Tabs: Jugadores, Calendario, Estadísticas
    val tabs = listOf("Jugadores", "Calendario", "Estadísticas")

    // Contenedor principal con fondo oscuro
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- 1. CABECERA DEL EQUIPO ---
            TeamHeaderCard(
                teamName = teamName,
                category = category,
                playerCount = playerCount,
                season = season,
                onBackClick = {
                    navController.navigate("teams") {
                        popUpTo("teams") { inclusive = true }
                    }
                },
                onEditClick = { /* Acción de editar */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. TABS (Jugadores, Calendario, Estadísticas) ---
            CustomTabs(
                tabs = tabs,
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. LISTA DE JUGADORES ---
            if (selectedTab == 0) {
                val players = listOf(
                    Player("1", "Santiago Méndez", "Mediocampista", 10, PlayerStatus.ACTIVE),
                    Player("2", "Mateo López", "Delantero", 7, PlayerStatus.ACTIVE),
                    Player("3", "Carlos Ruiz", "Portero", 1, PlayerStatus.INJURED)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(players) { player ->
                        PlayerCardItem(player = player)
                    }
                    // Espacio adicional abajo para evitar que el contenido sea tapado por la barra inferior global
                    item { Spacer(modifier = Modifier.height(90.dp)) }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Contenido de ${tabs[selectedTab]}", color = TextMuted)
                }
            }
        }

        // --- 4. BOTÓN FLOTANTE (FAB) ---
        FloatingActionButton(
            onClick = { /* Acción para agregar jugador */ },
            containerColor = PrimaryLime,
            contentColor = DarkBackground,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(60.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar Jugador",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// --- COMPONENTE: CABECERA DEL EQUIPO ---
@Composable
fun TeamHeaderCard(
    teamName: String,
    category: String,
    playerCount: Int,
    season: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Atrás
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(InputBackgroundColor, CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Botón Editar
                Button(
                    onClick = onEditClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryLime,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Editar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = teamName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = category,
                    color = PrimaryLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(PrimaryLime.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "$playerCount Jugadores • $season",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// --- COMPONENTE: TABS PERSONALIZADOS ---
@Composable
fun CustomTabs(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(8.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(index) }
                    .background(
                        if (isSelected) DarkCardBackground else Color.Transparent,
                        shape = if (isSelected) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
                    )
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) PrimaryLime else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) PrimaryLime else TextMuted,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// --- COMPONENTE: ITEM DE JUGADOR ---
@Composable
fun PlayerCardItem(player: Player) {
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${player.position} • Dorsal #${player.number}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Text(
                text = player.status.text,
                color = player.status.color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(player.status.color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}