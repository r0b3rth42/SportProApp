package com.example.sportproapp.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.data.remote.FirebaseAuthManager
import com.example.sportproapp.presentation.components.LoadingScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onForgotPasswordClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    // Estado para controlar la pantalla de carga
    var isLoading by remember { mutableStateOf(false) }

    // Estados para control de errores visuales
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackBarHostState) }
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

                // --- 1. CABECERA (Logo y Título) ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(PrimaryLime, shape = RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(26.dp),
                            color = Color.Transparent,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(3.dp, DarkBackground)
                        ) {}
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row {
                        Text(
                            text = "Sport",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pro",
                            color = PrimaryLime,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Bienvenido de vuelta a tu academia",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }

                // --- 2. TARJETA FORMULARIO LOGIN ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorderColor, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Iniciar sesión",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )

                        // Campo Correo
                        CustomLoginTextField(
                            label = "Correo electrónico",
                            value = email,
                            onValueChange = {
                                email = it
                                if (emailError) emailError = false
                            },
                            placeholder = "carlos@academia.com",
                            isError = emailError,
                            errorMessage = if (emailError) "Ingresa un correo válido" else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = {
                                Icon(Icons.Outlined.Email, contentDescription = null, tint = TextMuted)
                            }
                        )

                        // Campo Contraseña
                        CustomLoginTextField(
                            label = "Contraseña",
                            value = password,
                            onValueChange = {
                                password = it
                                if (passwordError) passwordError = false
                            },
                            placeholder = "••••••••••••",
                            isPassword = true,
                            isError = passwordError,
                            errorMessage = if (passwordError) "Ingresa tu contraseña" else null,
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextMuted)
                            }
                        )

                        // Opciones adicionales
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrimaryLime,
                                        checkmarkColor = DarkBackground,
                                        uncheckedColor = TextMuted
                                    )
                                )
                                Text(
                                    text = "Recordarme",
                                    color = TextLight,
                                    fontSize = 13.sp
                                )
                            }

                            TextButton(
                                onClick = onForgotPasswordClick,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "¿Olvidaste tu contraseña?",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Botón de Ingreso
                        Button(
                            onClick = {
                                emailError = email.isBlank() || !email.contains("@")
                                passwordError = password.isBlank()

                                if (emailError || passwordError) {
                                    scope.launch {
                                        snackBarHostState.showSnackbar("Por favor completa los campos correctamente")
                                    }
                                } else {
                                    isLoading = true
                                    scope.launch {
                                        val result = FirebaseAuthManager.loginUser(email, password)
                                        isLoading = false
                                        result.fold(
                                            onSuccess = {
                                                navController.navigate("home") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            },
                                            onFailure = { exception ->
                                                Toast.makeText(
                                                    context,
                                                    exception.message ?: "Error al iniciar sesión",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        )
                                    }
                                }
                            },
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
                                text = "Iniciar sesión",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // --- 3. PIE DE PÁGINA (Ir a Registro) ---
                Row(
                    modifier = Modifier.padding(vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿No tienes una cuenta? ",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                    TextButton(
                        onClick = {
                            navController.navigate("register") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Regístrate aquí",
                            color = PrimaryLime,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Muestra la pantalla de espera mientras se procesa la autenticación
            if (isLoading) {
                LoadingScreen(message = "Iniciando sesión...")
            }
        }
    }
}

@Composable
fun CustomLoginTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
            trailingIcon = if (isPassword) {
                {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = TextMuted)
                    }
                }
            } else null,
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InputBackgroundColor,
                unfocusedContainerColor = InputBackgroundColor,
                disabledContainerColor = InputBackgroundColor,
                focusedBorderColor = CardBorderColor,
                unfocusedBorderColor = Color.Transparent,
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