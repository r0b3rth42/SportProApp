package com.example.sportproapp.data.moderation

import android.util.Log
import com.example.sportproapp.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

data class ModeracionResult(
    val esAprobado: Boolean,
    val mensajeError: String? = null
)

/**
 * Agente Inteligente de Moderación de Contenido con IA (Google Gemini API / LLM Agent)
 * para la detección de lenguaje ofensivo, groserías, violencia y acoso en comentarios.
 */
object ComentarioModerator {

    private const val TAG = "ComentarioModerator"

    // CONFIGURACIÓN SEGURA DE LA API DE IA (Se lee de forma segura desde .env / BuildConfig)
    private var geminiApiKey: String = BuildConfig.GEMINI_API_KEY

    // Lista de modelos compatibles de Gemini para probar automáticamente en orden
    private val modelosGemini = listOf(
        "gemini-2.5-flash",
        "gemini-2.0-flash",
        "gemini-1.5-flash-latest",
        "gemini-1.5-flash",
        "gemini-1.5-pro"
    )

    private val httpClient = HttpClient(Android) {
        engine {
            connectTimeout = 10_000
            socketTimeout = 10_000
        }
    }

    private val jsonParser = Json { ignoreUnknownKeys = true }

    /**
     * Permite configurar dinámicamente la clave de API de IA si es necesario.
     */
    fun setApiKey(apiKey: String) {
        this.geminiApiKey = apiKey.trim()
    }

    /**
     * Evalúa el comentario utilizando un Agente de IA Generativa (LLM).
     * Si no hay API Key configurada o hay un error de red/404, utiliza el filtro estricto de respaldo.
     */
    suspend fun evaluarComentario(texto: String): ModeracionResult {
        if (texto.isBlank()) return ModeracionResult(true)

        // 1. Si la API Key de Gemini está configurada, consultar al Agente de IA
        if (geminiApiKey.isNotBlank()) {
            val resultadoIA = evaluarConAgenteIA(texto)
            if (resultadoIA != null) {
                return resultadoIA
            }
        }

        // 2. Respaldo (Fallback) local: Análisis de reglas NLP y patrones de groserías
        return evaluarRespaldoLocal(texto)
    }

