package com.example.sportproapp.presentation.partidos

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
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
import androidx.navigation.NavController
import com.example.sportproapp.data.model.FaseCampeonato
import com.example.sportproapp.data.model.Partido
import com.example.sportproapp.data.remote.supabase.DesafioPartidoRepository
import com.example.sportproapp.data.remote.supabase.EquipoRepository
import com.example.sportproapp.data.remote.supabase.SedeRepository
import com.example.sportproapp.data.session.UserSessionManager
import com.example.sportproapp.presentation.auth.*
import com.example.sportproapp.presentation.desafio.SedeUiModel
import com.example.sportproapp.presentation.teams.TeamUiModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPartidoScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Opciones y listas
    var misEquipos by remember { mutableStateOf<List<TeamUiModel>>(emptyList()) }
    var todosLosEquipos by remember { mutableStateOf<List<TeamUiModel>>(emptyList()) }
    var sedesDisponibles by remember { mutableStateOf<List<SedeUiModel>>(emptyList()) }
    var fasesDisponibles by remember { mutableStateOf<List<FaseCampeonato>>(emptyList()) }

    // Campos seleccionados
    var tipoPartidoSeleccionado by remember { mutableStateOf("AMISTOSO") }
    var faseSeleccionada by remember { mutableStateOf<FaseCampeonato?>(null) }
    var equipoLocalSeleccionado by remember { mutableStateOf<TeamUiModel?>(null) }
    var equipoVisitanteSeleccionado by remember { mutableStateOf<TeamUiModel?>(null) }
    var sedeSeleccionada by remember { mutableStateOf<SedeUiModel?>(null) }
    var fechaSeleccionada by remember { mutableStateOf("") }
    var horaSeleccionada by remember { mutableStateOf("") }

    // Estados de UI
    var isLoadingDatos by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Dropdowns
    var expandedLocal by remember { mutableStateOf(false) }
    var expandedVisitante by remember { mutableStateOf(false) }
    var expandedSedes by remember { mutableStateOf(false) }
    var expandedFases by remember { mutableStateOf(false) }

    val userSession = UserSessionManager.getCurrentUser()
    val academiaId = userSession?.academiaId ?: ""

    // 1. Cargar equipos, sedes y fases de Supabase
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // A. Cargar TODOS los equipos registrados en la tabla 'equipos'
            EquipoRepository.getEquipos().fold(
                onSuccess = { lista ->
                    todosLosEquipos = lista.map {
                        TeamUiModel(it.id, it.nombre, it.categoria, Color.Green, it.status)
                    }
                },
                onFailure = { }
            )

            // B. Cargar los equipos de mi academia y las sedes
            if (academiaId.isNotBlank()) {
                EquipoRepository.getEquiposByAcademy(academiaId).fold(
                    onSuccess = { lista ->
                        misEquipos = lista.map {
                            TeamUiModel(it.id, it.nombre, it.categoria, Color.Green, it.status)
                        }
                    },
                    onFailure = { }
                )

                val sedes = SedeRepository.getSedes(academiaId).getOrNull()
                sedesDisponibles = sedes?.map { SedeUiModel(it.id, it.nombre) } ?: emptyList()
            } else {
                misEquipos = todosLosEquipos
            }

            // Fallback si sedes de mi academia están vacías
            if (sedesDisponibles.isEmpty()) {
                val sedesTodas = SedeRepository.getSedes().getOrNull()
                sedesDisponibles = sedesTodas?.map { SedeUiModel(it.id, it.nombre) } ?: emptyList()
            }

            // C. Cargar fases de campeonato
            fasesDisponibles = DesafioPartidoRepository.getFasesCampeonato().getOrDefault(emptyList())

            isLoadingDatos = false
        }
    }

    // Pickers de Fecha y Hora
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            horaSeleccionada = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Programar Nuevo Partido",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { innerPadding ->
        if (isLoadingDatos) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryLime)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Registra y programa un encuentro deportivo para tu academia.",
                    color = TextMuted,
                    fontSize = 14.sp
                )

                // 1. TIPO DE PARTIDO (AMISTOSO / OFICIAL)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Tipo de Partido", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("AMISTOSO", "OFICIAL").forEach { tipo ->
                            val isSelected = tipoPartidoSeleccionado == tipo
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .background(
                                        color = if (isSelected) PrimaryLime else InputBackgroundColor,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryLime else CardBorderColor,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { tipoPartidoSeleccionado = tipo },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tipo,
                                    color = if (isSelected) DarkBackground else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // 1.B FASE DE CAMPEONATO (Solo si el tipo es OFICIAL y hay fases disponibles)
                if (tipoPartidoSeleccionado == "OFICIAL" && fasesDisponibles.isNotEmpty()) {
                    DropdownSelectorField(
                        label = "Fase de Campeonato",
                        valueText = faseSeleccionada?.let { "${it.nombre} (${it.tipoFase})" } ?: "",
                        placeholder = "Selecciona la fase del campeonato",
                        icon = Icons.Default.EmojiEvents,
                        expanded = expandedFases,
                        onExpandedChange = { expandedFases = it },
                        onDismiss = { expandedFases = false }
                    ) {
                        fasesDisponibles.forEach { fase ->
                            DropdownMenuItem(
                                text = { Text("${fase.nombre} - ${fase.tipoFase}", color = Color.White) },
                                onClick = {
                                    faseSeleccionada = fase
                                    expandedFases = false
                                }
                            )
                        }
                    }
                }

                // 2. EQUIPO LOCAL
                val opcionesLocal = if (misEquipos.isNotEmpty()) misEquipos else todosLosEquipos

                DropdownSelectorField(
                    label = "Equipo Local",
                    valueText = equipoLocalSeleccionado?.let { "${it.name} (${it.category})" } ?: "",
                    placeholder = "Selecciona el equipo local",
                    icon = Icons.Default.Groups,
                    expanded = expandedLocal,
                    onExpandedChange = { expandedLocal = it },
                    onDismiss = { expandedLocal = false }
                ) {
                    opcionesLocal.forEach { equipo ->
                        DropdownMenuItem(
                            text = { Text("${equipo.name} - ${equipo.category}", color = Color.White) },
                            onClick = {
                                equipoLocalSeleccionado = equipo
                                expandedLocal = false
                            }
                        )
                    }
                }

                // 3. EQUIPO VISITANTE
                DropdownSelectorField(
                    label = "Equipo Visitante",
                    valueText = equipoVisitanteSeleccionado?.let { "${it.name} (${it.category})" } ?: "",
                    placeholder = "Selecciona el equipo visitante (rival)",
                    icon = Icons.Default.SportsSoccer,
                    expanded = expandedVisitante,
                    onExpandedChange = { expandedVisitante = it },
                    onDismiss = { expandedVisitante = false }
                ) {
                    todosLosEquipos.forEach { equipo ->
                        if (equipo.id != equipoLocalSeleccionado?.id) {
                            DropdownMenuItem(
                                text = { Text("${equipo.name} - ${equipo.category}", color = Color.White) },
                                onClick = {
                                    equipoVisitanteSeleccionado = equipo
                                    expandedVisitante = false
                                }
                            )
                        }
                    }
                }

                // 4. SEDE DEL PARTIDO
                DropdownSelectorField(
                    label = "Sede / Campo Deportivo",
                    valueText = sedeSeleccionada?.nombre ?: "",
                    placeholder = "Selecciona la sede",
                    icon = Icons.Default.Place,
                    expanded = expandedSedes,
                    onExpandedChange = { expandedSedes = it },
                    onDismiss = { expandedSedes = false }
                ) {
                    sedesDisponibles.forEach { sede ->
                        DropdownMenuItem(
                            text = { Text(sede.nombre, color = Color.White) },
                            onClick = {
                                sedeSeleccionada = sede
                                expandedSedes = false
                            }
                        )
                    }
                }

                // 5. FECHA Y HORA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ClickableField(
                            label = "Fecha",
                            value = fechaSeleccionada,
                            placeholder = "AAAA-MM-DD",
                            icon = Icons.Default.CalendarToday,
                            onClick = { datePickerDialog.show() }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        ClickableField(
                            label = "Hora inicio",
                            value = horaSeleccionada,
                            placeholder = "HH:MM",
                            icon = Icons.Default.Schedule,
                            onClick = { timePickerDialog.show() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // BOTÓN CREAR PARTIDO
                Button(
                    onClick = {
                        if (equipoLocalSeleccionado == null) {
                            Toast.makeText(context, "Selecciona el equipo local", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (equipoVisitanteSeleccionado == null) {
                            Toast.makeText(context, "Selecciona el equipo visitante", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (sedeSeleccionada == null) {
                            Toast.makeText(context, "Selecciona la sede del partido", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (fechaSeleccionada.isBlank() || horaSeleccionada.isBlank()) {
                            Toast.makeText(context, "Ingresa fecha y hora válidas", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSaving = true
                        coroutineScope.launch {
                            val nuevoPartido = Partido(
                                tipoPartido = tipoPartidoSeleccionado,
                                faseId = if (tipoPartidoSeleccionado == "OFICIAL") faseSeleccionada?.id else null,
                                sedeId = sedeSeleccionada!!.id,
                                fecha = fechaSeleccionada,
                                hora = horaSeleccionada,
                                estado = "PROGRAMADO"
                            )

                            DesafioPartidoRepository.crearPartidoDirecto(
                                partido = nuevoPartido,
                                equipoLocalId = equipoLocalSeleccionado!!.id,
                                equipoVisitanteId = equipoVisitanteSeleccionado!!.id
                            ).fold(
                                onSuccess = {
                                    Toast.makeText(context, "¡Partido programado con éxito!", Toast.LENGTH_LONG).show()
                                    navController.popBackStack()
                                },
                                onFailure = { ex ->
                                    Toast.makeText(context, "Error al crear partido: ${ex.message}", Toast.LENGTH_LONG).show()
                                    isSaving = false
                                }
                            )
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryLime,
                        contentColor = DarkBackground
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = DarkBackground,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Guardar y Programar Partido",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelectorField(
    label: String,
    valueText: String,
    placeholder: String,
    icon: ImageVector,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange
        ) {
            OutlinedTextField(
                value = valueText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                placeholder = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
                leadingIcon = { Icon(icon, contentDescription = null, tint = TextMuted) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputBackgroundColor,
                    unfocusedContainerColor = InputBackgroundColor,
                    focusedBorderColor = CardBorderColor,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss,
                modifier = Modifier.background(DarkCardBackground)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ClickableField(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(InputBackgroundColor, RoundedCornerShape(12.dp))
                .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = value.ifEmpty { placeholder },
                    color = if (value.isNotEmpty()) Color.White else TextMuted,
                    fontSize = 14.sp
                )
            }
        }
    }
}