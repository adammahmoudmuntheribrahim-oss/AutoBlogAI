package com.autoblog.ai.data.repository

import com.autoblog.ai.data.api.*
import com.autoblog.ai.data.model.Article
import com.autoblog.ai.utils.LocalStorage
import com.autoblog.ai.utils.PreferencesManager

class PostRepository(
    private val localStorage: LocalStorage,
    private val rss: RssRepository,
    private val prefs: PreferencesManager
) {
    private val gemini = GeminiService(prefs)
    private val blogger = BloggerRepository(prefs)
    private val imageAi = AiImageRepository(prefs)
    private val seo = SeoRepository(prefs)

    suspend fun fetchAndProcess() {
        if (!prefs.isAllKeysSet()) throw Exception("يجب إدخال جميع مفاتيح API أولاً")

        val articles = rss.fetchArticles()
        for (article in articles) {
            if (localStorage.isDuplicate(article)) continue

            val rewritten = gemini.rewriteArticle(article)
            val imageUrl = imageAi.generateImage(article)
            val tags = seo.generateTags(article)

            val newArticle = Article(
                title = article,
                content = rewritten,
                imageUrl = imageUrl,
                tags = tags,
                published = false
            )
            localStorage.addArticle(newArticle)

            // النشر في بلوجر
            blogger.publishPost(article, rewritten)
        }
    }
}
