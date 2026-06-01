package com.example.articleautomator.workflow

import com.example.articleautomator.data.PublishedArticleDao
import com.example.articleautomator.data.LogRepository
import com.example.articleautomator.data.PublishedArticle
import com.example.articleautomator.model.BlogPost
import com.example.articleautomator.model.WriterPersonality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleWorkflow @Inject constructor(
    private val rssFetcher: RssFetcher,
    private val geminiRewriter: GeminiRewriter,
    private val imageGenerator: ImageGenerator,
    private val bloggerPublisher: BloggerPublisher,
    private val pinterestPublisher: PinterestPublisher,
    private val publishedDao: PublishedArticleDao,
    private val logRepository: LogRepository
) {
    suspend fun execute(rssUrl: String, lengthOption: String, personality: WriterPersonality) {
        withContext(Dispatchers.IO) {
            try {
                logRepository.addLog("بدء سير العمل للرابط: $rssUrl", "INFO")
                
                val item = rssFetcher.fetchLatest(rssUrl)
                if (item == null) {
                    logRepository.addLog("فشل جلب المقال من RSS", "ERROR")
                    return@withContext
                }
                logRepository.addLog("تم جلب المقال: ${item.title}", "INFO")

                // Enhanced Anti-Duplicate: Check both GUID and Content Hash
                if (publishedDao.isPublished(item.guid, item.contentHash)) {
                    logRepository.addLog("المقال موجود مسبقًا - تم تخطيه", "INFO")
                    return@withContext
                }

                val (articleHtml, metaDescription) = geminiRewriter.rewriteWithRetry(
                    originalText = item.description,
                    lengthOption = lengthOption,
                    personalityInstruction = personality.instruction
                )
                logRepository.addLog("تمت إعادة كتابة المقال (الشخصية: ${personality.displayName})", "SUCCESS")

                val imagePrompt = geminiRewriter.generateImagePrompt(item.title, articleHtml.take(500))
                val imageUrl = imageGenerator.generateImageUrl(imagePrompt)
                logRepository.addLog("تم إنشاء صورة معبرة", "SUCCESS")

                val blogPost = BlogPost(
                    title = item.title,
                    content = articleHtml,
                    metaDescription = metaDescription
                )
                val blogUrl = bloggerPublisher.publish(blogPost)
                logRepository.addLog("تم النشر على Blogger: $blogUrl", "SUCCESS")

                pinterestPublisher.createPin(blogUrl, imageUrl, item.title)
                logRepository.addLog("تم إنشاء Pin في Pinterest", "SUCCESS")

                publishedDao.insert(PublishedArticle(guid = item.guid, contentHash = item.contentHash))
                logRepository.addLog("اكتمل سير العمل بنجاح (${personality.displayName})", "SUCCESS")
            } catch (e: Exception) {
                logRepository.addLog("خطأ في سير العمل: ${e.message}", "ERROR")
            }
        }
    }
}
