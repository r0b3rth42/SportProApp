package com.example.sportproapp.presentation.jugador

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportproapp.presentation.auth.*

// --- MODELOS DE DATOS ---
data class PlayerProfile(
    val id: String = "1",
    val name: String = "Santiago Méndez",
    val position: String = "Mediocampista",
    val teamName: String = "Tigres Sub-15",
    val age: Int = 14,
    val footedness: String = "Diestro",
    val number: Int = 10,
    val matchesPlayed: Int = 12,
    val goals: Int = 8,
    val assists: Int = 5,
    val birthDate: String = "12/04/2011",
    val document: String = "TI 102938481",
    val dominantFoot: String = "Derecho",
    val secondaryPosition: String = "Extremo Derecho"
)

val EmergencyRed = Color(0xFFFF3B30)

@Composable
fun PlayerProfileScreen(
    navController: NavController,
    playerId: String = "1"
) {
    val player = getPlayerProfileById(playerId)

    // Contenedor principal con fondo oscuro
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- 1. BARRA SUPERIOR ---
            PlayerProfileTopBar(
                title = "Ficha Técnica",
                onBackClick = { navController.popBackStack() },
                onPrivacyClick = { /* Abrir privacidad del menor */ },
                onShareClick = { /* Compartir ficha */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. TARJETA PRINCIPAL DEL JUGADOR ---
            PlayerHeaderCard(
                player = player,
                onStatsClick = { /* Ver estadísticas completas */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- 3. DATOS PERSONALES ---
            InfoSectionCard(
                title = "DATOS PERSONALES",
                rows = listOf(
                    "Fecha de Nacimiento" to player.birthDate,
                    "Documento" to player.document
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- 4. DATOS DEPORTIVOS ---
            InfoSectionCard(
                title = "DATOS DEPORTIVOS",
                rows = listOf(
                    "Pie Hábil" to player.dominantFoot,
                    "Posición Secundaria" to player.secondaryPosition
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 5. BOTÓN CONTACTO DE EMERGENCIA ---
            OutlinedButton(
                onClick = { /* Abrir contacto de emergencia */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, EmergencyRed),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = EmergencyRed.copy(alpha = 0.08f),
                    contentColor = EmergencyRed
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Contacto de Emergencia",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Espacio adicional abajo para evitar que el contenido sea tapado por la barra inferior global
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

// --- COMPONENTE: BARRA SUPERIOR ---
@Composable
fun PlayerProfileTopBar(
    title: String,
    onBackClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
    ) {
        // Botón Atrás
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(36.dp)
                .background(DarkCardBackground, RoundedCornerShape(10.dp))
                .border(1.dp, CardBorderColor, RoundedCornerShape(10.dp))
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Privacidad (escudo)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PrimaryLime.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    .border(1.dp, PrimaryLime, RoundedCornerShape(10.dp))
                    .clickable { onPrivacyClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = "Privacidad",
                    tint = PrimaryLime,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Botón Compartir
            Icon(
                imageVector = Icons.Outlined.IosShare,
                contentDescription = "Compartir",
                tint = PrimaryLime,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onShareClick() }
            )
        }
    }
}

// --- COMPONENTE: TARJETA PRINCIPAL DEL JUGADOR ---
@Composable
fun PlayerHeaderCard(
    player: PlayerProfile,
    onStatsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar con etiqueta de dorsal superpuesta
            Box(contentAlignment = Alignment.BottomCenter) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .border(3.dp, PrimaryLime, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = "DORSAL #${player.number}",
                    color = DarkBackground,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(PrimaryLime, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = player.name,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${player.position} • ${player.teamName}",
                color = PrimaryLime,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${player.age} años • ${player.footedness}",
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Resumen de estadísticas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderColor, RoundedCornerShape(10.dp))
                    .padding(vertical = 12.dp)
            ) {
                StatItem(value = player.matchesPlayed, label = "Partidos J.", modifier = Modifier.weight(1f))
                StatItem(value = player.goals, label = "Goles", modifier = Modifier.weight(1f))
                StatItem(value = player.assists, label = "Asistencias", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onStatsClick() }
            ) {
                Text(
                    text = "Ver estadísticas completas",
                    color = PrimaryLime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = PrimaryLime,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// --- COMPONENTE: ESTADÍSTICA INDIVIDUAL ---
@Composable
fun StatItem(
    value: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

// --- COMPONENTE: TARJETA DE SECCIÓN (ETIQUETA / VALOR) ---
@Composable
fun InfoSectionCard(
    title: String,
    rows: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            rows.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (index < rows.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

// --- DATOS DE EJEMPLO SEGÚN EL JUGADOR SELECCIONADO ---
fun getPlayerProfileById(playerId: String): PlayerProfile {
    return when (playerId) {
        "2" -> PlayerProfile(
            id = "2",
            name = "Mateo López",
            position = "Delantero",
            teamName = "Tigres Sub-15",
            age = 15,
            footedness = "Diestro",
            number = 7,
            matchesPlayed = 11,
            goals = 9,
            assists = 3,
            birthDate = "23/08/2010",
            document = "TI 104583920",
            dominantFoot = "Derecho",
            secondaryPosition = "Extremo Izquierdo"
        )
        "3" -> PlayerProfile(
            id = "3",
            name = "Carlos Ruiz",
            position = "Portero",
            teamName = "Tigres Sub-15",
            age = 14,
            footedness = "Zurdo",
            number = 1,
            matchesPlayed = 10,
            goals = 0,
            assists = 1,
            birthDate = "05/02/2011",
            document = "TI 101276345",
            dominantFoot = "Izquierdo",
            secondaryPosition = "Defensa Central"
        )
        else -> PlayerProfile() // Santiago Méndez
    }
}
