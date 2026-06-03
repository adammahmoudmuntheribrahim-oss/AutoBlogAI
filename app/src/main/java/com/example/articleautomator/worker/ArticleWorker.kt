package com.example.articleautomator.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.articleautomator.model.WriterPersonality
import com.example.articleautomator.workflow.ArticleWorkflow
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@HiltWorker
class ArticleWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val workflow: ArticleWorkflow
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val rssUrls = prefs.getStringSet("rss_urls", emptySet())?.toList() ?: return Result.success()
        val lengthOption = prefs.getString("length_option", "متوسط") ?: "متوسط"
        val personalityName = prefs.getString("personality", WriterPersonality.REALIST.name)
        val personality = WriterPersonality.valueOf(personalityName ?: WriterPersonality.REALIST.name)
        val isRandom = prefs.getBoolean("random_scheduling", false)
        val pinterestEnabled = prefs.getBoolean("pinterest_enabled", true)

        if (rssUrls.isEmpty()) return Result.success()

        // Execute workflow for a random RSS URL or all
        val targetUrl = rssUrls.random()
        workflow.execute(targetUrl, lengthOption, personality, pinterestEnabled)

        // If random scheduling is on, schedule next run with random delay
        if (isRandom) {
            val maxDelay = prefs.getInt("max_delay_minutes", 240)
            val nextDelay = Random.nextLong(15, maxDelay.toLong())
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val nextRequest = OneTimeWorkRequestBuilder<ArticleWorker>()
                .setInitialDelay(nextDelay, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag("ARTICLE_WORKER")
                .build()
            
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "ArticleWorker_Random",
                ExistingWorkPolicy.REPLACE,
                nextRequest
            )
        }

        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return super.getForegroundInfo()
    }
}
