package com.example.sportproapp.data.remote.supabase

import android.util.Log
import com.example.sportproapp.data.model.DetalleMensualidad
import com.example.sportproapp.data.model.Equipo
import com.example.sportproapp.data.model.EstadoMensualidad
import com.example.sportproapp.data.model.JugadorPerfil
import com.example.sportproapp.data.model.Mensualidad
import com.example.sportproapp.data.model.MensualidadJugadorUI
import com.example.sportproapp.data.model.MetodoPago
import com.example.sportproapp.data.model.Notificacion
import com.example.sportproapp.data.model.PagoMensualidad
import com.example.sportproapp.data.model.Usuarios
import com.example.sportproapp.data.model.formatearFecha
import com.example.sportproapp.data.model.formatearSoles
import com.example.sportproapp.data.model.nombrePeriodo
import com.example.sportproapp.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// Datos mínimos que se leen de otras tablas
@Serializable
private data class JugadorFoto(
    @SerialName("id") val id: String,
    @SerialName("fotografia_url") val fotografiaUrl: String? = null
)

@Serializable
private data class EntrenadorEquipo(
    @SerialName("id") val id: String,
    @SerialName("equipo_id") val equipoId: String? = null
)

object MensualidadRepository {
    private const val TAG = "MensualidadRepository"

    // 1. Historial de cuotas de un jugador (4.01), la más reciente primero
    suspend fun getMensualidadesPorJugador(jugadorId: String): Result<List<Mensualidad>> {
        return try {
            val lista = supabase.from("mensualidades")
                .select {
                    filter { eq("jugador_id", jugadorId) }
                }
                .decodeList<Mensualidad>()
            Result.success(lista.sortedByDescending { it.periodo })
        } catch (e: Exception) {
            Log.e(TAG, "Error al listar mensualidades del jugador", e)
            Result.failure(e)
        }
    }

    // 2. Cuotas de un periodo para el administrador (toda la academia) o el DT (solo su equipo) (4.02)
    suspend fun getMensualidadesPorPeriodo(
        periodo: String,
        academiaId: String? = null,
        equipoId: String? = null
    ): Result<List<MensualidadJugadorUI>> {
        return try {
            val cuotas = supabase.from("mensualidades")
                .select {
                    filter {
                        eq("periodo", periodo)
                        academiaId?.let { eq("academia_id", it) }
                        equipoId?.let { eq("equipo_id", it) }
                    }
                }
                .decodeList<Mensualidad>()

            if (cuotas.isEmpty()) return Result.success(emptyList())

            val jugadorIds = cuotas.map { it.jugadorId }.distinct()

            val usuarios = supabase.from("usuarios")
                .select {
                    filter { isIn("id", jugadorIds) }
                }
                .decodeList<Usuarios>()
                .associateBy { it.id }

            val fotos = supabase.from("jugadores")
                .select(Columns.list("id", "fotografia_url")) {
                    filter { isIn("id", jugadorIds) }
                }
                .decodeList<JugadorFoto>()
                .associateBy { it.id }

            val lista = cuotas.map { cuota ->
                MensualidadJugadorUI(
                    mensualidad = cuota,
                    nombreJugador = usuarios[cuota.jugadorId]?.name ?: "Jugador",
                    fotoUrl = fotos[cuota.jugadorId]?.fotografiaUrl
                )
            }.sortedBy { it.nombreJugador }

            Result.success(lista)
        } catch (e: Exception) {
            Log.e(TAG, "Error al listar mensualidades del periodo $periodo", e)
            Result.failure(e)
        }
    }

