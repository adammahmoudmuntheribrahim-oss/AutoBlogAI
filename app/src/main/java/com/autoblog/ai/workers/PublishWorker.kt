package com.autoblog.ai.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.autoblog.ai.data.api.RssRepository
import com.autoblog.ai.data.repository.PostRepository
import com.autoblog.ai.utils.LocalStorage
import com.autoblog.ai.utils.PreferencesManager

import androidx.hilt.work.HiltWorker
import com.autoblog.ai.data.repository.PostRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PublishWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: PostRepository,
    private val prefs: PreferencesManager
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "AutoBlogAI_PublishWorker"
    }

    override suspend fun doWork(): Result {
        if (!prefs.isAllKeysSet()) {
            return Result.failure()
        }

        return try {
            repository.fetchAndProcess()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
