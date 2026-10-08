package com.example.sportproapp.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sportproapp.presentation.auth.LoginScreen
import com.example.sportproapp.presentation.auth.RegisterScreen
import com.example.sportproapp.presentation.home.HomeScreen


@Composable
fun AppNavGraph () {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "register"
    ) {
        composable("register") { RegisterScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("home") { HomeScreen(navController) }
    }
}