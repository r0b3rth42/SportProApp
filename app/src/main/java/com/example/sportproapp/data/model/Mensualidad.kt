package com.example.sportproapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Serializable
enum class EstadoMensualidad {
    @SerialName("PENDIENTE") PENDIENTE,
    @SerialName("PAGADO") PAGADO
}

@Serializable
enum class MetodoPago(val label: String) {
    @SerialName("YAPE_PLIN") YAPE_PLIN("Yape / Plin"),
    @SerialName("TARJETA") TARJETA("Tarjeta de débito o crédito"),
    @SerialName("TRANSFERENCIA") TRANSFERENCIA("Transferencia bancaria")
}

// Tabla mensualidades: una cuota por jugador y periodo
@Serializable
data class Mensualidad(
    @SerialName("id") val id: String? = null,
    @SerialName("academia_id") val academiaId: String,
    @SerialName("equipo_id") val equipoId: String,
    @SerialName("jugador_id") val jugadorId: String,
    @SerialName("periodo") val periodo: String,                    // "2026-11-01"
    @SerialName("monto") val monto: Double,
    @SerialName("fecha_vencimiento") val fechaVencimiento: String, // "2026-11-10"
    @SerialName("estado") val estado: EstadoMensualidad = EstadoMensualidad.PENDIENTE,
    @SerialName("creado_por") val creadoPor: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// Tabla pagos_mensualidad: pago simulado y comprobante
@Serializable
data class PagoMensualidad(
    @SerialName("id") val id: String? = null,
    @SerialName("mensualidad_id") val mensualidadId: String,
    @SerialName("pagado_por") val pagadoPor: String,
    @SerialName("metodo") val metodo: MetodoPago,
    @SerialName("monto") val monto: Double,
    @SerialName("numero_operacion") val numeroOperacion: String? = null, // lo genera la BD: SIM-100001
    @SerialName("fecha_pago") val fechaPago: String? = null
)

// "Atrasado" no se guarda en la BD: es una cuota pendiente con la fecha de vencimiento pasada
enum class EstadoVisualMensualidad(val label: String) {
    PAGADO("Pagado"),
    PENDIENTE("Pendiente"),
    ATRASADO("Atrasado")
}

fun Mensualidad.estadoVisual(): EstadoVisualMensualidad {
    return when {
        estado == EstadoMensualidad.PAGADO -> EstadoVisualMensualidad.PAGADO
        fechaVencimiento < hoyIso() -> EstadoVisualMensualidad.ATRASADO
        else -> EstadoVisualMensualidad.PENDIENTE
    }
}

// Wrapper de ayuda para la lista del administrador (4.02)
data class MensualidadJugadorUI(
    val mensualidad: Mensualidad,
    val nombreJugador: String,
    val fotoUrl: String? = null
)

// Wrapper de ayuda para pagar (4.03) y ver el comprobante (4.04)
data class DetalleMensualidad(
    val mensualidad: Mensualidad,
    val nombreJugador: String,
    val nombreEquipo: String,
    val pago: PagoMensualidad? = null,
    val nombrePagador: String? = null,
    val rolPagador: String? = null
)

// --- Utilidades de formato ---

private val NOMBRES_MES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Setiembre", "Octubre", "Noviembre", "Diciembre"
)

fun formatearSoles(monto: Double): String = "S/ " + String.format(Locale.US, "%.2f", monto)

// "2026-11-01" -> "Noviembre 2026"
fun nombrePeriodo(periodo: String): String {
    val partes = periodo.split("-")
    if (partes.size < 2) return periodo
    val mes = partes[1].toIntOrNull() ?: return periodo
    return "${NOMBRES_MES[mes - 1]} ${partes[0]}"
}

// "2026-11-10" -> "10/11/2026"
fun formatearFecha(fecha: String): String {
    val partes = fecha.take(10).split("-")
    if (partes.size < 3) return fecha
    return "${partes[2]}/${partes[1]}/${partes[0]}"
}

// "2026-11-10" -> "10 de Noviembre de 2026"
fun formatearFechaLarga(fecha: String): String {
    val partes = fecha.take(10).split("-")
    if (partes.size < 3) return fecha
    val mes = partes[1].toIntOrNull() ?: return fecha
    return "${partes[2].toInt()} de ${NOMBRES_MES[mes - 1]} de ${partes[0]}"
}

// Fecha de hoy en formato "2026-11-07"
fun hoyIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)

// Primer día del mes actual: "2026-11-01"
fun periodoActual(): String = hoyIso().take(8) + "01"

// Suma (o resta) meses a un periodo: ("2026-11-01", 1) -> "2026-12-01"
fun moverPeriodo(periodo: String, meses: Int): String {
    val partes = periodo.split("-")
    val calendar = Calendar.getInstance()
    calendar.set(partes[0].toInt(), partes[1].toInt() - 1, 1)
    calendar.add(Calendar.MONTH, meses)
    return String.format(
        Locale.US, "%04d-%02d-01",
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1
    )
}

// Timestamp de Supabase ("2026-11-07T23:42:10.123+00:00") a hora local: "07/11/2026 · 18:42"
fun formatearFechaHora(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return "-"
    return try {
        val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        entrada.timeZone = TimeZone.getTimeZone("UTC")
        val fecha = entrada.parse(timestamp.take(19)) ?: return timestamp
        SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.US).format(fecha)
    } catch (e: Exception) {
        timestamp
    }
}
