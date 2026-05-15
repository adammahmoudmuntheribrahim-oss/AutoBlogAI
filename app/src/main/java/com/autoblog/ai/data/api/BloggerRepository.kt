package com.autoblog.ai.data.api

import com.autoblog.ai.utils.PreferencesManager
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.blogger.Blogger
import com.google.api.services.blogger.model.Post
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class BloggerRepository(private val prefs: PreferencesManager) {

    private val applicationName = "AutoBlog-AI/1.0"

    private fun getCredential(): GoogleCredential {
        val clientId = prefs.getBloggerClientId()
        val clientSecret = prefs.getBloggerClientSecret()
        val refreshToken = prefs.getBloggerRefreshToken()

        if (clientId.isEmpty() || clientSecret.isEmpty() || refreshToken.isEmpty()) {
            throw Exception("بيانات OAuth غير مكتملة")
        }

        return GoogleCredential.Builder()
            .setTransport(NetHttpTransport())
            .setJsonFactory(GsonFactory.getDefaultInstance())
            .setClientSecrets(clientId, clientSecret)
            .build()
            .apply {
                this.refreshToken = refreshToken
            }
    }

    private fun getBloggerService(): Blogger {
        val credential = getCredential()
        return Blogger.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
        .setApplicationName(applicationName)
        .build()
    }

    suspend fun publishPost(title: String, content: String): Post = withContext(Dispatchers.IO) {
        val blogId = prefs.getBloggerBlogId()
        if (blogId.isEmpty()) throw Exception("معرف المدونة غير موجود")

        val service = getBloggerService()
        val post = Post().apply {
            setTitle(title)
            setContent(content)
        }

        try {
            service.posts().insert(blogId, post).execute()
        } catch (e: IOException) {
            throw Exception("فشل النشر: ${e.message}", e)
        }
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        val blogId = prefs.getBloggerBlogId()
        if (blogId.isEmpty()) return@withContext false

        try {
            val service = getBloggerService()
            service.blogs().get(blogId).execute()
            true
        } catch (e: Exception) {
            false
        }
    }
}
