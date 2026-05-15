package com.autoblog.ai.utils

import android.content.Context
import com.autoblog.ai.data.model.Article
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalStorage(private val context: Context) {
    private val gson = Gson()
    private val fileName = "articles.json"

    suspend fun saveArticles(articles: List<Article>) {
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, fileName)
            file.writeText(gson.toJson(articles))
        }
    }

    suspend fun loadArticles(): List<Article> {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<Article>>() {}.type
                gson.fromJson<List<Article>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        }
    }

    suspend fun addArticle(article: Article) {
        val current = loadArticles().toMutableList()
        val newId = if (current.isEmpty()) 1 else current.maxOf { it.id } + 1
        current.add(article.copy(id = newId))
        saveArticles(current)
    }

    suspend fun isDuplicate(title: String): Boolean {
        return loadArticles().any { it.title == title }
    }

    suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            File(context.filesDir, fileName).delete()
        }
    }
}
