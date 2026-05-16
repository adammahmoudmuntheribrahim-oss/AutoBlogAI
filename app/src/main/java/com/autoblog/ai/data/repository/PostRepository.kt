package com.autoblog.ai.data.repository

import com.autoblog.ai.data.api.*
import com.autoblog.ai.data.model.Article
import com.autoblog.ai.data.model.ArticleItem
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

        val rssUrl = prefs.getRssFeedUrl()
        if (rssUrl.isEmpty()) throw Exception("يجب إدخال رابط RSS أولاً")

        val articlesFromRss = rss.fetchArticles(rssUrl)
        for (articleItem in articlesFromRss) {
            if (localStorage.isDuplicate(articleItem.link)) continue

            val rewrittenContent = gemini.rewriteArticle(articleItem.description.ifEmpty { articleItem.title })
            val imageUrl = imageAi.generateImage(rewrittenContent.title)
            val tags = seo.generateTags(rewrittenContent.content)

            val newArticle = Article(
                title = rewrittenContent.title,
                link = articleItem.link,
                content = rewrittenContent.content,
                imageUrl = imageUrl,
                tags = tags,
                published = false
            )
            localStorage.addArticle(newArticle)

            // النشر في بلوجر
            val postId = blogger.publishPost(newArticle.title, newArticle.content)
            if (postId != null) {
                localStorage.updateArticlePublishedStatus(newArticle.copy(published = true))
            }
        }
    }
}
