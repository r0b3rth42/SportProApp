package com.example.sportproapp.presentation.auth

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ContactPhone
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sportproapp.data.model.JugadorPerfil
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileCompletionScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- ESTADOS DEL FORMULARIO ---
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var birthDate by remember { mutableStateOf("") }
    var selectedPosition by remember { mutableStateOf("Delantero Centro") }
    var heightCm by remember { mutableStateOf("") }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactPhone by remember { mutableStateOf("") }

    var expandedDropdown by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Selector de Galería Nativo de Android
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val posicionesDisponibles = listOf(
        "Guardameta",
        "Defensa Central",
        "Lateral Izquierdo",
        "Lateral Derecho",
        "Mediocampista Defensivo",
        "Mediocampista Ofensivo",
        "Extremo Izquierdo",
        "Extremo Derecho",
        "Delantero Centro"
    )

    // Configuración del DatePicker Nativo
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedMonth = String.format("%02d", month + 1)
            val formattedDay = String.format("%02d", dayOfMonth)
            birthDate = "$year-$formattedMonth-$formattedDay"
        },
        calendar.get(Calendar.YEAR) - 14, // Año sugerido por defecto (14 años)
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        containerColor = DarkBackground
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
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // --- 1. ENCABEZADO ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "Ficha del Jugador",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Completa tus datos deportivos y de contacto",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }

                // --- 2. TARJETA FORMULARIO JUGADOR ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorderColor, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // --- COMPONENTE DE SELECCIÓN DE FOTO DE PERFIL ---
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable { photoPickerLauncher.launch("image/*") }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(InputBackgroundColor)
                                    .border(2.dp, PrimaryLime, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedImageUri != null) {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Foto de perfil seleccionada",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = "Foto por defecto",
                                        tint = TextMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }

                            // Badge Ícono de Cámara
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryLime)
                                    .border(2.dp, DarkCardBackground, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddAPhoto,
                                    contentDescription = "Cambiar foto",
                                    tint = DarkBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = if (selectedImageUri == null) "Toca para agregar foto" else "Foto seleccionada",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Datos Físicos y Deporte",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // --- FECHA DE NACIMIENTO ---
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Fecha de Nacimiento",
                                    color = TextLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Box(modifier = Modifier.clickable { datePickerDialog.show() }) {
                                    OutlinedTextField(
                                        value = birthDate,
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        placeholder = { Text("AAAA-MM-DD", color = TextMuted, fontSize = 14.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = TextMuted)
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledContainerColor = InputBackgroundColor,
                                            disabledBorderColor = CardBorderColor,
                                            disabledTextColor = Color.White,
                                            disabledPlaceholderColor = TextMuted
                                        )
                                    )
                                }
                            }

                            // --- POSICIÓN HABITUAL ---
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Posición Habitual",
                                    color = TextLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                ExposedDropdownMenuBox(
                                    expanded = expandedDropdown,
                                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                                ) {
                                    OutlinedTextField(
                                        value = selectedPosition,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(12.dp),
                                        leadingIcon = {
                                            Icon(Icons.Outlined.SportsSoccer, contentDescription = null, tint = TextMuted)
                                        },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                                        },
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
                                        expanded = expandedDropdown,
                                        onDismissRequest = { expandedDropdown = false },
                                        modifier = Modifier.border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                                    ) {
                                        posicionesDisponibles.forEach { posicion ->
                                            DropdownMenuItem(
                                                text = { Text(text = posicion, color = Color.White) },
                                                onClick = {
                                                    selectedPosition = posicion
                                                    expandedDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // --- ALTURA (EN CENTÍMETROS) ---
                            CustomLabelTextField(
                                label = "Altura (cm)",
                                value = heightCm,
                                onValueChange = { heightCm = it },
                                placeholder = "170",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = {
                                    Icon(Icons.Outlined.Height, contentDescription = null, tint = TextMuted)
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Contacto de Emergencia",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // --- NOMBRE CONTACTO DE EMERGENCIA ---
                            CustomLabelTextField(
                                label = "Nombre del Contacto",
                                value = emergencyContactName,
                                onValueChange = { emergencyContactName = it },
                                placeholder = "María Ramos (Mamá)",
                                leadingIcon = {
                                    Icon(Icons.Outlined.Person, contentDescription = null, tint = TextMuted)
                                }
                            )

                            // --- TELÉFONO DE EMERGENCIA ---
                            CustomLabelTextField(
                                label = "Teléfono de Emergencia",
                                value = emergencyContactPhone,
                                onValueChange = { emergencyContactPhone = it },
                                placeholder = "987654321",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                leadingIcon = {
                                    Icon(Icons.Outlined.ContactPhone, contentDescription = null, tint = TextMuted)
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // --- BOTÓN GUARDAR Y FINALIZAR ---
                            Button(
                                onClick = {
                                    val currentUser = FirebaseAuth.getInstance().currentUser
                                    if (currentUser == null) {
                                        Toast.makeText(context, "Sesión no válida", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    if (birthDate.isBlank() || heightCm.isBlank() || emergencyContactName.isBlank() || emergencyContactPhone.isBlank()) {
                                        Toast.makeText(context, "Por favor completa todos los campos requeridos", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    isLoading = true
                                    scope.launch {
                                        val perfil = JugadorPerfil(
                                            id = currentUser.uid,
                                            fechaNacimiento = birthDate,
                                            posicion = selectedPosition,
                                            alturaCm = heightCm.toDoubleOrNull() ?: 0.0,
                                            fotografiaUrl = selectedImageUri?.toString(),
                                            contactoEmergenciaNombre = emergencyContactName,
                                            contactoEmergenciaTelefono = emergencyContactPhone
                                        )

                                        val result = UsuarioRepository.savePlayerProfile(perfil)
                                        isLoading = false

                                        result.fold(
                                            onSuccess = {
                                                Toast.makeText(context, "¡Perfil guardado correctamente!", Toast.LENGTH_SHORT).show()
                                                val userName = currentUser.displayName ?: "Jugador"
                                                navController.navigate("home/$userName") {
                                                    popUpTo("select_role") { inclusive = true }
                                                }
                                            },
                                            onFailure = { ex ->
                                                Toast.makeText(context, ex.message ?: "Error al guardar el perfil", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryLime,
                                    contentColor = DarkBackground
                                )
                            ) {
                                Text(
                                    text = if (isLoading) "Guardando..." else "Completar Registro",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}