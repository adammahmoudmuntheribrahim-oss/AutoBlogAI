package com.example.articleautomator.workflow

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.articleautomator.api.Content
import com.example.articleautomator.api.GeminiApiService
import com.example.articleautomator.api.GeminiRequest
import com.example.articleautomator.api.Part
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiKeyManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: Lazy<GeminiApiService>
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_gemini_keys",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private var keys: List<String> = prefs.getStringSet("gemini_keys", emptySet())?.toList() ?: emptyList()
    private var currentIndex = 0

    fun getCurrentKey(): String {
        if (keys.isEmpty()) throw IllegalStateException("لا توجد مفاتيح Gemini")
        return keys[currentIndex % keys.size]
    }

    fun rotateKey() {
        if (keys.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % keys.size
        }
    }

    fun getAllKeys(): List<String> = keys

    fun updateKeys(newKeys: List<String>) {
        keys = newKeys
        prefs.edit().putStringSet("gemini_keys", keys.toSet()).apply()
    }

    suspend fun validateKey(key: String): Boolean {
        return try {
            val request = GeminiRequest(listOf(Content(listOf(Part("hi")))))
            api.get().generateContent(key, request)
            true
        } catch (e: Exception) {
            false
        }
    }
}
