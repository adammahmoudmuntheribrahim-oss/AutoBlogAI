package com.autoblog.ai.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.autoblog.ai.data.api.RssRepository
import com.autoblog.ai.data.repository.PostRepository
import com.autoblog.ai.utils.LocalStorage
import com.autoblog.ai.utils.PreferencesManager

class PublishWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = PreferencesManager(applicationContext)
        if (!prefs.isAllKeysSet()) {
            return Result.failure()
        }

        return try {
            val localStorage = LocalStorage(applicationContext)
            val repository = PostRepository(
                localStorage = localStorage,
                rss = RssRepository(),
                prefs = prefs
            )
            repository.fetchAndProcess()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
