package com.autoblog.ai.workers

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object Scheduler {
    fun start(context: Context) {
        val request = PeriodicWorkRequestBuilder<PublishWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "autoblog_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
