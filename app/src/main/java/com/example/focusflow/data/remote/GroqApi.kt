package com.example.focusflow.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApi {
    @POST("chat/completions")
    suspend fun completarChat(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): ChatResponse

    companion object {
        const val BASE_URL = "https://api.groq.com/openai/v1/"
    }
}
