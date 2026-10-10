package com.example.sportproapp.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sportproapp.presentation.auth.LoginScreen
import com.example.sportproapp.presentation.auth.RegisterScreen
import com.example.sportproapp.presentation.home.AdminHomeScreen
import com.example.sportproapp.presentation.home.PlayerHomeScreen
import com.example.sportproapp.presentation.jugador.TeamDetailScreen
import com.example.sportproapp.presentation.teams.CreateTeamScreen
import com.example.sportproapp.presentation.teams.TeamsListScreen


@Composable
fun AppNavGraph(
    userRole: String // "ADM", "DT", "JUG", "PAD"
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Definimos las pantallas que DEBEN mostrar la barra inferior
    val bottomBarRoutes = listOf("home", "teams", "trainings", "matches", "community")

    Scaffold(
        bottomBar = {
            // Verificamos si la ruta actual comienza con alguna de las rutas principales
            val showBottomBar = bottomBarRoutes.any { route ->
                currentRoute?.startsWith(route) == true
            }

            if (showBottomBar) {
                AppBottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            // 💡 IMPORTANTE: Aplicar el innerPadding aquí evita que la barra inferior tape el contenido
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- Pantalla Principal (Se adapta según el rol del usuario) ---
            composable("home/{userName}") { backStackEntry ->
                val userName = backStackEntry.arguments?.getString("userName") ?: "Usuario"
                when (userRole) {
                    "ADM" -> AdminHomeScreen(navController, userName = userName)
                    "DT" -> AdminHomeScreen(navController) // Puedes cambiarlo a CoachHomeScreen si lo tienes creado
                    "JUG" -> AdminHomeScreen(navController)
                    "PAD" -> AdminHomeScreen(navController) // Puedes cambiarlo a TutorHomeScreen si lo tienes creado
                    else -> AdminHomeScreen(navController)
                }
            }

            composable("register") { RegisterScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("teams") { TeamsListScreen(navController) }
            composable("teams-create") { CreateTeamScreen(navController) }
            composable("jugador") { TeamDetailScreen(navController) }
        }
    }
}

// Pantalla temporal por si navegas a secciones que aún están en desarrollo
@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, color = androidx.compose.ui.graphics.Color.White)
    }
}