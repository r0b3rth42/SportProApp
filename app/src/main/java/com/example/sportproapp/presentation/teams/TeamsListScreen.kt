package com.example.sportproapp.presentation.teams

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
import androidx.compose.material.icons.filled.Group
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

// --- MODELO DE DATOS DE EQUIPO ---
data class Team(
    val id: String,
    val name: String,
    val playerCount: Int,
    val coachName: String,
    val stripeColor: Color,
    val badgeTag: String = "+ sub-división"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsListScreen(
    navController: NavController,
    onTeamClick: (String) -> Unit = {},
    onAddTeamClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedBottomTab by remember { mutableIntStateOf(1) } // 1 = Equipos

    // Lista mock de equipos basada en tu diseño
    val teamsList = remember {
        listOf(
            Team("1", "Tigres Academia", 18, "Carlos Ortiz", Color(0xFFFF2A6D)),
            Team("2", "Halcones Norte", 22, "Andrés Silva", PrimaryLime),
            Team("3", "Leones Sub-13", 16, "Carlos Ortiz", Color(0xFF007AFF))
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("teams-create")
                },
                containerColor = PrimaryLime,
                contentColor = DarkBackground,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Equipo",
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
                    Triple("Inicio", Icons.Outlined.Home, 0),
                    Triple("Equipos", Icons.Filled.Group, 1),
                    Triple("Entrenamientos", Icons.Outlined.Cancel, 2),
                    Triple("Partidos", Icons.Outlined.EmojiEvents, 3),
                    Triple("Comunidad", Icons.Outlined.ChatBubbleOutline, 4)
                )

                navItems.forEach { (label, icon, index) ->
                    val isSelected = selectedBottomTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedBottomTab = index },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // --- 1. CABECERA Y FILTRO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mis Equipos",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                // Botón de Filtro
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(InputBackgroundColor, shape = RoundedCornerShape(12.dp))
                        .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                        .clickable { /* Abrir filtros */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Filtros",
                        tint = PrimaryLime,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // --- 2. BARRA DE BÚSQUEDA ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                placeholder = {
                    Text("Buscar por equipo o categoría...", color = TextMuted, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = TextMuted)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputBackgroundColor,
                    unfocusedContainerColor = InputBackgroundColor,
                    focusedBorderColor = PrimaryLime,
                    unfocusedBorderColor = CardBorderColor,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // --- 3. LISTA DE TARJETAS DE EQUIPO ---
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val filteredTeams = teamsList.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.coachName.contains(searchQuery, ignoreCase = true)
                }

                items(filteredTeams) { team ->
                    TeamCardItem(team = team, onClick = { onTeamClick(team.id) })
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

// --- COMPONENTE: TARJETA DE EQUIPO CON BANDA LATERAL ---
@Composable
fun TeamCardItem(
    team: Team,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Franja vertical de color distintivo
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(team.stripeColor)
            )

            // Contenido de la tarjeta
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Fila Superior: Nombre del equipo y Etiqueta Sub-división
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = team.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = InputBackgroundColor,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Text(
                            text = team.badgeTag,
                            color = PrimaryLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Fila Inferior: Cantidad de jugadores y Nombre del Entrenador
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cantidad de Jugadores
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Group,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${team.playerCount} Jugadores",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }

                    // Entrenador
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Cancel, // Ícono circular de perfil/detalles
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = team.coachName,
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}