package com.example.articleautomator.api

import retrofit2.http.*

data class GeminiRequest(val contents: List<Content>, val generationConfig: GenerationConfig? = null)
data class Content(val parts: List<Part>)
data class Part(val text: String)
data class GenerationConfig(val temperature: Double = 0.7, val maxOutputTokens: Int = 1500)
data class GeminiResponse(val candidates: List<Candidate>?)
data class Candidate(val content: Content?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
