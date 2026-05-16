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

class GeminiService(private val prefs: PreferencesManager) {

    private val client = OkHttpClient()

    suspend fun rewriteArticle(content: String): ArticleContent {
        val apiKey = prefs.getGeminiApiKey()
        if (apiKey.isEmpty()) throw Exception("مفتاح Gemini غير موجود")

        val prompt = """
            أعد كتابة المقال التالي بطريقة احترافية مع تحسين السيو. يجب أن تتضمن الاستجابة عنوانًا جذابًا في السطر الأول، يليه المحتوى المعاد صياغته. لا تقم بتضمين أي مقدمات أو خاتمات إضافية غير العنوان والمحتوى.
            
            $content
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", prompt)
                }))
            }))
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey")
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
                val rewrittenText = parts.getJSONObject(0).getString("text")

                val lines = rewrittenText.split("\n", limit = 2)
                val title = lines.firstOrNull()?.trim() ?: ""
                val rewrittenContent = if (lines.size > 1) lines[1].trim() else ""

                ArticleContent(title, rewrittenContent)
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
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey")
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