    /**
     * Consulta la API del Agente de Inteligencia Artificial probando los modelos de Gemini disponibles.
     */
    private suspend fun evaluarConAgenteIA(texto: String): ModeracionResult? {
        val promptSistema = """
            Eres un Agente Inteligente de Moderación de Contenido para una aplicación deportiva infantil y para todo público.
            Analiza el siguiente comentario enviado por un usuario: "$texto"
            
            Instrucciones estrictas:
            - Evalúa si contiene groserías, palabras ofensivas, insultos, violencia, acoso, discriminación o términos inapropiados para niños.
            - Responde ÚNICAMENTE en formato JSON válido con la clave "aprobado": true si el comentario es adecuado y respetuoso, o "aprobado": false si contiene cualquier término o frase ofensiva.
            - Ejemplo de respuesta válida: {"aprobado": true} o {"aprobado": false}
        """.trimIndent()

        val promptJson = Json.encodeToJsonElement(promptSistema).toString()

        val requestBodyJson = """
            {
              "contents": [
                {
                  "parts": [
                    { "text": $promptJson }
                  ]
                }
              ]
            }
        """.trimIndent()

        // Probar los modelos de Gemini disponibles en orden hasta recibir respuesta válida
        for (modelo in modelosGemini) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelo:generateContent?key=$geminiApiKey"
                
                val response = httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBodyJson)
                }

                val responseBody = response.bodyAsText()
                Log.d(TAG, "Proband modelo $modelo - Respuesta: $responseBody")

                val jsonRoot = jsonParser.parseToJsonElement(responseBody).jsonObject

                // Si la respuesta contiene un error (ej. 404 Model Not Found), probar el siguiente modelo
                if (jsonRoot.containsKey("error")) {
                    val errorMsg = jsonRoot["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content ?: ""
                    Log.w(TAG, "Modelo $modelo devolvió error: $errorMsg")
                    continue
                }

                // Extraer el texto JSON generado por la IA
                val candidates = jsonRoot["candidates"]?.jsonArray
                val firstCandidate = candidates?.getOrNull(0)?.jsonObject
                val content = firstCandidate?.get("content")?.jsonObject
                val parts = content?.get("parts")?.jsonArray
                val rawTextResponse = parts?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.content ?: ""

                if (rawTextResponse.isNotBlank()) {
                    val esAprobadoIA = parsearAprobadoDesdeTexto(rawTextResponse)

                    return if (!esAprobadoIA) {
                        Log.w(TAG, "El Agente de IA ($modelo) RECHAZÓ el comentario por contenido ofensivo")
                        ModeracionResult(
                            esAprobado = false,
                            mensajeError = "Comentario tiene palabras o frases ofensivas y no será publicado"
                        )
                    } else {
                        Log.d(TAG, "El Agente de IA ($modelo) APROBÓ el comentario")
                        ModeracionResult(esAprobado = true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error intentando consultar modelo $modelo: ${e.message}")
            }
        }

        // Si ninguno de los endpoints respondió correctamente, retorna null para usar el respaldo
        return null
    }

    /**
     * Extrae el valor booleano "aprobado" de la respuesta devuelta por el modelo de IA.
     */
    private fun parsearAprobadoDesdeTexto(textResponse: String): Boolean {
        return try {
            val jsonLimpio = textResponse.replace("```json", "").replace("```", "").trim()
            val parsedObj = jsonParser.parseToJsonElement(jsonLimpio).jsonObject
            parsedObj["aprobado"]?.jsonPrimitive?.boolean ?: true
        } catch (e: Exception) {
            !textResponse.lowercase(Locale.getDefault()).contains("false")
        }
    }

    // --- RESPALDO LOCAL DE SEGURIDAD (Detección por patrones e insultos) ---
    private val palabrasOfensivas = setOf(
        "mierda", "puta", "puto", "carajo", "pendejo", "pendeja", "maricon", "maricón",
        "concha", "culero", "culera", "chingar", "chingada", "chingado", "verga", "maldito",
        "maldita", "bastardo", "bastarda", "cabron", "cabrón", "coño", "hostia", "joder",
        "imbecil", "imbécil", "estúpido", "estúpida", "tarado", "tarada",
        "idiota", "zonzo", "zonza", "baboso", "babosa", "tonto", "tonta", "perra", "perro",
        "zorra", "malparido", "malparida", "hijo de puta", "conchesumadre", "ctm",
        "lpm", "hdp", "reputa", "asshole", "bitch", "bastard", "fuck", "shit", "dick",
        "crap", "cunt", "motherfucker", "whore", "slut", "idiot", "stupid", "dumb",
        "inutil", "inútil", "incompetente", "maldicion", "maldición", "manco", "asno",
        "basura", "asco", "muérete", "muerete", "lárgate", "largate", "violado", "violada"
    )

    private val patronesOfensivos = listOf(
        Pattern.compile("\\bhijo\\s+de\\s+puta\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bconcha\\s+de\\s+tu\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bvas\\s+a\\s+morir\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bte\\s+voy\\s+a\\s+(matar|golpear|romper)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bno\\s+sirves\\s+para\\s+nada\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\beres\\s+un\\s+(inutil|idiota|estupido|basura)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bve\\s+a\\s+la\\s+mierda\\b", Pattern.CASE_INSENSITIVE)
    )

    private fun evaluarRespaldoLocal(texto: String): ModeracionResult {
        val textoNormalizado = normalizarTexto(texto)

        for (patron in patronesOfensivos) {
            if (patron.matcher(textoNormalizado).find()) {
                return ModeracionResult(
                    esAprobado = false,
                    mensajeError = "Comentario tiene palabras o frases ofensivas y no será publicado"
                )
            }
        }

        val palabras = textoNormalizado.split(Regex("[\\s\\p{Punct}]+"))
        for (palabra in palabras) {
            val palabraLimpia = sustituirLeetspeak(palabra)
            if (palabrasOfensivas.contains(palabraLimpia) || palabrasOfensivas.contains(palabra)) {
                return ModeracionResult(
                    esAprobado = false,
                    mensajeError = "Comentario tiene palabras o frases ofensivas y no será publicado"
                )
            }
        }

        for (palabra in palabras) {
            val palabraLimpia = sustituirLeetspeak(palabra)
            for (ofensiva in palabrasOfensivas) {
                if (ofensiva.length >= 4 && palabraLimpia.contains(ofensiva)) {
                    return ModeracionResult(
                        esAprobado = false,
                        mensajeError = "Comentario tiene palabras o frases ofensivas y no será publicado"
                    )
                }
            }
        }

        return ModeracionResult(esAprobado = true)
    }

    private fun normalizarTexto(texto: String): String {
        var temp = texto.lowercase(Locale.getDefault())
        temp = Normalizer.normalize(temp, Normalizer.Form.NFD)
        temp = temp.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        temp = temp.replace(Regex("(.)\\1{2,}"), "$1$1")
        return temp
    }

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
