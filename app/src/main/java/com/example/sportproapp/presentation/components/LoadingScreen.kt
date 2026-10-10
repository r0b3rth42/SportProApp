package com.example.sportproapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportproapp.presentation.auth.DarkBackground
import com.example.sportproapp.presentation.auth.PrimaryLime
import com.example.sportproapp.presentation.auth.TextMuted

@Composable
fun LoadingScreen(
    message: String = "CARGANDO..."
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // --- 1. ÍCONO DE LA APLICACIÓN ---
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(PrimaryLime, shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Figura hexagonal interior que representa la pelota / red
                Surface(
                    modifier = Modifier.size(32.dp),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(3.dp, DarkBackground)
                ) {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. TÍTULO BRANDING (SportPro) ---
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sport",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pro",
                    color = PrimaryLime,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- 3. SUBTÍTULO DESCRIPTIVO ---
            Text(
                text = "Gestión integral de equipos y academias",
                color = TextMuted,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(56.dp))

            // --- 4. INDICADOR DE CARGA (SPINNER) ---
            CircularProgressIndicator(
                color = PrimaryLime,
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- 5. TEXTO DE ESTADO ---
            Text(
                text = message.uppercase(),
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }
    }
}