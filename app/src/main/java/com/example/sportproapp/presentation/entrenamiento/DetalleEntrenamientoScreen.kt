package com.example.sportproapp.presentation.entrenamiento

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportproapp.data.model.BloqueEntrenamiento
import com.example.sportproapp.data.model.Ejercicio
import com.example.sportproapp.presentation.auth.InputBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEntrenamientoScreen(
    navController: NavController,
    entrenamientoId: String,
    fechaSubtitulo: String = "Mier, 15 de Mayo • Tigres Sub-15",
    viewModel: DetalleEntrenamientoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val ejerciciosAcademia by viewModel.ejerciciosAcademia.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    // Control de visibilidad del BottomSheet
    var bloqueSeleccionado by remember { mutableStateOf<BloqueEntrenamiento?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(entrenamientoId) {
        viewModel.cargarDetalle(entrenamientoId)
        viewModel.cargarEjerciciosAcademia()
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Detalle de Sesión",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = fechaSubtitulo,
                            color = TextGray,
                            fontSize = 13.sp
                        )
                    }
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
        when (val state = uiState) {
            is DetalleEntrenamientoUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            is DetalleEntrenamientoUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is DetalleEntrenamientoUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // BANNER DURACIÓN TOTAL
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Duración Total estimada:",
                                        color = PrimaryGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Text(
                                    text = "${state.duracionTotalMin} MIN",
                                    color = PrimaryGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // BLOQUES DE EJERCICIOS
                    BloqueEntrenamiento.values().forEach { bloque ->
                        val ejerciciosBloque = state.ejercicios.filter { it.bloque == bloque }

                        item {
                            SeccionHeader(
                                titulo = bloque.tituloMostrar,
                                onAgregarClick = {
                                    bloqueSeleccionado = bloque
                                    showBottomSheet = true
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (ejerciciosBloque.isEmpty()) {
                            item {
                                Text(
                                    text = "Sin ejercicios asignados",
                                    color = TextGray,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
                                )
                            }
                        } else {
                            // Dentro de DetalleEntrenamientoScreen.kt, en la renderización de las tarjetas:
                            items(ejerciciosBloque) { ejercicio ->
                                TarjetaEjercicioItem(
                                    ejercicio = ejercicio,
                                    onEliminarClick = {
                                        ejercicio.id?.let { ejercicioId ->
                                            viewModel.quitarEjercicio(entrenamientoId, ejercicioId)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        item { Spacer(modifier = Modifier.height(12.dp)) }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // BOTTOM SHEET PARA AGREGAR O CREAR EJERCICIO
        if (showBottomSheet && bloqueSeleccionado != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = CardBackgroundColor
            ) {
                AgregarEjercicioSheetContent(
                    bloque = bloqueSeleccionado!!,
                    ejerciciosExistentes = ejerciciosAcademia.filter { it.bloque == bloqueSeleccionado },
                    isSaving = isSaving,
                    onSeleccionarExistente = { ejercicioId ->
                        viewModel.asociarEjercicioExistente(entrenamientoId, ejercicioId) {
                            showBottomSheet = false
                        }
                    },
                    onCrearNuevo = { titulo, desc, duracion ->
                        viewModel.crearYAsociarNuevoEjercicio(
                            entrenamientoId = entrenamientoId,
                            bloque = bloqueSeleccionado!!,
                            titulo = titulo,
                            descripcion = desc,
                            duracionMin = duracion
                        ) {
                            showBottomSheet = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AgregarEjercicioSheetContent(
    bloque: BloqueEntrenamiento,
    ejerciciosExistentes: List<Ejercicio>,
    isSaving: Boolean,
    onSeleccionarExistente: (ejercicioId: String) -> Unit,
    onCrearNuevo: (titulo: String, descripcion: String, duracionMin: Int) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Lista Existente, 1: Crear Nuevo

    // Campos formulario "Nuevo"
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var duracionText by remember { mutableStateOf("15") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = "Agregar a ${bloque.tituloMostrar}",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pestañas (TabRow)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = PrimaryGreen
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Elegir existente", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Crear nuevo", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // TAB 1: LISTA DE EJERCICIOS EXISTENTES
            if (ejerciciosExistentes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay ejercicios previos en este bloque",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    items(ejerciciosExistentes) { ejercicio ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = InputBackgroundColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    ejercicio.id?.let { id -> onSeleccionarExistente(id) }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ejercicio.titulo,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    ejercicio.descripcion?.let {
                                        Text(
                                            text = it,
                                            color = TextGray,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                                Text(
                                    text = "${ejercicio.duracionMin} min",
                                    color = PrimaryGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 2: FORMULARIO CREAR NUEVO
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título del ejercicio", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción / Indicaciones (Opcional)", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = duracionText,
                    onValueChange = { duracionText = it.filter { char -> char.isDigit() } },
                    label = { Text("Duración (minutos)", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val duracionInt = duracionText.toIntOrNull() ?: 10
                        if (titulo.isNotBlank()) {
                            onCrearNuevo(titulo.trim(), descripcion.trim(), duracionInt)
                        }
                    },
                    enabled = !isSaving && titulo.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Text("Guardar y Asociar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun SeccionHeader(
    titulo: String,
    onAgregarClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            color = PrimaryGreen,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedButton(
            onClick = onAgregarClick,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = CardBackgroundColor,
                contentColor = TextGray
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(
                text = "+ Agregar",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TarjetaEjercicioItem(
    ejercicio: Ejercicio,
    onEliminarClick: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ejercicio.titulo,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${ejercicio.duracionMin} min",
                        color = PrimaryGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                ejercicio.descripcion?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón de eliminar ejercicio de esta sesión
            IconButton(
                onClick = onEliminarClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Quitar ejercicio",
                    tint = Color(0xFFFF5555),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}