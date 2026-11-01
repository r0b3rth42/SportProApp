package com.example.sportproapp.presentation.entrenamiento

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

private val InputBackgroundColor = Color(0xFF0C1915)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEntrenamientoScreen(
    navController: NavController,
    equipoId: String,
    viewModel: CrearEntrenamientoViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Campos del formulario
    var ubicacion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("2026-11-02") }
    var horaIni by remember { mutableStateOf("16:30:00") }
    var horaFin by remember { mutableStateOf("18:00:00") }

    val snackbarHostState = remember { SnackbarHostState() }

    // Manejo reactivo de los estados del ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CrearEntrenamientoUiState.Success -> {
                Toast.makeText(context, "Entrenamiento creado exitosamente", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                navController.popBackStack()
            }
            is CrearEntrenamientoUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nuevo Entrenamiento",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Planificación semanal",
                    color = TextGray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de días de la semana
                SelectorDiasSemanaCrear()

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Detalles de la sesión",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Ubicación / Sede
                CampoFormulario(
                    titulo = "Ubicación / Sede",
                    placeholder = "Ej. Cancha de Fútbol 11 (Sede Principal)",
                    valor = ubicacion,
                    onValorChange = { ubicacion = it },
                    icono = Icons.Default.LocationOn
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Fecha
                CampoFormulario(
                    titulo = "Fecha (YYYY-MM-DD)",
                    placeholder = "Ej. 2026-11-02",
                    valor = fecha,
                    onValorChange = { fecha = it },
                    icono = Icons.Default.DateRange
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campos Hora de Inicio y Fin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CampoFormulario(
                            titulo = "Hora Inicio",
                            placeholder = "16:30:00",
                            valor = horaIni,
                            onValorChange = { horaIni = it },
                            icono = Icons.Default.Schedule
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        CampoFormulario(
                            titulo = "Hora Fin",
                            placeholder = "18:00:00",
                            valor = horaFin,
                            onValorChange = { horaFin = it },
                            icono = Icons.Default.Schedule
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Botón principal "Crear Entrenamiento"
                val isLoading = uiState is CrearEntrenamientoUiState.Loading

                Button(
                    onClick = {
                        viewModel.guardarEntrenamiento(
                            fecha = fecha,
                            horaIni = horaIni,
                            horaFin = horaFin,
                            equipoId = equipoId,
                            ubicacion = ubicacion
                        )
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Crear Entrenamiento",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

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