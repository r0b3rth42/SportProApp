package com.example.sportproapp.presentation.desafio

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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.data.model.Academia
import com.example.sportproapp.data.model.DesafioPartido
import com.example.sportproapp.data.model.EstadoDesafio
import com.example.sportproapp.data.remote.supabase.AcademiaRepository
import com.example.sportproapp.data.remote.supabase.DesafioPartidoRepository
import com.example.sportproapp.data.remote.supabase.EquipoRepository
import com.example.sportproapp.data.remote.supabase.SedeRepository
import com.example.sportproapp.data.session.UserSessionManager
import com.example.sportproapp.presentation.auth.*
import com.example.sportproapp.presentation.teams.TeamUiModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class SedeUiModel(val id: String, val nombre: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnviarDesafioScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Listas origen
    var misEquipos by remember { mutableStateOf<List<TeamUiModel>>(emptyList()) }
    var academiasRivales by remember { mutableStateOf<List<Academia>>(emptyList()) }
    var equiposRivales by remember { mutableStateOf<List<TeamUiModel>>(emptyList()) }
    var sedesDisponibles by remember { mutableStateOf<List<SedeUiModel>>(emptyList()) }

    // Campos seleccionados
    var equipoEmisorSeleccionado by remember { mutableStateOf<TeamUiModel?>(null) }
    var academiaReceptoraSeleccionada by remember { mutableStateOf<Academia?>(null) }
    var equipoReceptorSeleccionado by remember { mutableStateOf<TeamUiModel?>(null) }
    var sedeSeleccionada by remember { mutableStateOf<SedeUiModel?>(null) }
    var fechaSeleccionada by remember { mutableStateOf("") }
    var horaSeleccionada by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    // Estados de UI y carga
    var isLoadingDatos by remember { mutableStateOf(true) }
    var isLoadingEquiposRivales by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }

    // Menús desplegables
    var expandedEquipos by remember { mutableStateOf(false) }
    var expandedAcademias by remember { mutableStateOf(false) }
    var expandedEquiposRivales by remember { mutableStateOf(false) }
    var expandedSedes by remember { mutableStateOf(false) }

    val miAcademiaId = UserSessionManager.getAcademiaId() ?: ""

    // 1. Cargar datos iniciales
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val userSession = UserSessionManager.getCurrentUser()

            EquipoRepository.getEquiposByAcademy(userSession?.academiaId.toString()).fold(
                onSuccess = { lista ->
                    misEquipos = lista.map {
                        TeamUiModel(it.id, it.nombre, it.categoria, Color.Green, it.status)
                    }
                },
                onFailure = { }
            )

            AcademiaRepository.getAcademias().fold(
                onSuccess = { lista ->
                    academiasRivales = lista.filter { it.id != miAcademiaId }
                },
                onFailure = { }
            )

            val sedes = SedeRepository.getSedes(userSession?.academiaId.toString()).getOrNull()
            sedesDisponibles = sedes?.map { sede -> SedeUiModel(sede.id, sede.nombre) } ?: emptyList()

            isLoadingDatos = false
        }
    }

    // 2. Carga en cascada: Al seleccionar una academia rival, se obtienen sus equipos específicos
    LaunchedEffect(academiaReceptoraSeleccionada) {
        val idRival = academiaReceptoraSeleccionada?.id
        if (idRival != null) {
            isLoadingEquiposRivales = true
            equipoReceptorSeleccionado = null
            EquipoRepository.getEquiposByAcademy(idRival).fold(
                onSuccess = { lista ->
                    equiposRivales = lista.map {
                        TeamUiModel(it.id, it.nombre, it.categoria, Color.Green, it.status)
                    }
                    isLoadingEquiposRivales = false
                },
                onFailure = {
                    equiposRivales = emptyList()
                    isLoadingEquiposRivales = false
                }
            )
        } else {
            equiposRivales = emptyList()
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
                        text = "Crear Desafío Amistoso",
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
                    text = "Configura los detalles del partido y reta a un equipo rival.",
                    color = TextMuted,
                    fontSize = 14.sp
                )

                // 1. MI EQUIPO EMISOR
                DropdownSelector(
                    label = "Tu Equipo Retador",
                    valueText = equipoEmisorSeleccionado?.let { "${it.name} (${it.category})" } ?: "",
                    placeholder = "Selecciona tu equipo",
                    icon = Icons.Default.Groups,
                    expanded = expandedEquipos,
                    onExpandedChange = { expandedEquipos = it },
                    onDismiss = { expandedEquipos = false }
                ) {
                    misEquipos.forEach { equipo ->
                        DropdownMenuItem(
                            text = { Text("${equipo.name} - ${equipo.category}", color = Color.White) },
                            onClick = {
                                equipoEmisorSeleccionado = equipo
                                expandedEquipos = false
                            }
                        )
                    }
                }

                // 2. ACADEMIA RIVAL
                DropdownSelector(
                    label = "Academia / Club Rival",
                    valueText = academiaReceptoraSeleccionada?.nombre ?: "",
                    placeholder = "Selecciona la academia a retar",
                    icon = Icons.Default.School,
                    expanded = expandedAcademias,
                    onExpandedChange = { expandedAcademias = it },
                    onDismiss = { expandedAcademias = false }
                ) {
                    if (academiasRivales.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay otras academias registradas", color = TextMuted) },
                            onClick = { expandedAcademias = false }
                        )
                    } else {
                        academiasRivales.forEach { academia ->
                            DropdownMenuItem(
                                text = { Text(academia.nombre, color = Color.White) },
                                onClick = {
                                    academiaReceptoraSeleccionada = academia
                                    expandedAcademias = false
                                }
                            )
                        }
                    }
                }

                // 3. EQUIPO RIVAL ESPECÍFICO (Se habilita tras elegir Academia)
                DropdownSelector(
                    label = "Equipo Rival Específico",
                    valueText = when {
                        isLoadingEquiposRivales -> "Cargando equipos..."
                        equipoReceptorSeleccionado != null -> "${equipoReceptorSeleccionado!!.name} (${equipoReceptorSeleccionado!!.category})"
                        else -> ""
                    },
                    placeholder = if (academiaReceptoraSeleccionada == null) "Primero elige una academia" else "Selecciona el equipo rival",
                    icon = Icons.Default.Groups,
                    expanded = expandedEquiposRivales,
                    onExpandedChange = {
                        if (academiaReceptoraSeleccionada != null && !isLoadingEquiposRivales) {
                            expandedEquiposRivales = it
                        }
                    },
                    onDismiss = { expandedEquiposRivales = false }
                ) {
                    if (equiposRivales.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay equipos disponibles en esta academia", color = TextMuted) },
                            onClick = { expandedEquiposRivales = false }
                        )
                    } else {
                        equiposRivales.forEach { equipo ->
                            DropdownMenuItem(
                                text = { Text("${equipo.name} - ${equipo.category}", color = Color.White) },
                                onClick = {
                                    equipoReceptorSeleccionado = equipo
                                    expandedEquiposRivales = false
                                }
                            )
                        }
                    }
                }

                // 4. SEDE PROPUESTA
                DropdownSelector(
                    label = "Sede Propuesta",
                    valueText = sedeSeleccionada?.nombre ?: "",
                    placeholder = "Selecciona el campo deportivo",
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
                        CustomClickableField(
                            label = "Fecha",
                            value = fechaSeleccionada,
                            placeholder = "AAAA-MM-DD",
                            icon = Icons.Default.CalendarToday,
                            onClick = { datePickerDialog.show() }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        CustomClickableField(
                            label = "Hora inicio",
                            value = horaSeleccionada,
                            placeholder = "HH:MM",
                            icon = Icons.Default.Schedule,
                            onClick = { timePickerDialog.show() }
                        )
                    }
                }

                // 6. MENSAJE OPCIONAL
                CustomLabelTextField(
                    label = "Mensaje para el rival (Opcional)",
                    value = mensaje,
                    onValueChange = { mensaje = it },
                    placeholder = "Ej. Partido amistoso categoría Sub-15.",
                    leadingIcon = {
                        Icon(Icons.Default.Message, contentDescription = null, tint = TextMuted)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // BOTÓN ENVIAR DESAFÍO
                Button(
                    onClick = {
                        if (equipoEmisorSeleccionado == null) {
                            Toast.makeText(context, "Selecciona tu equipo", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (academiaReceptoraSeleccionada == null) {
                            Toast.makeText(context, "Selecciona la academia rival", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (equipoReceptorSeleccionado == null) {
                            Toast.makeText(context, "Selecciona el equipo rival específico", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (sedeSeleccionada == null) {
                            Toast.makeText(context, "Selecciona una sede", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (fechaSeleccionada.isBlank() || horaSeleccionada.isBlank()) {
                            Toast.makeText(context, "Ingresa fecha y hora válidas", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSending = true
                        coroutineScope.launch {
                            val nuevoDesafio = DesafioPartido(
                                equipoEmisorId = equipoEmisorSeleccionado!!.id,
                                academiaEmisorId = miAcademiaId,
                                academiaReceptorId = academiaReceptoraSeleccionada!!.id,
                                equipoReceptorId = equipoReceptorSeleccionado!!.id,
                                sedeId = sedeSeleccionada!!.id,
                                fecha = fechaSeleccionada,
                                hora = horaSeleccionada,
                                mensaje = mensaje.ifBlank { null },
                                estado = EstadoDesafio.PENDIENTE
                            )

                            val result = DesafioPartidoRepository.enviarDesafio(nuevoDesafio)
                            isSending = false

                            result.fold(
                                onSuccess = {
                                    Toast.makeText(context, "¡Desafío enviado con éxito!", Toast.LENGTH_LONG).show()
                                    navController.popBackStack()
                                },
                                onFailure = { ex ->
                                    Toast.makeText(context, "Error: ${ex.message}", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    enabled = !isSending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryLime,
                        contentColor = DarkBackground
                    )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = DarkBackground,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Enviar Desafío",
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
private fun DropdownSelector(
    label: String,
    valueText: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
private fun CustomClickableField(
    label: String,
    value: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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