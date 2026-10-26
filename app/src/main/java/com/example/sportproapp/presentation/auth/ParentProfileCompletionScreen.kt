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
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ParentProfileCompletionScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var direccion by remember { mutableStateOf("") }
    var direccionError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val currentUser = FirebaseAuth.getInstance().currentUser

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
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "Dirección",
                            tint = PrimaryLime,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Datos del Apoderado",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingresa tu dirección de residencia para completar tu perfil de tutor.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                            text = "Dirección de Domicilio",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = direccion,
                            onValueChange = {
                                direccion = it
                                if (direccionError) direccionError = false
                            },
                            placeholder = { Text("Ej. Av. Primavera 123, Dpto 402", color = TextMuted) },
                            singleLine = true,
                            isError = direccionError,
                            leadingIcon = {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = TextMuted)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = InputBackgroundColor,
                                unfocusedContainerColor = InputBackgroundColor,
                                focusedBorderColor = PrimaryLime,
                                unfocusedBorderColor = CardBorderColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        if (direccionError) {
                            Text(
                                text = "Por favor, ingresa tu dirección de domicilio.",
                                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // --- 3. BOTÓN GUARDAR ---
                Button(
                    onClick = {
                        if (direccion.isBlank()) {
                            direccionError = true
                            return@Button
                        }

                        if (currentUser == null) {
                            Toast.makeText(context, "Sesión inválida. Vuelve a iniciar sesión.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true
                        coroutineScope.launch {
                            // 1. Guardar la dirección en Supabase
                            val saveResult = UsuarioRepository.saveParentProfile(currentUser.uid, direccion)

                            saveResult.fold(
                                onSuccess = {
                                    // 2. Obtener el nombre del usuario para la ruta del Home
                                    val userResult = UsuarioRepository.getUser(currentUser.uid).getOrNull()
                                    val userName = userResult?.name?.takeIf { it.isNotBlank() } ?: "Apoderado"

                                    isLoading = false
                                    Toast.makeText(context, "Perfil guardado con éxito", Toast.LENGTH_SHORT).show()

                                    // 3. Redirigir al Home del Apoderado
                                    navController.navigate("home/$userName/PAD") {
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
                        text = if (isLoading) "Guardando..." else "Guardar Apoderado",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}