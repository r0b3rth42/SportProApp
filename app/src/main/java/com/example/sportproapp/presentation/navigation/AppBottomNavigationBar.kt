package com.example.sportproapp.presentation.navigation

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.data.remote.supabase.UsuarioRepository
import com.example.sportproapp.presentation.auth.DarkCardBackground
import com.example.sportproapp.presentation.auth.PrimaryLime
import com.example.sportproapp.presentation.auth.TextMuted
import com.google.firebase.auth.FirebaseAuth

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val routeKey: String
)

@Composable
fun AppBottomNavigationBar(
    navController: NavController,
    currentRoute: String?
) {
    // 1. Estados para almacenar el nombre y rol del usuario logueado
    var userName by remember { mutableStateOf("Usuario") }
    var userRole by remember { mutableStateOf("JUG") }

    val currentUser = FirebaseAuth.getInstance().currentUser

    // 2. Obtener los datos reales del usuario desde Supabase
    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            val userResult = UsuarioRepository.getUser(uid).getOrNull()
            userName = userResult?.name?.takeIf { it.isNotBlank() } ?: "Usuario"
            userRole = userResult?.role?.takeIf { it.isNotBlank() } ?: "JUG"
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
            // 3. Determinar la ruta destino según el ítem seleccionado
            val targetRoute = if (item.routeKey == "home") {
                "home/$userName/$userRole"
            } else {
                item.routeKey
            }

            // 4. Validar si la pestaña actual está seleccionada
            val isSelected = if (item.routeKey == "home") {
                currentRoute?.startsWith("home") == true
            } else {
                currentRoute == item.routeKey
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(targetRoute) {
                            // Evita acumular historial al cambiar de pestañas principales
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
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}