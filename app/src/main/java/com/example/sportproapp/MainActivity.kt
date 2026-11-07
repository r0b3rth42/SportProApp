package com.example.sportproapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.sportproapp.presentation.navigation.AppNavGraph
import com.example.sportproapp.ui.theme.SportProAppTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SportProAppTheme {
                AppNavGraph(
                    startDestination = "login",
                    initialRole = "JUG"
                )
            }
        }
    }
}