    // 3. Cuotas pendientes (incluye atrasadas) para la tarjeta del Home del administrador o DT
    suspend fun contarPendientes(academiaId: String? = null, equipoId: String? = null): Result<Int> {
        return try {
            val lista = supabase.from("mensualidades")
                .select(Columns.list("id")) {
                    filter {
                        eq("estado", EstadoMensualidad.PENDIENTE.name)
                        academiaId?.let { eq("academia_id", it) }
                        equipoId?.let { eq("equipo_id", it) }
                    }
                }
                .decodeList<Map<String, String>>()
            Result.success(lista.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. Una cuota con el nombre del jugador, del equipo y su pago si existe (4.03 y 4.04)
    suspend fun getDetalleMensualidad(mensualidadId: String): Result<DetalleMensualidad> {
        return try {
            val cuota = supabase.from("mensualidades")
                .select {
                    filter { eq("id", mensualidadId) }
                }
                .decodeSingle<Mensualidad>()

            val pago = getPagoPorMensualidad(mensualidadId).getOrNull()

            val usuarioIds = listOfNotNull(cuota.jugadorId, pago?.pagadoPor).distinct()
            val usuarios = supabase.from("usuarios")
                .select {
                    filter { isIn("id", usuarioIds) }
                }
                .decodeList<Usuarios>()
                .associateBy { it.id }

            val equipo = supabase.from("equipos")
                .select(Columns.list("id", "nombre", "categoria")) {
                    filter { eq("id", cuota.equipoId) }
                }
                .decodeList<Equipo>()
                .firstOrNull()

            val pagador = pago?.let { usuarios[it.pagadoPor] }

            Result.success(
                DetalleMensualidad(
                    mensualidad = cuota,
                    nombreJugador = usuarios[cuota.jugadorId]?.name ?: "Jugador",
                    nombreEquipo = equipo?.let { "${it.nombre} · ${it.categoria}" } ?: "-",
                    pago = pago,
                    nombrePagador = pagador?.name,
                    rolPagador = pagador?.role
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener la mensualidad $mensualidadId", e)
            Result.failure(e)
        }
    }

    // 5. Crear la cuota del mes para todos los jugadores de un equipo (4.05)
    //    Si un jugador ya tiene cuota en ese periodo no se duplica ni se modifica.
    //    Devuelve cuántas cuotas se crearon.
    suspend fun crearMensualidades(
        academiaId: String,
        equipoId: String,
        periodo: String,
        monto: Double,
        fechaVencimiento: String,
        creadoPor: String?
    ): Result<Int> {
        return try {
            val jugadores = UsuarioRepository.getJugadoresConEquipo(equipoId).getOrThrow()
            if (jugadores.isEmpty()) {
                return Result.failure(Exception("El equipo no tiene jugadores asignados"))
            }

            val cuotas = jugadores.map { jugador ->
                Mensualidad(
                    academiaId = academiaId,
                    equipoId = equipoId,
                    jugadorId = jugador.id,
                    periodo = periodo,
                    monto = monto,
                    fechaVencimiento = fechaVencimiento,
                    creadoPor = creadoPor
                )
            }

            Log.d(TAG, "Creando ${cuotas.size} mensualidades del periodo $periodo para el equipo $equipoId")

            // Solo devuelve las cuotas nuevas: las que ya existían se ignoran
            val creadas = supabase.from("mensualidades")
                .upsert(cuotas) {
                    onConflict = "jugador_id,periodo"
                    ignoreDuplicates = true
                    select()
                }
                .decodeList<Mensualidad>()

            // Aviso al jugador y a su apoderado por cada cuota nueva (2.05).
            // Si falla, las cuotas ya quedaron creadas: solo se registra el error.
            notificarNuevasMensualidades(creadas, jugadores)

            Result.success(creadas.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear mensualidades", e)
            Result.failure(e)
        }
    }

    // 6. Registrar el pago simulado (4.03) en dos pasos:
    //    A. Guardar el pago (el comprobante). UNIQUE(mensualidad_id) impide pagar dos veces.
    //    B. Cambiar la cuota a PAGADO.
    //    Si B falla, al reintentar el pago A detecta que ya existe y vuelve a ejecutar B,
    //    así la cuota no queda como Pendiente teniendo un pago registrado.
    suspend fun registrarPagoSimulado(
        mensualidad: Mensualidad,
        pagadoPor: String,
        metodo: MetodoPago
    ): Result<PagoMensualidad> {
        val mensualidadId = mensualidad.id ?: return Result.failure(Exception("Mensualidad sin id"))
        return try {
            // A. Guardar el pago (o recuperar el que ya existe)
            val pago = try {
                supabase.from("pagos_mensualidad")
                    .insert(
                        PagoMensualidad(
                            mensualidadId = mensualidadId,
                            pagadoPor = pagadoPor,
                            metodo = metodo,
                            monto = mensualidad.monto
                        )
                    ) {
                        select()
                    }
                    .decodeSingle<PagoMensualidad>()
            } catch (e: Exception) {
                if (e.message?.contains("pagos_mensualidad_mensualidad_id_key") == true) {
                    Log.w(TAG, "La mensualidad $mensualidadId ya tenía un pago registrado")
                    getPagoPorMensualidad(mensualidadId).getOrNull() ?: throw e
                } else {
                    throw e
                }
            }

            // B. Marcar la cuota como pagada
            supabase.from("mensualidades").update(
                buildJsonObject { put("estado", EstadoMensualidad.PAGADO.name) }
            ) {
                filter { eq("id", mensualidadId) }
            }

            Log.d(TAG, "Pago simulado registrado: ${pago.numeroOperacion}")
            Result.success(pago)
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar el pago simulado", e)
            Result.failure(Exception(e.message ?: "No se pudo registrar el pago"))
        }
    }

    // Arma un aviso para el jugador y otro para su apoderado (si tiene) por cada cuota nueva
    private suspend fun notificarNuevasMensualidades(
        creadas: List<Mensualidad>,
        jugadores: List<JugadorPerfil>
    ) {
        if (creadas.isEmpty()) return
        try {
            val perfiles = jugadores.associateBy { it.id }

            val nombres = supabase.from("usuarios")
                .select {
                    filter { isIn("id", creadas.map { it.jugadorId }) }
                }
                .decodeList<Usuarios>()
                .associate { it.id to it.name }

            val notificaciones = creadas.flatMap { cuota ->
                val detalle = "${nombrePeriodo(cuota.periodo)} por ${formatearSoles(cuota.monto)}. " +
                        "Vence el ${formatearFecha(cuota.fechaVencimiento)}."

                val avisoJugador = Notificacion(
                    usuarioId = cuota.jugadorId,
                    tipo = "MENSUALIDAD",
                    titulo = "Nueva mensualidad",
                    mensaje = "Se registró tu mensualidad de $detalle",
                    referenciaId = cuota.id
                )

                val avisoApoderado = perfiles[cuota.jugadorId]?.apoderadoId?.let { apoderadoId ->
                    Notificacion(
                        usuarioId = apoderadoId,
                        tipo = "MENSUALIDAD",
                        titulo = "Nueva mensualidad",
                        mensaje = "Mensualidad de ${nombres[cuota.jugadorId] ?: "tu hijo"}: $detalle",
                        referenciaId = cuota.id
                    )
                }

                listOfNotNull(avisoJugador, avisoApoderado)
            }

            NotificacionRepository.crearNotificaciones(notificaciones)
        } catch (e: Exception) {
            Log.e(TAG, "No se pudieron crear los avisos de las mensualidades", e)
        }
    }

    // 7. Pago de una cuota (4.04). Devuelve null si todavía no se pagó.
    suspend fun getPagoPorMensualidad(mensualidadId: String): Result<PagoMensualidad?> {
        return try {
            val pago = supabase.from("pagos_mensualidad")
                .select {
                    filter { eq("mensualidad_id", mensualidadId) }
                }
                .decodeList<PagoMensualidad>()
                .firstOrNull()
            Result.success(pago)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 8. Pagos de varias cuotas, para mostrar el número de operación en el historial (4.01)
    suspend fun getPagosPorMensualidades(mensualidadIds: List<String>): Result<Map<String, PagoMensualidad>> {
        if (mensualidadIds.isEmpty()) return Result.success(emptyMap())
        return try {
            val pagos = supabase.from("pagos_mensualidad")
                .select {
                    filter { isIn("mensualidad_id", mensualidadIds) }
                }
                .decodeList<PagoMensualidad>()
            Result.success(pagos.associateBy { it.mensualidadId })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 9. Equipo asignado a un entrenador (DT), para mostrarle solo las cuotas de su equipo
    suspend fun getEquipoDelEntrenador(entrenadorId: String): Result<String?> {
        return try {
            val entrenador = supabase.from("entrenadores_dt")
                .select(Columns.list("id", "equipo_id")) {
                    filter { eq("id", entrenadorId) }
                }
                .decodeList<EntrenadorEquipo>()
                .firstOrNull()
            Result.success(entrenador?.equipoId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
