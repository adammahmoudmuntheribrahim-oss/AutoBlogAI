package com.autoblog.ai.data.api

import com.autoblog.ai.utils.PreferencesManager

class SeoRepository(private val prefs: PreferencesManager) {
    private val gemini = GeminiService(prefs)

    suspend fun generateTags(title: String): String {
        return try {
            val prompt = "مناسبة للمقال التالي بالعربية SEO اقترح 5 وسوم: $title"
            val response = gemini.rewriteArticle(prompt)
            response.split(",").take(5).joinToString(", ") { it.trim() }
        } catch (e: Exception) {
            "أندرويد، بلوجر، تقنية، أخبار، AI"
        }
    }
}
