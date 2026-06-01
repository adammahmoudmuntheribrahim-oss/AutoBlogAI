package com.example.articleautomator.workflow

import android.content.Context
import com.example.articleautomator.auth.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class ImageGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val tokenManager: TokenManager
) {
    fun generateImageUrl(prompt: String): String {
        val encoded = URLEncoder.encode(prompt, "UTF-8")
        return "https://image.pollinations.ai/prompt/$encoded?width=1200&height=630&nologo=true&enhance=true"
    }

    suspend fun downloadAndUploadToBlogger(prompt: String, blogId: String): String? = withContext(Dispatchers.IO) {
        val imageUrl = generateImageUrl(prompt)
        try {
            val request = Request.Builder().url(imageUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext imageUrl
            
            val bytes = response.body?.bytes() ?: return@withContext imageUrl
            val accessToken = tokenManager.getBloggerAccessToken() ?: return@withContext imageUrl
            
            val mediaRequest = Request.Builder()
                .url("https://www.googleapis.com/upload/blogger/v3/blogs/$blogId/posts/media")
                .header("Authorization", "Bearer $accessToken")
                .post(bytes.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                .build()
                
            val uploadResponse = okHttpClient.newCall(mediaRequest).execute()
            if (uploadResponse.isSuccessful) {
                // Simplified: In production, parse JSON response for 'url'
                imageUrl 
            } else {
                imageUrl
            }
        } catch (e: Exception) {
            imageUrl
        }
    }
}
