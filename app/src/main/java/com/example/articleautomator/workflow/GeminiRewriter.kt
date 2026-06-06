package com.example.articleautomator.workflow

import com.example.articleautomator.api.GeminiApiService
import com.example.articleautomator.api.GeminiRequest
import com.example.articleautomator.api.Content
import com.example.articleautomator.api.Part
import com.example.articleautomator.api.GenerationConfig
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRewriter @Inject constructor(
    private val api: GeminiApiService,
    private val keyManager: GeminiKeyManager
) {

    suspend fun rewriteWithRetry(
        originalText: String,
        lengthOption: String,
        personalityInstruction: String,
        targetLanguage: String = "English"
    ): Pair<String, String> {
        var lastException: Exception? = null
        val keysCount = keyManager.getAllKeys().size
        
        if (keysCount == 0) throw IllegalStateException("لا توجد مفاتيح Gemini صالحة")

        repeat(keysCount) {
            val key = keyManager.getCurrentKey()
            try {
                return rewriteWithKey(key, originalText, lengthOption, personalityInstruction, targetLanguage)
            } catch (e: HttpException) {
                if (e.code() == 429 || e.code() == 403) {
                    keyManager.rotateKey()
                    lastException = e
                } else {
                    throw e
                }
            }
        }
        throw lastException ?: RuntimeException("All Gemini keys exhausted")
    }

    private suspend fun rewriteWithKey(
        apiKey: String,
        originalText: String,
        lengthOption: String,
        personalityInstruction: String,
        targetLanguage: String
    ): Pair<String, String> {
        val lengthInstruction = when (lengthOption) {
            "قصير" -> "Keep the article concise, around 200-300 words."
            "متوسط" -> "Write a medium-length article, around 500-600 words."
            "طويل" -> "Write a detailed, long-form article, around 1000-1200 words."
            "تلقائي" -> "Match the natural length and depth of the original article."
            else -> "Match the length of the original article."
        }

        val prompt = """
            You are an expert SEO blog writer. Rewrite the following article in **$targetLanguage** with these strict SEO guidelines:
            
            1. **Title**: Create a compelling title (50-60 chars).
            2. **Meta Description**: Write a concise meta description (max 155 characters) starting with "META: ".
            3. **Structure**: Use <h2> and <h3> tags for better readability.
            4. **SEO Optimization**: 
               - Include relevant keywords naturally.
               - Add **Internal Links** placeholders like [LINK: Related Topic] where appropriate.
               - Add **Alt Text** for images by including a comment like <!-- ALT: Descriptive text for image -->.
            5. **Personality**: $personalityInstruction
            6. **Length**: $lengthInstruction
            
            Output format:
            [Article Content in HTML]
            META: [Meta Description]
            
            Original article:
            $originalText
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(listOf(Part(prompt)))),
            generationConfig = GenerationConfig(temperature = 0.7, maxOutputTokens = 2048)
        )
        val response = api.generateContent(apiKey, request)
        val fullResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from Gemini")

        val metaMarker = "META: "
        val metaIndex = fullResponse.lastIndexOf(metaMarker)
        return if (metaIndex != -1) {
            val articleHtml = fullResponse.substring(0, metaIndex).trim()
            val metaDescription = fullResponse.substring(metaIndex + metaMarker.length).trim()
            Pair(articleHtml, metaDescription)
        } else {
            Pair(fullResponse.trim(), "")
        }
    }

    suspend fun generateImagePrompt(title: String, excerpt: String): String {
        val prompt = "Create a detailed, high-quality image prompt for an article titled: $title. The prompt should describe a professional illustration or photograph. Excerpt: ${excerpt.take(150)}"
        val response = rewriteWithRetry(prompt, "قصير", "Write a neutral, descriptive image prompt.", "English")
        return response.first
    }
}
