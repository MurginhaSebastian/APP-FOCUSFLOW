package com.example.focusflow.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

data class WeatherResponse(
    val main: MainData? = null,
    val weather: List<WeatherData>? = null,
    val name: String? = ""
)

data class MainData(
    val temp: Double = 0.0
)

data class WeatherData(
    val description: String = "",
    val main: String = ""
)

interface WeatherApi {
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appId: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "es"
    ): WeatherResponse
}

data class WeatherInfo(
    val temp: String,
    val description: String,
    val condition: String,
    val suggestedActivity: String
)

@Singleton
class WeatherRepository @Inject constructor() {

    private val api: WeatherApi = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)

    suspend fun getWeather(lat: Double, lon: Double): WeatherInfo? {
        return try {
            val response = api.getWeather(
                lat = lat,
                lon = lon,
                appId = ApiConfig.WEATHER_API_KEY
            )
            val temp = response.main?.temp ?: 0.0
            val description = response.weather?.firstOrNull()?.description ?: ""
            val condition = response.weather?.firstOrNull()?.main ?: ""
            val activity = suggestActivity(condition, temp)

            WeatherInfo(
                temp = String.format("%.0f", temp),
                description = description,
                condition = condition,
                suggestedActivity = activity
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun suggestActivity(condition: String, temp: Double): String {
        return when {
            condition.contains("Clear", ignoreCase = true) -> "Caminar al parque 🌳"
            condition.contains("Clouds", ignoreCase = true) -> "Hacer ejercicio en casa 🏋️"
            condition.contains("Rain", ignoreCase = true) -> "Leer un libro 📖"
            condition.contains("Snow", ignoreCase = true) -> "Ver una película 🎬"
            condition.contains("Thunderstorm", ignoreCase = true) -> "Meditar en casa 🧘"
            condition.contains("Drizzle", ignoreCase = true) -> "Escuchar música 🎵"
            condition.contains("Mist", ignoreCase = true) ||
            condition.contains("Fog", ignoreCase = true) -> "Hacer estiramientos 🤸"
            temp > 30 -> "Ir a la piscina 🏊"
            temp < 10 -> "Tomar un café caliente ☕"
            else -> "Salir a dar un paseo 🚶"
        }
    }
}
