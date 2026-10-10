package com.example.sportproapp.presentation.entrenamiento

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.presentation.auth.DarkCardBackground
import com.example.sportproapp.presentation.auth.PrimaryLime
import com.example.sportproapp.presentation.auth.TextMuted

// Colores del diseño de Figma
val BackgroundColor = Color(0xFF09120F)
val CardBackgroundColor = Color(0xFF0F1E19)
val PrimaryGreen = Color(0xFFCCFF00)
val TextGray = Color(0xFF8A9E97)
val CardBorderColor = Color(0xFF1B2E28)

@Composable
fun EntrenamientosScreen(
    navController: NavController,
    onAddClick: () -> Unit // Función que se ejecutará al presionar el botón +
) {
    // Datos de prueba basados en tu imagen de Figma
    val listaEntrenamientos = remember {
        listOf(
            Entrenamiento("1", "16:30 - 18:00", "Tigres Sub-15", "Cancha de Fútbol 11 (Césped)", 5, EstadoEntrenamiento.EN_CURSO),
            Entrenamiento("2", "18:00 - 19:30", "Halcones Sub-17", "Campo Sintético Auxiliar", 4, EstadoEntrenamiento.PLANIFICADO),
            Entrenamiento("3", "19:30 - 21:00", "Fútbol Femenino Mayor", "Gimnasio & Arena", 3, EstadoEntrenamiento.COMPLETADO)
        )
    }
    var selectedBottomTab by remember { mutableIntStateOf(2) }

    Scaffold(
        containerColor = BackgroundColor,
        floatingActionButton = {
            // Botón (+) Verde
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = PrimaryGreen,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Entrenamiento",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        bottomBar = {
            // Barra de navegación inferior
            NavigationBar(
                containerColor = DarkCardBackground,
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    Triple("Inicio", Icons.Outlined.Home, 0),
                    Triple("Equipos", Icons.Outlined.Group, 1),
                    Triple("Entrenamientos", Icons.Filled.ModelTraining, 2),
                    Triple("Partidos", Icons.Outlined.EmojiEvents, 3),
                    Triple("Comunidad", Icons.Outlined.ChatBubbleOutline, 4)
                )

                navItems.forEach { (label, icon, index) ->
                    val isSelected = selectedBottomTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            selectedBottomTab = index
                            when (index) {
                                0 -> navController.navigate("home")
                                1 -> navController.navigate("teams")
                                2 -> navController.navigate("entrenamiento")
                                else -> navController.navigate("teams")
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
                .padding(horizontal = 16.dp)
        ) {
            // Header: Título y Subtítulo
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Entrenamientos",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Planificación semanal",
                    color = TextGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Calendario Semanal
            item {
                SelectorDiasSemana()
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Subtítulo
            item {
                Text(
                    text = "Sesiones de hoy",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Lista de Tarjetas de Entrenamientos
            items(listaEntrenamientos) { entrenamiento ->
                TarjetaEntrenamiento(entrenamiento)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SelectorDiasSemana() {
    val dias = listOf(
        "L" to "13",
        "M" to "14",
        "M" to "15",
        "J" to "16",
        "V" to "17",
        "S" to "18",
        "D" to "19"
    )
    val diaSeleccionado = "15" // El día seleccionado en Figma (Miércoles 15)

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(dias) { (letra, numero) ->
            val esSeleccionado = numero == diaSeleccionado
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .size(width = 46.dp, height = 64.dp)
                    .background(
                        color = if (esSeleccionado) PrimaryGreen else CardBackgroundColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (esSeleccionado) Color.Transparent else CardBorderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Text(
                    text = letra,
                    color = if (esSeleccionado) Color.Black else TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = numero,
                    color = if (esSeleccionado) Color.Black else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TarjetaEntrenamiento(entrenamiento: Entrenamiento) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Fila de la Hora y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entrenamiento.horaInicioFin,
                        color = PrimaryGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Badge Estado
                Box(
                    modifier = Modifier
                        .border(1.dp, PrimaryGreen, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = entrenamiento.estado.titulo,
                        color = when (entrenamiento.estado) {
                            EstadoEntrenamiento.EN_CURSO -> PrimaryGreen
                            EstadoEntrenamiento.PLANIFICADO -> TextGray
                            EstadoEntrenamiento.COMPLETADO -> Color(0xFF00FF66)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = CardBorderColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Nombre del Equipo
            Text(
                text = entrenamiento.equipo,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Ubicación y Número de Ejercicios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = entrenamiento.lugar,
                        color = TextGray,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "${entrenamiento.cantidadEjercicios} ejercicios",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
