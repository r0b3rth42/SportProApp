package com.example.sportproapp.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CorporateFare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.data.model.Academia
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileCompletionScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Estados para el Combo de Academias
    var academiasList by remember { mutableStateOf<List<Academia>>(emptyList()) }
    var selectedAcademia by remember { mutableStateOf<Academia?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isLoadingAcademias by remember { mutableStateOf(true) }

    var cargo by remember { mutableStateOf("") }

    var academiaError by remember { mutableStateOf(false) }
    var cargoError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val currentUser = FirebaseAuth.getInstance().currentUser

    // Cargar la lista de academias desde Supabase al entrar
    LaunchedEffect(Unit) {
        val result = UsuarioRepository.getAcademias()
        academiasList = result.getOrNull() ?: emptyList()
        isLoadingAcademias = false
    }

    Scaffold(
        containerColor = DarkBackground
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
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // --- 1. ENCABEZADO ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF162D24), shape = RoundedCornerShape(20.dp))
                            .border(1.dp, PrimaryLime, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AdminPanelSettings,
                            contentDescription = "Administrador",
                            tint = PrimaryLime,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Datos de la Academia",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Selecciona la academia que administrarás y especifica tu cargo institucional.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // --- 2. FORMULARIO ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Información de Gestión",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // COMBO / DROPDOWN DE ACADEMIAS
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Academia / Club", color = TextLight, fontSize = 13.sp)

                            ExposedDropdownMenuBox(
                                expanded = isDropdownExpanded,
                                onExpandedChange = { if (!isLoadingAcademias) isDropdownExpanded = !isDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedAcademia?.nombre ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = {
                                        Text(
                                            text = if (isLoadingAcademias) "Cargando academias..." else "Selecciona una academia",
                                            color = TextMuted,
                                            fontSize = 14.sp
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.CorporateFare, contentDescription = null, tint = TextMuted)
                                    },
                                    isError = academiaError,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = InputBackgroundColor,
                                        unfocusedContainerColor = InputBackgroundColor,
                                        focusedBorderColor = PrimaryLime,
                                        unfocusedBorderColor = CardBorderColor,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    )
                                )

                                ExposedDropdownMenu(
                                    expanded = isDropdownExpanded,
                                    onDismissRequest = { isDropdownExpanded = false },
                                    modifier = Modifier.background(DarkCardBackground)
                                ) {
                                    if (academiasList.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("No hay academias registradas", color = TextMuted) },
                                            onClick = { isDropdownExpanded = false }
                                        )
                                    } else {
                                        academiasList.forEach { academia ->
                                            DropdownMenuItem(
                                                text = { Text(academia.nombre, color = Color.White) },
                                                onClick = {
                                                    selectedAcademia = academia
                                                    isDropdownExpanded = false
                                                    academiaError = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (academiaError) {
                                Text("Debes seleccionar una academia", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }
                        }

                        // Campo Cargo
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Cargo o Puesto", color = TextLight, fontSize = 13.sp)
                            OutlinedTextField(
                                value = cargo,
                                onValueChange = {
                                    cargo = it
                                    if (cargoError) cargoError = false
                                },
                                placeholder = { Text("Ej. Director General, Administrador", color = TextMuted) },
                                singleLine = true,
                                isError = cargoError,
                                leadingIcon = {
                                    Icon(Icons.Outlined.Badge, contentDescription = null, tint = TextMuted)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = InputBackgroundColor,
                                    unfocusedContainerColor = InputBackgroundColor,
                                    focusedBorderColor = PrimaryLime,
                                    unfocusedBorderColor = CardBorderColor,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                )
                            )
                            if (cargoError) {
                                Text("Ingresa tu cargo en la institución", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // --- 3. BOTÓN GUARDAR ---
                Button(
                    onClick = {
                        academiaError = selectedAcademia == null
                        cargoError = cargo.isBlank()

                        if (academiaError || cargoError) return@Button

                        if (currentUser == null) {
                            Toast.makeText(context, "Sesión inválida. Vuelve a iniciar sesión.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val selectedAcademiaId = selectedAcademia!!.id

                        isLoading = true
                        coroutineScope.launch {
                            val saveResult = UsuarioRepository.saveAdminProfile(
                                idAdmin = currentUser.uid,
                                academiaId = selectedAcademiaId, // Enviamos el ID de la academia seleccionada
                                cargo = cargo
                            )

                            saveResult.fold(
                                onSuccess = {
                                    val userResult = UsuarioRepository.getUser(currentUser.uid).getOrNull()
                                    val userName = userResult?.name?.takeIf { it.isNotBlank() } ?: "Administrador"

                                    isLoading = false
                                    Toast.makeText(context, "Perfil de Administrador guardado", Toast.LENGTH_SHORT).show()

                                    navController.navigate("home/$userName/ADM") {
                                        popUpTo("selected_role") { inclusive = true }
                                    }
                                },
                                onFailure = { ex ->
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        ex.message ?: "Error al guardar perfil",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryLime,
                        contentColor = DarkBackground
                    )
                ) {
                    Text(
                        text = if (isLoading) "Guardando..." else "Guardar Administrador",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}