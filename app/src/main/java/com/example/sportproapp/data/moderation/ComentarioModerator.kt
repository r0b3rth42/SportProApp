package com.example.sportproapp.data.moderation

import android.util.Log
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

data class ModeracionResult(
    val esAprobado: Boolean,
    val mensajeError: String? = null
)

/**
 * Agente de Moderación de Contenido e Inteligencia para detección de lenguaje ofensivo,
 * groserías, insultos, acoso y frases inapropiadas para todo público e infantil.
 */
object ComentarioModerator {

    private const val TAG = "ComentarioModerator"

    // Lista exhaustiva de raíces y palabras ofensivas, groserías, insultos y acoso en español e inglés
    private val palabrasOfensivas = setOf(
        "mierda", "puta", "puto", "carajo", "pendejo", "pendeja", "maricon", "maricón",
        "concha", "culero", "culera", "chingar", "chingada", "chingado", "verga", "maldito",
        "maldita", "bastardo", "bastarda", "cabron", "cabrón", "coño", "hostia", "joder",
        "imbecil", "imbécil", "estúpido", "estúpida", "tarado", "tarada",
        "idiota", "zonzo", "zonza", "baboso", "babosa", "tonto", "tonta", "perra", "perro",
        "zorra", "malparido", "malparida", "hijo de puta", "hijole", "conchesumadre", "ctm",
        "lpm", "hdp", "reputa", "asshole", "bitch", "bastard", "fuck", "shit", "dick",
        "crap", "cunt", "motherfucker", "whore", "slut", "idiot", "stupid", "dumb",
        "inutil", "inútil", "incompetente", "maldicion", "maldición", "manco", "asno",
        "basura", "asco", "muérete", "muerete", "lárgate", "largate", "violado", "violada"
    )

    // Frases ofensivas compuestas y patrones de acoso
    private val patronesOfensivos = listOf(
        Pattern.compile("\\bhijo\\s+de\\s+puta\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bconcha\\s+de\\s+tu\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bvas\\s+a\\s+morir\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bte\\s+voy\\s+a\\s+(matar|golpear|romper)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bno\\s+sirves\\s+para\\s+nada\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\beres\\s+un\\s+(inutil|idiota|estupido|basura)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bve\\s+a\\s+la\\s+mierda\\b", Pattern.CASE_INSENSITIVE)
    )

    /**
     * Evalúa el comentario de forma estricta para garantizar que sea apto para niños y todo público.
     */
    suspend fun evaluarComentario(texto: String): ModeracionResult {
        if (texto.isBlank()) return ModeracionResult(true)

        val textoNormalizado = normalizarTexto(texto)

        // 1. Validar patrones de frases ofensivas compuestas
        for (patron in patronesOfensivos) {
            if (patron.matcher(textoNormalizado).find()) {
                Log.w(TAG, "Comentario bloqueado por patrón ofensivo: $texto")
                return ModeracionResult(
                    esAprobado = false,
                    mensajeError = "Comentario tiene palabras o frases ofensivas y no será publicado"
                )
            }
        }

        // 2. Tokenizar palabras y revisar coincidencias exactas o variaciones leetspeak
        val palabras = textoNormalizado.split(Regex("[\\s\\p{Punct}]+"))
        for (palabra in palabras) {
            val palabraLimpia = sustituirLeetspeak(palabra)
            if (palabrasOfensivas.contains(palabraLimpia) || palabrasOfensivas.contains(palabra)) {
                Log.w(TAG, "Comentario bloqueado por palabra ofensiva '$palabra': $texto")
                return ModeracionResult(
                    esAprobado = false,
                    mensajeError = "Comentario tiene palabras o frases ofensivas y no será publicado"
                )
            }
        }

        // 3. Evaluar sub-cadenas peligrosas dentro de las palabras
        for (palabra in palabras) {
            val palabraLimpia = sustituirLeetspeak(palabra)
            for (ofensiva in palabrasOfensivas) {
                if (ofensiva.length >= 4 && palabraLimpia.contains(ofensiva)) {
                    Log.w(TAG, "Comentario bloqueado por coincidencia en substring '$ofensiva': $texto")
                    return ModeracionResult(
                        esAprobado = false,
                        mensajeError = "Comentario tiene palabras o frases ofensivas y no será publicado"
                    )
                }
            }
        }

        Log.d(TAG, "Comentario aprobado correctamente: $texto")
        return ModeracionResult(esAprobado = true)
    }

    /**
     * Elimina acentos, pasa a minúsculas y remueve caracteres repetidos de más.
     */
    private fun normalizarTexto(texto: String): String {
        var temp = texto.lowercase(Locale.getDefault())
        temp = Normalizer.normalize(temp, Normalizer.Form.NFD)
        temp = temp.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        // Reducir letras repetidas más de 2 veces (ejemplo: "mdeeeerrrdda" -> "mderrda")
        temp = temp.replace(Regex("(.)\\1{2,}"), "$1$1")
        return temp
    }

    /**
     * Sustituye variaciones tipo Leetspeak (ejemplo: 'p3nd3j0' -> 'pendejo', '$' -> 's', '@' -> 'a')
     */
    private fun sustituirLeetspeak(palabra: String): String {
        return palabra
            .replace('@', 'a')
            .replace('4', 'a')
            .replace('3', 'e')
            .replace('1', 'i')
            .replace('!', 'i')
            .replace('0', 'o')
            .replace('$', 's')
            .replace('5', 's')
            .replace('7', 't')
    }
}
