package com.autoblog.ai.data.api

import com.autoblog.ai.data.model.ArticleContent
import com.autoblog.ai.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor(
    private val prefs: PreferencesManager,
    private val client: OkHttpClient
) {

    suspend fun rewriteArticle(content: String): ArticleContent {
        val apiKey = prefs.getGeminiApiKey()
        if (apiKey.isEmpty()) throw Exception("مفتاح Gemini غير موجود")

        val prompt = """
            You are an SEO expert blog writer.
            Rewrite the following article in Arabic:
            - Human-like style
            - SEO optimized
            - No plagiarism
            - Add headings (HTML h2, h3)
            - Add conclusion
            - Arabic friendly
            - Blogger HTML ready
            
            Return the result in JSON format with the following fields:
            - title: A catchy SEO title
            - content: The rewritten article in HTML format
            - metaDescription: A brief SEO meta description
            - keywords: Comma-separated SEO keywords
            - slug: A URL-friendly slug in English
            - faqSchema: A basic FAQ schema in JSON-LD if applicable
            
            Article Content:
            $content
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", prompt)
                }))
            }))
            put("generationConfig", JSONObject().apply {
                put("response_mime_type", "application/json")
            })
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
            .post(RequestBody.create("application/json".toMediaType(), jsonBody.toString()))
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("فشل الاتصال بـ Gemini: ${response.message}")
                val body = response.body?.string() ?: throw IOException("استجابة فارغة من Gemini")
                val json = JSONObject(body)
                val candidates = json.getJSONArray("candidates")
                val firstCandidate = candidates.getJSONObject(0)
                val contentJson = firstCandidate.getJSONObject("content")
                val parts = contentJson.getJSONArray("parts")
                val responseText = parts.getJSONObject(0).getString("text")
                
                val resultJson = JSONObject(responseText)
                
                ArticleContent(
                    title = resultJson.optString("title"),
                    content = resultJson.optString("content"),
                    metaDescription = resultJson.optString("metaDescription"),
                    keywords = resultJson.optString("keywords"),
                    slug = resultJson.optString("slug"),
                    faqSchema = resultJson.optString("faqSchema")
                )
            }
        }
    }

    suspend fun testApiKey(apiKey: String): Boolean {
        return try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", "مرحباً")
                    }))
                }))
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                .post(RequestBody.create("application/json".toMediaType(), jsonBody.toString()))
                .build()
            
            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { it.isSuccessful }
            }
        } catch (e: Exception) {
            false
        }
    }
}
