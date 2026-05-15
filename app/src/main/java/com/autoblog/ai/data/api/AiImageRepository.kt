package com.autoblog.ai.data.api

import com.autoblog.ai.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class AiImageRepository(private val prefs: PreferencesManager) {

    private val client = OkHttpClient()

    suspend fun generateImage(prompt: String): String {
        val apiKey = prefs.getPexelsApiKey()
        if (apiKey.isEmpty()) throw Exception("مفتاح Pexels غير موجود")

        val request = Request.Builder()
            .url("https://api.pexels.com/v1/search?query=$prompt&per_page=1")
            .header("Authorization", apiKey)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("فشل الاتصال بـ Pexels")
                val json = JSONObject(response.body?.string() ?: "")
                val photos = json.getJSONArray("photos")
                if (photos.length() > 0) {
                    photos.getJSONObject(0).getJSONObject("src").getString("large")
                } else {
                    "https://via.placeholder.com/400"
                }
            }
        }
    }

    suspend fun testConnection(): Boolean {
        val apiKey = prefs.getPexelsApiKey()
        if (apiKey.isEmpty()) return false
        return try {
            val request = Request.Builder()
                .url("https://api.pexels.com/v1/curated?per_page=1")
                .header("Authorization", apiKey)
                .build()
            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { it.isSuccessful }
            }
        } catch (e: Exception) {
            false
        }
    }
}
