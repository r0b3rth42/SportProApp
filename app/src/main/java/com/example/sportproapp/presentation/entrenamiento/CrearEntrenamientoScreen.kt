package com.example.sportproapp.presentation.entrenamiento

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ModelTraining
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.sportproapp.presentation.auth.DarkCardBackground
import com.example.sportproapp.presentation.auth.PrimaryLime
import com.example.sportproapp.presentation.auth.TextMuted

// Reutilizamos la paleta de colores del diseño
// private val BackgroundColor = Color(0xFF09120F)
// private val CardBackgroundColor = Color(0xFF0F1E19)
// private val PrimaryGreen = Color(0xFFCCFF00)
// private val TextGray = Color(0xFF8A9E97)
// private val CardBorderColor = Color(0xFF1B2E28)
private val InputBackgroundColor = Color(0xFF0C1915)

@Composable
fun CrearEntrenamientoScreen(
    navController: NavController,
    onCrearClick: () -> Unit)
{
    // Variables de estado para los campos del formulario
    var equipo by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var selectedBottomTab by remember { mutableIntStateOf(2) }

    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundColor)
            ) {
                // Botón principal "Crear Entrenamiento"
                Button(
                    onClick = onCrearClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(52.dp)
                ) {
                    Text(
                        text = "Crear Entrenamiento",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
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

            // Calendario Semanal
            SelectorDiasSemanaCrear()

            Spacer(modifier = Modifier.height(24.dp))

            // Subtítulo
            Text(
                text = "Sesiones de hoy",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campos del formulario según Figma
            CampoFormulario(
                titulo = "Equipo",
                placeholder = "Tigres Sub-15",
                valor = equipo,
                onValorChange = { equipo = it },
                icono = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoFormulario(
                titulo = "Ubicacion",
                placeholder = "Cancha de Fútbol 11",
                valor = ubicacion,
                onValorChange = { ubicacion = it },
                icono = Icons.Default.LocationOn
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoFormulario(
                titulo = "Fecha",
                placeholder = "15/10/2026",
                valor = fecha,
                onValorChange = { fecha = it },
                icono = Icons.Default.DateRange
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoFormulario(
                titulo = "Hora",
                placeholder = "18:00 - 19:30",
                valor = hora,
                onValorChange = { hora = it },
                icono = Icons.Default.Schedule
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFormulario(
    titulo: String,
    placeholder: String,
    valor: String,
    onValorChange: (String) -> Unit,
    icono: ImageVector
) {
    Column {
        Text(
            text = titulo,
            color = TextGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextGray.copy(alpha = 0.5f),
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InputBackgroundColor,
                unfocusedContainerColor = InputBackgroundColor,
                focusedBorderColor = CardBorderColor,
                unfocusedBorderColor = CardBorderColor,
                cursorColor = PrimaryGreen,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

@Composable
fun SelectorDiasSemanaCrear() {
    val dias = listOf(
        "L" to "13",
        "M" to "14",
        "M" to "15",
        "J" to "16",
        "V" to "17",
        "S" to "18",
        "D" to "19"
    )
    val diaSeleccionado = "15"

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
