package com.autoblog.ai.data.repository

import com.autoblog.ai.data.api.*
import com.autoblog.ai.utils.InternalLinker
import com.autoblog.ai.utils.AiHumanizer
import com.autoblog.ai.data.db.ArticleDao
import com.autoblog.ai.data.model.Article
import com.autoblog.ai.data.model.PostStatus
import com.autoblog.ai.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val articleDao: ArticleDao,
    private val rssRepository: RssRepository,
    private val geminiService: GeminiService,
    private val bloggerRepository: BloggerRepository,
    private val aiImageRepository: AiImageRepository,
    private val prefs: PreferencesManager,
    private val internalLinker: InternalLinker,
    private val aiHumanizer: AiHumanizer
) {

    suspend fun fetchAndProcess() = withContext(Dispatchers.IO) {
        val rssUrl = prefs.getRssUrl()
        if (rssUrl.isEmpty()) return@withContext

        val items = rssRepository.fetchArticles(rssUrl)
        for (item in items) {
            if (articleDao.isDuplicate(item.link)) continue

            val article = Article(
                title = item.title,
                originalLink = item.link,
                content = item.description ?: "",
                status = PostStatus.PENDING
            )
            articleDao.insertArticle(article)
        }

        processQueue()
    }

    private suspend fun processQueue() {
        val pendingArticles = articleDao.getArticlesByStatus(PostStatus.PENDING)
        for (article in pendingArticles) {
            processArticle(article)
            // Rate Limiting: 3 requests / minute (20 seconds delay)
            delay(20000)
        }
    }

    private suspend fun processArticle(article: Article) {
        var currentArticle = article.copy(status = PostStatus.PROCESSING)
        articleDao.updateArticle(currentArticle)

        try {
            // 1. AI Rewrite & SEO Generation
            val rewritten = geminiService.rewriteArticle(article.content)
            
            // 2. Image Generation
            val imageUrl = aiImageRepository.generateImage(rewritten.title)

            // 3. Humanize & Internal Linking
            val humanizedContent = aiHumanizer.humanize(rewritten.content)
            val finalContent = internalLinker.addInternalLinks(humanizedContent)

            currentArticle = currentArticle.copy(
                title = rewritten.title,
                content = finalContent,
                imageUrl = imageUrl,
                seoTitle = rewritten.title,
                metaDescription = rewritten.metaDescription,
                keywords = rewritten.keywords,
                slug = rewritten.slug,
                faqSchema = rewritten.faqSchema,
                tags = rewritten.keywords
            )

            // 3. Publish to Blogger
            val success = bloggerRepository.publishPost(
                title = currentArticle.title,
                content = formatBloggerContent(currentArticle)
            )

            if (success) {
                currentArticle = currentArticle.copy(status = PostStatus.PUBLISHED)
            } else {
                currentArticle = currentArticle.copy(status = PostStatus.FAILED, failureReason = "Blogger publishing failed")
            }
        } catch (e: Exception) {
            currentArticle = currentArticle.copy(status = PostStatus.FAILED, failureReason = e.message)
        }

        articleDao.updateArticle(currentArticle)
    }

    private fun formatBloggerContent(article: Article): String {
        return """
            <div dir="rtl" style="text-align: right;">
                ${if (article.imageUrl != null) "<img src='${article.imageUrl}' style='width:100%; height:auto; margin-bottom:20px;' alt='${article.title}' loading='lazy' />" else ""}
                ${article.content}
                ${if (article.faqSchema != null) "<script type='application/ld+json'>${article.faqSchema}</script>" else ""}
            </div>
        """.trimIndent()
    }
}
