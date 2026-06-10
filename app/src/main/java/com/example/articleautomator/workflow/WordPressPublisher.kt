package com.example.articleautomator.workflow

import android.content.Context
import com.example.articleautomator.api.WordPressApiService
import com.example.articleautomator.api.WordPressPostRequest
import com.example.articleautomator.model.BlogPost
import dagger.hilt.android.qualifiers.ApplicationContext
import android.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordPressPublisher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: WordPressApiService
) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    suspend fun publish(post: BlogPost): String {
        var siteUrl = prefs.getString("wp_site_url", "") ?: ""
        val username = prefs.getString("wp_username", "") ?: ""
        val appPassword = prefs.getString("wp_app_password", "") ?: ""

        if (siteUrl.isEmpty() || username.isEmpty() || appPassword.isEmpty()) {
            throw Exception("إعدادات WordPress غير مكتملة")
        }

        if (!siteUrl.endsWith("/")) siteUrl += "/"
        val fullUrl = "${siteUrl}wp-json/wp/v2/posts"

        val authString = "$username:$appPassword"
        val authHeader = "Basic " + Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)

        val request = WordPressPostRequest(
            title = post.title,
            content = post.content
        )

        val response = api.createPost(fullUrl, authHeader, request)
        return response.link
    }
}
