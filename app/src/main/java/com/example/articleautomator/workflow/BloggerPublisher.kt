package com.example.articleautomator.workflow

import android.content.Context
import com.example.articleautomator.api.BloggerApiService
import com.example.articleautomator.api.BloggerPost
import com.example.articleautomator.api.GoogleTokenApi
import com.example.articleautomator.auth.TokenManager
import com.example.articleautomator.model.BlogPost
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BloggerPublisher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: BloggerApiService,
    private val tokenApi: GoogleTokenApi,
    private val tokenManager: TokenManager
) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private suspend fun getValidAccessToken(): String {
        val accessToken = tokenManager.getBloggerAccessToken()
        if (accessToken != null) return accessToken
        
        val refreshToken = tokenManager.getBloggerRefreshToken() ?: throw Exception("Blogger غير مرتبط")
        val clientId = prefs.getString("blogger_client_id", "") ?: ""
        val clientSecret = prefs.getString("blogger_secret", "") ?: ""
        
        val response = tokenApi.refreshToken(clientId, clientSecret, refreshToken)
        tokenManager.saveBloggerTokens(response.access_token, response.refresh_token ?: refreshToken, response.expires_in)
        return response.access_token
    }

    suspend fun publish(post: BlogPost): String {
        val token = getValidAccessToken()
        val blogId = prefs.getString("blog_id", "") ?: ""
        
        // Use proper SEO meta description (some themes might need it in the content too)
        val fullContent = if (post.metaDescription.isNotBlank()) {
            "<!-- ${post.metaDescription} -->\n${post.content}"
        } else post.content

        val response = api.createPost(
            "Bearer $token",
            blogId,
            BloggerPost(title = post.title, content = fullContent, labels = post.labels)
        )
        return response.url
    }

    suspend fun authenticateWithCode(authCode: String) {
        val clientId = prefs.getString("blogger_client_id", "") ?: ""
        val clientSecret = prefs.getString("blogger_secret", "") ?: ""
        val verifier = tokenManager.getCodeVerifier() ?: throw Exception("Missing Code Verifier")
        
        val response = tokenApi.exchangeCode(
            clientId = clientId,
            clientSecret = clientSecret,
            code = authCode,
            codeVerifier = verifier,
            redirectUri = "com.example.articleautomator:/oauth2redirect"
        )
        tokenManager.saveBloggerTokens(response.access_token, response.refresh_token, response.expires_in)
    }

    fun getAuthUrl(): String {
        val clientId = prefs.getString("blogger_client_id", "") ?: ""
        val state = tokenManager.generateState()
        val verifier = tokenManager.generateCodeVerifier()
        val challenge = tokenManager.generateCodeChallenge(verifier)
        
        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=$clientId" +
                "&redirect_uri=com.example.articleautomator:/oauth2redirect" +
                "&response_type=code" +
                "&scope=https://www.googleapis.com/auth/blogger" +
                "&state=$state" +
                "&code_challenge=$challenge" +
                "&code_challenge_method=S256"
    }
}
