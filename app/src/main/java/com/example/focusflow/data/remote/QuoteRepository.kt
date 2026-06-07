package com.example.focusflow.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton

data class ZenQuote(
    val q: String = "",
    val a: String = ""
)

interface ZenQuoteApi {
    @GET("api/random")
    suspend fun getRandomQuote(): List<ZenQuote>
}

@Singleton
class QuoteRepository @Inject constructor() {

    private val api: ZenQuoteApi = Retrofit.Builder()
        .baseUrl("https://zenquotes.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ZenQuoteApi::class.java)

    private val fallbackQuotes = listOf(
        "No necesitas hacerlo perfecto, solo dar el siguiente paso.",
        "Tu forma de pensar es diferente, y eso también puede ser una fortaleza.",
        "Está bien avanzar poco a poco; el progreso sigue siendo progreso.",
        "No eres distraído por falta de capacidad, tu mente simplemente funciona de otra manera.",
        "Descansar también es parte del proceso.",
        "Cada pequeña tarea terminada cuenta.",
        "Tu creatividad y energía tienen mucho valor.",
        "No te compares con el ritmo de los demás.",
        "Puedes reorganizarte las veces que necesites.",
        "Un mal día no define todo tu esfuerzo.",
        "Lo importante no es cuánto tardas, sino que sigues intentándolo.",
        "Tu potencial no se mide por tu nivel de concentración.",
        "Está bien pedir ayuda cuando la necesites.",
        "Las metas grandes también se logran con pasos pequeños.",
        "Tu mente puede ser caótica a veces, pero también muy brillante."
    )

    suspend fun getRandomQuote(): String {
        return try {
            val response = api.getRandomQuote()
            response.firstOrNull()?.q ?: fallbackQuotes.random()
        } catch (e: Exception) {
            fallbackQuotes.random()
        }
    }
}
