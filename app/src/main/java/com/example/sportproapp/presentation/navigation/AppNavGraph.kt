package com.example.sportproapp.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sportproapp.presentation.auth.LoginScreen
import com.example.sportproapp.presentation.auth.RegisterScreen
import com.example.sportproapp.presentation.home.AdminHomeScreen
import com.example.sportproapp.presentation.home.PlayerHomeScreen
import com.example.sportproapp.presentation.teams.CreateTeamScreen
import com.example.sportproapp.presentation.teams.TeamsListScreen


@Composable
fun AppNavGraph () {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "adminHome"
    ) {
        composable("register") { RegisterScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("home") { PlayerHomeScreen(navController) }
        composable("adminHome") { AdminHomeScreen(navController) }
        composable("teams") { TeamsListScreen(navController) }
        composable("teams-create") { CreateTeamScreen(navController) }
    }
}