package com.example.sportproapp.presentation.mensualidades

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sportproapp.data.model.EstadoVisualMensualidad
import com.example.sportproapp.data.model.nombrePeriodo
import com.example.sportproapp.presentation.auth.*

// Colores de estado del módulo de mensualidades (Figma 4.01 - 4.05)
val PagadoGreen = Color(0xFF4EFE58)
val PendienteYellow = Color(0xFFFFD028)
val AtrasadoRed = Color(0xFFFF3B30)
val InfoBlue = Color(0xFF5AB8FF)

fun colorEstadoMensualidad(estado: EstadoVisualMensualidad): Color = when (estado) {
    EstadoVisualMensualidad.PAGADO -> PagadoGreen
    EstadoVisualMensualidad.PENDIENTE -> PendienteYellow
    EstadoVisualMensualidad.ATRASADO -> AtrasadoRed
}

// --- CABECERA CON BOTÓN ATRÁS ---
@Composable
fun MensualidadHeader(
    titulo: String,
    subtitulo: String? = null,
    onBack: () -> Unit,
    accion: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .background(DarkCardBackground, RoundedCornerShape(10.dp))
                .border(1.dp, CardBorderColor, RoundedCornerShape(10.dp))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = PrimaryLime,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (subtitulo != null) {
                Text(text = subtitulo, color = TextMuted, fontSize = 12.sp)
            }
        }
        accion()
    }
}

// --- INSIGNIA DE ESTADO: Pagado / Pendiente / Atrasado ---
@Composable
fun EstadoMensualidadBadge(
    estado: EstadoVisualMensualidad,
    textoPagado: String = estado.label
) {
    val color = colorEstadoMensualidad(estado)
    val texto = if (estado == EstadoVisualMensualidad.PAGADO) textoPagado else estado.label
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = texto,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// --- SELECTOR DE MES: ‹ Noviembre 2026 › ---
@Composable
fun SelectorPeriodo(
    periodo: String,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(DarkCardBackground, RoundedCornerShape(12.dp))
            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAnterior, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior", tint = PrimaryLime)
        }
        Text(
            text = nombrePeriodo(periodo),
            color = PrimaryLime,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onSiguiente, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente", tint = PrimaryLime)
        }
    }
}

// --- FILA ETIQUETA / VALOR (resumen de la cuota y comprobante) ---
@Composable
fun FilaDato(etiqueta: String, valor: String, colorValor: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = etiqueta, color = TextMuted, fontSize = 13.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = valor, color = colorValor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// --- AVISO CON ÍCONO (pago simulado, notificaciones) ---
@Composable
fun BannerAviso(icon: ImageVector, texto: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = texto, color = color, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

// --- AVATAR DEL JUGADOR: foto o inicial ---
@Composable
fun AvatarJugador(nombre: String, fotoUrl: String?, size: Int = 44) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(InputBackgroundColor)
            .border(1.dp, CardBorderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!fotoUrl.isNullOrBlank()) {
            AsyncImage(
                model = fotoUrl,
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = nombre.take(1).uppercase(),
                color = PrimaryLime,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MensajeVacioMensualidades(mensaje: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = mensaje, color = TextMuted, fontSize = 14.sp)
    }
}
