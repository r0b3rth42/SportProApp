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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
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

private val InputBackgroundColor = Color(0xFF0C1915)

@Composable
fun CrearEntrenamientoScreen(
    navController: NavController,
    onCrearClick: () -> Unit
) {
    // Variables de estado para los campos del formulario
    var equipo by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
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

            // Botón principal "Crear Entrenamiento" integrado en el flujo de scroll
            Button(
                onClick = onCrearClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Crear Entrenamiento",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Espacio inferior para evitar que la barra de navegación global cubra el contenido final
            Spacer(modifier = Modifier.height(90.dp))
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