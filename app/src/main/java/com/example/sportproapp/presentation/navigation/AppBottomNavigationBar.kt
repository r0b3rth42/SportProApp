package com.example.sportproapp.presentation.navigation

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import com.example.sportproapp.presentation.auth.DarkCardBackground
import com.example.sportproapp.presentation.auth.PrimaryLime
import com.example.sportproapp.presentation.auth.TextMuted
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.Period

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val routeKey: String
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppBottomNavigationBar(
    navController: NavController,
    currentRoute: String?
) {
    var userName by remember { mutableStateOf("Usuario") }
    var userRole by remember { mutableStateOf("JUG") }

    // Estado para controlar si el acceso a las demás pestañas está bloqueado
    var isAccountBlocked by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Obtener los datos reales del usuario y verificar vinculación si es jugador
    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            val userResult = UsuarioRepository.getUser(uid).getOrNull()
            userName = userResult?.name?.takeIf { it.isNotBlank() } ?: "Usuario"
            userRole = userResult?.role?.takeIf { it.isNotBlank() } ?: "JUG"

            // Si es jugador, validamos si es menor de edad y si su vinculación está PENDIENTE
            if (userRole == "JUG") {
                val playerProfile = UsuarioRepository.getPlayerProfile(uid).getOrNull()
                if (playerProfile != null) {
                    val esMenor = try {
                        playerProfile.fechaNacimiento?.let { fechaStr ->
                            val fechaNac = LocalDate.parse(fechaStr)
                            val hoy = LocalDate.now()
                            Period.between(fechaNac, hoy).years < 18
                        } ?: false
                    } catch (e: Exception) {
                        false
                    }

                    isAccountBlocked = esMenor && playerProfile.estadoVinculacion == "PENDIENTE"
                }
            }
        }
    }

    val navItems = listOf(
        BottomNavItem("Inicio", Icons.Outlined.Home, "home"),
        BottomNavItem("Equipos", Icons.Outlined.Group, "teams"),
        BottomNavItem("Entrenamiento", Icons.Outlined.Cancel, "entrenamiento"),
        BottomNavItem("Partidos", Icons.Outlined.EmojiEvents, "matches"),
        BottomNavItem("Comunidad", Icons.Outlined.ChatBubbleOutline, "community")
    )

    NavigationBar(
        containerColor = DarkCardBackground,
        tonalElevation = 8.dp
    ) {
        navItems.forEach { item ->
            val targetRoute = if (item.routeKey == "home") {
                "home/$userName/$userRole"
            } else {
                item.routeKey
            }

            val isSelected = if (item.routeKey == "home") {
                currentRoute?.startsWith("home") == true
            } else {
                currentRoute == item.routeKey
            }

            // Habilitado solo si la cuenta no está bloqueada o si la opción es "Inicio"
            val isEnabled = !isAccountBlocked || item.routeKey == "home"

            NavigationBarItem(
                selected = isSelected,
                enabled = true, // Mantenemos true para capturar el click y avisar al usuario
                onClick = {
                    if (isAccountBlocked && item.routeKey != "home") {
                        Toast.makeText(
                            context,
                            "Acceso restringido: Cuenta pendiente de vinculación por apoderado",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (!isSelected) {
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(text = item.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryLime,
                    selectedTextColor = PrimaryLime,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = if (isEnabled) TextMuted else TextMuted.copy(alpha = 0.3f),
                    unselectedTextColor = if (isEnabled) TextMuted else TextMuted.copy(alpha = 0.3f)
                )
            )
        }
    }
}