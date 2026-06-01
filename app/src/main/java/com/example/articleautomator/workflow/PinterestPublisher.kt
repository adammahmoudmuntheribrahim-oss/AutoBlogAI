package com.example.articleautomator.workflow

import android.content.Context
import android.util.Base64
import com.example.articleautomator.api.PinterestApiService
import com.example.articleautomator.api.PinRequest
import com.example.articleautomator.api.MediaSource
import com.example.articleautomator.api.PinterestTokenApi
import com.example.articleautomator.auth.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinterestPublisher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: PinterestApiService,
    private val tokenApi: PinterestTokenApi,
    private val tokenManager: TokenManager
) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private suspend fun getValidAccessToken(): String {
        val accessToken = tokenManager.getPinterestAccessToken()
        if (accessToken != null) return accessToken
        
        val refreshToken = tokenManager.getPinterestRefreshToken() ?: throw Exception("Pinterest غير مرتبط")
        val appId = prefs.getString("pinterest_app_id", "") ?: ""
        val appSecret = prefs.getString("pinterest_secret", "") ?: ""
        val authHeader = "Basic " + Base64.encodeToString("$appId:$appSecret".toByteArray(), Base64.NO_WRAP)
        
        val response = tokenApi.refreshToken(authHeader, refreshToken)
        tokenManager.savePinterestTokens(response.access_token, response.refresh_token ?: refreshToken, response.expires_in)
        return response.access_token
    }

    suspend fun createPin(blogUrl: String, imageUrl: String, title: String): String {
        val token = getValidAccessToken()
        val boardId = prefs.getString("board_id", "") ?: ""
        val pin = PinRequest(
            title = title,
            description = "Read the full article on our blog",
            link = blogUrl,
            media_source = MediaSource(url = imageUrl),
            board_id = boardId
        )
        return api.createPin("Bearer $token", pin).id
    }

    suspend fun authenticateWithCode(authCode: String) {
        val appId = prefs.getString("pinterest_app_id", "") ?: ""
        val appSecret = prefs.getString("pinterest_secret", "") ?: ""
        val verifier = tokenManager.getCodeVerifier() ?: throw Exception("Missing Code Verifier")
        val authHeader = "Basic " + Base64.encodeToString("$appId:$appSecret".toByteArray(), Base64.NO_WRAP)
        
        val response = tokenApi.exchangeToken(
            authHeader, 
            authCode, 
            verifier,
            "com.example.articleautomator:/pinterest-oauth"
        )
        tokenManager.savePinterestTokens(response.access_token, response.refresh_token, response.expires_in)
    }

    fun getAuthUrl(): String {
        val appId = prefs.getString("pinterest_app_id", "") ?: ""
        val state = tokenManager.generateState()
        val verifier = tokenManager.generateCodeVerifier()
        val challenge = tokenManager.generateCodeChallenge(verifier)
        
        return "https://www.pinterest.com/oauth/?" +
                "client_id=$appId" +
                "&redirect_uri=com.example.articleautomator:/pinterest-oauth" +
                "&response_type=code" +
                "&scope=boards:read,pins:read,pins:write" +
                "&state=$state" +
                "&code_challenge=$challenge" +
                "&code_challenge_method=S256"
    }
}
