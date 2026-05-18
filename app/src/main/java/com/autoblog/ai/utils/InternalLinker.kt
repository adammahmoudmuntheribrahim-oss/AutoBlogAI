package com.autoblog.ai.utils

import com.autoblog.ai.data.db.ArticleDao
import com.autoblog.ai.data.model.Article
import com.autoblog.ai.data.model.PostStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InternalLinker @Inject constructor(private val articleDao: ArticleDao) {

    suspend fun addInternalLinks(content: String): String {
        val publishedArticles = articleDao.getArticlesByStatus(PostStatus.PUBLISHED)
        if (publishedArticles.isEmpty()) return content

        var linkedContent = content
        // Simple keyword-based linking for demonstration
        // A more advanced version would use AI to find the best context for links
        for (article in publishedArticles.take(5)) {
            val keywords = article.keywords?.split(",") ?: emptyList()
            for (keyword in keywords) {
                val trimmedKeyword = keyword.trim()
                if (trimmedKeyword.length > 3 && linkedContent.contains(trimmedKeyword)) {
                    // Only link the first occurrence to avoid over-optimization
                    linkedContent = linkedContent.replaceFirst(
                        trimmedKeyword,
                        "<a href='${article.originalLink}'>$trimmedKeyword</a>"
                    )
                    break 
                }
            }
        }
        return linkedContent
    }
}
