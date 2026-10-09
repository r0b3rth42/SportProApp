package com.example.sportproapp.presentation.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.data.remote.FirebaseAuthManager
import com.example.sportproapp.presentation.auth.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamScreen(
    navController: NavController,
    onSaveTeamClick: (teamName: String, category: String, coachName: String, color: Color, hasSubdivision: Boolean) -> Unit = { _, _, _, _, _ -> }
) {
    // Estados del formulario
    var teamName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Sub-15") }
    var coachName by remember { mutableStateOf("Carlos Ortiz") }
    var hasSubdivision by remember { mutableStateOf(true) }

    // Colores disponibles para la franja distintiva
    val availableColors = listOf(
        Color(0xFFFF2A6D), // Rosa / Magenta
        PrimaryLime,        // Verde Lima Neón
        Color(0xFF007AFF), // Azul Neón
        Color(0xFFFF9500), // Naranja
        Color(0xFFAF52DE)  // Púrpura
    )
    var selectedColor by remember { mutableStateOf(availableColors[0]) }

    // Errores visuales
    var teamNameError by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crear Nuevo Equipo",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                    // --- 1. SELECCIÓN DE COLOR DE FRANJA DISTINTIVA ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Color distintivo del equipo",
                                color = TextLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                availableColors.forEach { color ->
                                    val isSelected = selectedColor == color
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 3.dp else 0.dp,
                                                color = if (isSelected) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedColor = color },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = "Seleccionado",
                                                tint = if (color == PrimaryLime) DarkBackground else Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- 2. FORMULARIO DE DATOS ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Detalles del equipo",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Nombre del equipo
                            CustomTeamTextField(
                                label = "Nombre del equipo",
                                value = teamName,
                                onValueChange = {
                                    teamName = it
                                    if (teamNameError) teamNameError = false
                                },
                                placeholder = "Ej. Tigres Academia",
                                isError = teamNameError,
                                errorMessage = if (teamNameError) "Ingresa el nombre del equipo" else null,
                                leadingIcon = {
                                    Icon(Icons.Outlined.Groups, contentDescription = null, tint = TextMuted)
                                }
                            )

                            // Categoría / División
                            CustomTeamTextField(
                                label = "Categoría",
                                value = category,
                                onValueChange = { category = it },
                                placeholder = "Ej. Sub-13, Sub-15, Sub-17",
                                leadingIcon = {
                                    Icon(Icons.Outlined.Category, contentDescription = null, tint = TextMuted)
                                }
                            )

                            // Entrenador asignado
                            CustomTeamTextField(
                                label = "Entrenador / DT a cargo",
                                value = coachName,
                                onValueChange = { coachName = it },
                                placeholder = "Nombre del entrenador",
                                leadingIcon = {
                                    Icon(Icons.Outlined.Person, contentDescription = null, tint = TextMuted)
                                }
                            )

                            // Checkbox: Sub-división
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { hasSubdivision = !hasSubdivision },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = hasSubdivision,
                                    onCheckedChange = { hasSubdivision = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrimaryLime,
                                        checkmarkColor = DarkBackground,
                                        uncheckedColor = TextMuted
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = "Habilitar etiqueta (+ sub-división)",
                                        color = TextLight,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Permite agrupar jugadores en planteles secundarios",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 3. BOTÓN DE GUARDADO ---
                Button(
                    onClick = {
                        if (teamName.isBlank()) {
                            teamNameError = true
                            scope.launch {
                                snackbarHostState.showSnackbar("Por favor ingresa un nombre para el equipo")
                            }
                        } else {
                            teamNameError = false
                            //FirebaseAuthManager.registerTeam()
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryLime,
                        contentColor = DarkBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar equipo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- COMPONENTE DE REUTILIZACIÓN DE CAMPOS ---
@Composable
fun CustomTeamTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = TextLight,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = isError,
            leadingIcon = leadingIcon,
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InputBackgroundColor,
                unfocusedContainerColor = InputBackgroundColor,
                disabledContainerColor = InputBackgroundColor,
                focusedBorderColor = PrimaryLime,
                unfocusedBorderColor = CardBorderColor,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}