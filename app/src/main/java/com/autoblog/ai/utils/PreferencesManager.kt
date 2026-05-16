package com.autoblog.ai.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPreferences = EncryptedSharedPreferences.create(
            "autoblog_secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getGeminiApiKey(): String = sharedPreferences.getString("gemini_api_key", "") ?: ""
    fun setGeminiApiKey(key: String) = sharedPreferences.edit().putString("gemini_api_key", key).apply()

    fun getBloggerClientId(): String = sharedPreferences.getString("blogger_client_id", "") ?: ""
    fun setBloggerClientId(id: String) = sharedPreferences.edit().putString("blogger_client_id", id).apply()

    fun getBloggerClientSecret(): String = sharedPreferences.getString("blogger_client_secret", "") ?: ""
    fun setBloggerClientSecret(secret: String) = sharedPreferences.edit().putString("blogger_client_secret", secret).apply()

    fun getBloggerRefreshToken(): String = sharedPreferences.getString("blogger_refresh_token", "") ?: ""
    fun setBloggerRefreshToken(token: String) = sharedPreferences.edit().putString("blogger_refresh_token", token).apply()

    fun getBloggerBlogId(): String = sharedPreferences.getString("blogger_blog_id", "") ?: ""
    fun setBloggerBlogId(id: String) = sharedPreferences.edit().putString("blogger_blog_id", id).apply()

    fun getPexelsApiKey(): String = sharedPreferences.getString("pexels_api_key", "") ?: ""
    fun setPexelsApiKey(key: String) = sharedPreferences.edit().putString("pexels_api_key", key).apply()

    fun getRssFeedUrl(): String = sharedPreferences.getString("rss_feed_url", "") ?: ""
    fun setRssFeedUrl(url: String) = sharedPreferences.edit().putString("rss_feed_url", url).apply()
    fun setPexelsApiKey(key: String) = sharedPreferences.edit().putString("pexels_api_key", key).apply()

    fun isAllKeysSet(): Boolean {
        return getGeminiApiKey().isNotEmpty() &&
                getBloggerClientId().isNotEmpty() &&
                getBloggerClientSecret().isNotEmpty() &&
                getBloggerRefreshToken().isNotEmpty() &&
                getBloggerBlogId().isNotEmpty() &&
                getPexelsApiKey().isNotEmpty() &&
                getRssFeedUrl().isNotEmpty()
    }
}
