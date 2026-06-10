package com.example.articleautomator.workflow

import android.content.Context
import com.example.articleautomator.data.PublishedArticleDao
import com.example.articleautomator.data.LogRepository
import com.example.articleautomator.data.PublishedArticle
import com.example.articleautomator.model.BlogPost
import com.example.articleautomator.model.WriterPersonality
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleWorkflow @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rssFetcher: RssFetcher,
    private val geminiRewriter: GeminiRewriter,
    private val imageGenerator: ImageGenerator,
    private val bloggerPublisher: BloggerPublisher,
    private val wordpressPublisher: WordPressPublisher,
    private val pinterestPublisher: PinterestPublisher,
    private val publishedDao: PublishedArticleDao,
    private val logRepository: LogRepository
) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    suspend fun <T> retry(times: Int = 3, initialDelay: Long = 1000, block: suspend () -> T): T {
        var currentDelay = initialDelay
        repeat(times - 1) {
            try {
                return block()
            } catch (e: Exception) {
                logRepository.addLog("فشل المحاولة، إعادة المحاولة بعد $currentDelay مللي ثانية...", "INFO")
                kotlinx.coroutines.delay(currentDelay)
                currentDelay *= 2
            }
        }
        return block() // Last attempt
    }

    suspend fun execute(
        rssUrl: String, 
        lengthOption: String, 
        personality: WriterPersonality, 
        targetLanguage: String = "English"
    ) {
        withContext(Dispatchers.IO) {
            try {
                val bloggerEnabled = prefs.getBoolean("blogger_enabled", true)
                val wordpressEnabled = prefs.getBoolean("wordpress_enabled", false)
                val pinterestEnabled = prefs.getBoolean("pinterest_enabled", true)

                if (!bloggerEnabled && !wordpressEnabled) {
                    logRepository.addLog("جميع منصات النشر (Blogger/WordPress) معطلة. تم إيقاف سير العمل.", "INFO")
                    return@withContext
                }

                logRepository.addLog("بدء سير العمل للرابط: $rssUrl", "INFO")
                
                val item = retry {
                    rssFetcher.fetchLatest(rssUrl) ?: throw Exception("فشل جلب المقال من RSS")
                }
                logRepository.addLog("تم جلب المقال: ${item.title}", "INFO")

                if (publishedDao.isPublished(item.guid, item.contentHash)) {
                    logRepository.addLog("المقال موجود مسبقًا - تم تخطيه", "INFO")
                    return@withContext
                }

                val (articleHtml, metaDescription) = retry {
                    geminiRewriter.rewriteWithRetry(
                        originalText = item.description,
                        lengthOption = lengthOption,
                        personalityInstruction = personality.instruction,
                        targetLanguage = targetLanguage
                    )
                }
                logRepository.addLog("تمت إعادة كتابة المقال (اللغة: $targetLanguage، الشخصية: ${personality.displayName})", "SUCCESS")

                val imageUrl = retry {
                    val imagePrompt = geminiRewriter.generateImagePrompt(item.title, articleHtml.take(500))
                    imageGenerator.generateImageUrl(imagePrompt)
                }
                logRepository.addLog("تم إنشاء صورة معبرة", "SUCCESS")

                val blogPost = BlogPost(
                    title = item.title,
                    content = articleHtml,
                    metaDescription = metaDescription
                )

                var primaryUrl: String? = null

                // Publish to Blogger
                if (bloggerEnabled) {
                    try {
                        val url = retry { bloggerPublisher.publish(blogPost) }
                        primaryUrl = url
                        logRepository.addLog("تم النشر على Blogger: $url", "SUCCESS")
                    } catch (e: Exception) {
                        logRepository.addLog("فشل النشر على Blogger: ${e.message}", "ERROR")
                    }
                }

                // Publish to WordPress
                if (wordpressEnabled) {
                    try {
                        val url = retry { wordpressPublisher.publish(blogPost) }
                        if (primaryUrl == null) primaryUrl = url
                        logRepository.addLog("تم النشر على WordPress: $url", "SUCCESS")
                    } catch (e: Exception) {
                        logRepository.addLog("فشل النشر على WordPress: ${e.message}", "ERROR")
                    }
                }

                // Publish to Pinterest
                if (pinterestEnabled && primaryUrl != null) {
                    try {
                        retry { pinterestPublisher.createPin(primaryUrl, imageUrl, item.title) }
                        logRepository.addLog("تم إنشاء Pin في Pinterest", "SUCCESS")
                    } catch (e: Exception) {
                        logRepository.addLog("فشل النشر على Pinterest (اختياري): ${e.message}", "INFO")
                    }
                }

                publishedDao.insert(PublishedArticle(guid = item.guid, contentHash = item.contentHash))
                logRepository.addLog("اكتمل سير العمل بنجاح (${personality.displayName})", "SUCCESS")
            } catch (e: Exception) {
                logRepository.addLog("خطأ فادح في سير العمل: ${e.message}", "ERROR")
            }
        }
    }
}
