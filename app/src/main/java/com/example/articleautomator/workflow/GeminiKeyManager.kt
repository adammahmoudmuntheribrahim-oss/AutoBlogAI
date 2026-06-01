package com.example.articleautomator.workflow

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiKeyManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
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
}
