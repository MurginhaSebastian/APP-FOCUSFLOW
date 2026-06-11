package com.example.focusflow.data.remote

import android.util.Log
import com.example.focusflow.data.model.RutinaSugerida
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroqService @Inject constructor(
    private val groqApi: GroqApi
) {
    private val gson = Gson()

    suspend fun generarRutina(promptUsuario: String): RutinaSugerida? = withContext(Dispatchers.IO) {
        val systemPrompt = """
            Actúa como un experto en productividad y gestión de tiempo. 
            Genera una rutina de tareas accionables en formato JSON.
            Cada tarea debe tener un título claro y una descripción breve que motive a empezar de inmediato.
            Estructura: {"nombre": "...", "descripcion": "...", "tareas": [{"titulo": "...", "descripcion": "...", "duracionEstimada": "..."}]}
            Responde ÚNICAMENTE el objeto JSON, sin texto adicional, sin bloques de código markdown.
        """.trimIndent()

        val request = ChatRequest(
            model = "llama-3.3-70b-versatile",
            messages = listOf(
                Message(role = "system", content = systemPrompt),
                Message(role = "user", content = "Usuario solicita: $promptUsuario")
            )
        )

        try {
            val response = groqApi.completarChat(
                token = "Bearer ${ApiConfig.GROQ_API_KEY}",
                request = request
            )

            val rawText = response.choices.firstOrNull()?.message?.content ?: ""
            Log.d("GroqService", "Respuesta RAW: $rawText")

            val jsonText = extraerJson(rawText)
            gson.fromJson(jsonText, RutinaSugerida::class.java)
        } catch (e: Exception) {
            Log.e("GroqService", "Error al generar rutina con Groq: ${e.message}", e)
            null
        }
    }

    private fun extraerJson(texto: String): String {
        val inicio = texto.indexOf("{")
        val fin = texto.lastIndexOf("}")
        return if (inicio != -1 && fin != -1) {
            texto.substring(inicio, fin + 1)
        } else {
            texto
        }
    }
}
