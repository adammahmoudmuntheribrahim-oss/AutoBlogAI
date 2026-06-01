package com.example.articleautomator.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy
import com.example.articleautomator.api.GoogleTokenApi
import com.example.articleautomator.api.PinterestTokenApi
import kotlinx.coroutines.runBlocking

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val googleTokenApi: Lazy<GoogleTokenApi>,
    private val pinterestTokenApi: Lazy<PinterestTokenApi>
) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Blogger Tokens
    fun saveBloggerTokens(accessToken: String, refreshToken: String?, expiresIn: Long) {
        val expirationTime = System.currentTimeMillis() + (expiresIn * 1000)
        prefs.edit()
            .putString("blogger_access_token", accessToken)
            .putString("blogger_refresh_token", refreshToken)
            .putLong("blogger_expiration", expirationTime)
            .apply()
    }

    fun getBloggerAccessToken(): String? {
        val expiration = prefs.getLong("blogger_expiration", 0)
        val accessToken = prefs.getString("blogger_access_token", null)
        val refreshToken = getBloggerRefreshToken()

        if (accessToken != null && System.currentTimeMillis() < expiration - 60000) {
            return accessToken
        }

        if (refreshToken != null) {
            return refreshBloggerToken(refreshToken)
        }
        
        return null
    }

    private fun refreshBloggerToken(refreshToken: String): String? = runBlocking {
        try {
            val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val clientId = sharedPrefs.getString("blogger_client_id", "") ?: ""
            val clientSecret = sharedPrefs.getString("blogger_secret", "") ?: ""
            
            val response = googleTokenApi.get().refreshToken(clientId, clientSecret, refreshToken)
            saveBloggerTokens(response.access_token, response.refresh_token ?: refreshToken, response.expires_in)
            response.access_token
        } catch (e: Exception) {
            null
        }
    }

    fun getBloggerRefreshToken(): String? = prefs.getString("blogger_refresh_token", null)

    // Pinterest Tokens
    fun savePinterestTokens(accessToken: String, refreshToken: String?, expiresIn: Long) {
        val expirationTime = System.currentTimeMillis() + (expiresIn * 1000)
        prefs.edit()
            .putString("pinterest_access_token", accessToken)
            .putString("pinterest_refresh_token", refreshToken)
            .putLong("pinterest_expiration", expirationTime)
            .apply()
    }

    fun getPinterestAccessToken(): String? {
        val expiration = prefs.getLong("pinterest_expiration", 0)
        val accessToken = prefs.getString("pinterest_access_token", null)
        val refreshToken = getPinterestRefreshToken()

        if (accessToken != null && System.currentTimeMillis() < expiration - 60000) {
            return accessToken
        }

        if (refreshToken != null) {
            return refreshPinterestToken(refreshToken)
        }

        return null
    }

    private fun refreshPinterestToken(refreshToken: String): String? = runBlocking {
        try {
            val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val clientId = sharedPrefs.getString("pinterest_app_id", "") ?: ""
            val clientSecret = sharedPrefs.getString("pinterest_secret", "") ?: ""
            val auth = android.util.Base64.encodeToString("$clientId:$clientSecret".toByteArray(), android.util.Base64.NO_WRAP)
            
            val response = pinterestTokenApi.get().refreshToken("Basic $auth", refreshToken)
            savePinterestTokens(response.access_token, response.refresh_token ?: refreshToken, response.expires_in)
            response.access_token
        } catch (e: Exception) {
            null
        }
    }

    fun getPinterestRefreshToken(): String? = prefs.getString("pinterest_refresh_token", null)

    // PKCE and State helpers
    fun generateState(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        val state = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        prefs.edit().putString("oauth_state", state).apply()
        return state
    }

    fun verifyState(state: String?): Boolean {
        val savedState = prefs.getString("oauth_state", null)
        return savedState != null && savedState == state
    }

    fun generateCodeVerifier(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        val verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        prefs.edit().putString("code_verifier", verifier).apply()
        return verifier
    }

    fun getCodeVerifier(): String? = prefs.getString("code_verifier", null)

    fun generateCodeChallenge(verifier: String): String {
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val messageDigest = java.security.MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}
