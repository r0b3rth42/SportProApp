package com.example.sportproapp.presentation.auth

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class RoleOption(
    val code: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun RoleSelectionScreen(
    navController: NavController
) {
    val roles = listOf(
        RoleOption("DT", "Entrenador", "Planifica y dirige equipos", Icons.Outlined.FitnessCenter),
        RoleOption("JUG", "Jugador", "Monitorea tu rendimiento", Icons.Outlined.MonitorHeart),
        RoleOption("PAD", "Padre de Fam.", "Sigue los pasos de tu hijo", Icons.Outlined.FavoriteBorder),
        RoleOption("ADM", "Administrador", "Gestiona toda la academia", Icons.Outlined.Settings)
    )

    var selectedRoleCode by remember { mutableStateOf("DT") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- ENCABEZADO ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Selecciona tu rol",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Esto personalizará tu experiencia en la plataforma.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }

            // --- LISTA DE OPCIONES DE ROL ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                roles.forEach { role ->
                    val isSelected = selectedRoleCode == role.code
                    RoleCardItem(
                        role = role,
                        isSelected = isSelected,
                        onClick = { selectedRoleCode = role.code }
                    )
                }
            }

            // --- BOTÓN CONTINUAR ---
            Button(
                onClick = {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser == null) {
                        Toast.makeText(context, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        val result = UsuarioRepository.updateUserRole(currentUser.uid, selectedRoleCode)
                        isLoading = false
                        result.fold(
                            onSuccess = {
                                // Navega a la pantalla principal o al login según tu flujo
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            },
                            onFailure = { ex ->
                                Toast.makeText(
                                    context,
                                    ex.message ?: "Error al actualizar el rol",
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
                    text = if (isLoading) "Guardando..." else "Continuar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RoleCardItem(
    role: RoleOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryLime else CardBorderColor
    val backgroundColor = if (isSelected) Color(0xFF142920) else DarkCardBackground

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Ícono del Rol
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (isSelected) PrimaryLime else Color(0xFF162D24),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = role.icon,
                        contentDescription = role.title,
                        tint = if (isSelected) DarkBackground else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Textos
                Column {
                    Text(
                        text = role.title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = role.description,
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }

            // Checkmark si está seleccionado
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = PrimaryLime,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}