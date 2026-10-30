package com.example.sportproapp.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.example.sportproapp.presentation.auth.AdminProfileCompletionScreen
import com.example.sportproapp.presentation.auth.LinkParentCodeScreen
import com.example.sportproapp.presentation.auth.LoginScreen
import com.example.sportproapp.presentation.auth.ParentProfileCompletionScreen
import com.example.sportproapp.presentation.auth.PlayerProfileCompletionScreen
import com.example.sportproapp.presentation.auth.RegisterScreen
import com.example.sportproapp.presentation.auth.RoleSelectionScreen
import com.example.sportproapp.presentation.entrenamiento.CrearEntrenamientoScreen
import com.example.sportproapp.presentation.entrenamiento.EntrenamientosScreen
import com.example.sportproapp.presentation.home.AdminHomeScreen
import com.example.sportproapp.presentation.home.ParentHomeScreen
import com.example.sportproapp.presentation.home.PlayerHomeScreen
import com.example.sportproapp.presentation.jugador.PlayerProfileScreen
import com.example.sportproapp.presentation.jugador.TeamDetailScreen
import com.example.sportproapp.presentation.teams.CreateTeamScreen
import com.example.sportproapp.presentation.teams.TeamsListScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    startDestination: String = "login",
    initialRole: String? = "JUG"
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf("home", "teams", "entrenamiento", "matches", "community")

    Scaffold(
        bottomBar = {
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
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- Pantalla Principal adaptada dinámicamente por ROL ---
            composable("home/{userName}/{userRole}") { backStackEntry ->
                val userName = backStackEntry.arguments?.getString("userName") ?: "Usuario"
                val role = backStackEntry.arguments?.getString("userRole") ?: initialRole

                when (role) {
                    "ADM" -> AdminHomeScreen(navController, userName = userName)
                    "JUG" -> PlayerHomeScreen(navController, userName = userName, userRole = role)
                    "DT" -> AdminHomeScreen(navController) // Reemplazar con CoachHomeScreen
                    "PAD" -> ParentHomeScreen(navController, userName = userName) // Reemplazar con TutorHomeScreen
                    else -> PlayerHomeScreen(navController, userName = userName, userRole = role)
                }
            }

            composable("register") { RegisterScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("teams") {
                TeamsListScreen(
                    navController = navController,
                    onTeamClick = { team ->
                        // Aquí manejamos la navegación centralizada recibiendo el objeto team
                        val encodedName = java.net.URLEncoder.encode(team.name, "UTF-8")
                        val encodedCategory = java.net.URLEncoder.encode(team.category, "UTF-8")
                        navController.navigate("team-detail/${team.id}/$encodedName/$encodedCategory")
                    }
                )
            }
            composable("teams-create") { CreateTeamScreen(navController) }
            composable(
                route = "team-detail/{teamId}/{teamName}/{category}"
            ) { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                val rawName = backStackEntry.arguments?.getString("teamName") ?: "Equipo"
                val rawCategory = backStackEntry.arguments?.getString("category") ?: "Sin categoría"

                val teamName = java.net.URLDecoder.decode(rawName, "UTF-8")
                val category = java.net.URLDecoder.decode(rawCategory, "UTF-8")

                TeamDetailScreen(
                    navController = navController,
                    teamId = teamId,
                    teamName = teamName,
                    category = category
                )
            }

            composable("player-profile/{playerId}") { backStackEntry ->
                val playerId = backStackEntry.arguments?.getString("playerId") ?: "1"
                PlayerProfileScreen(navController, playerId = playerId)
            }

            composable("entrenamiento") {
                EntrenamientosScreen(
                    navController = navController,
                    onAddClick = { navController.navigate("entrenamiento-create") }
                )
            }

            composable("entrenamiento-create") {
                CrearEntrenamientoScreen(
                    navController = navController,
                    onCrearClick = { navController.popBackStack() }
                )
            }

            composable("selected_role") { RoleSelectionScreen(navController) }
            composable("register-player-profile") { PlayerProfileCompletionScreen(navController) }
            composable("register-parent-profile") { ParentProfileCompletionScreen(navController) }
            composable("link_parent_code") { LinkParentCodeScreen(navController = navController) }
            composable("register-admin-profile") { AdminProfileCompletionScreen(navController = navController) }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, color = androidx.compose.ui.graphics.Color.White)
    }
}