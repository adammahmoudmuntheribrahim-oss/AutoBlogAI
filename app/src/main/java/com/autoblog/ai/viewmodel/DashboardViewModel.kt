package com.autoblog.ai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.autoblog.ai.data.db.ArticleDao
import com.autoblog.ai.data.model.Article
import com.autoblog.ai.data.model.PostStatus
import com.autoblog.ai.data.repository.PostRepository
import com.autoblog.ai.utils.PreferencesManager
import com.autoblog.ai.workers.PublishWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    private val articleDao: ArticleDao,
    private val repository: PostRepository,
    private val prefs: PreferencesManager
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    val posts: StateFlow<List<Article>> = articleDao.getAllArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSetupComplete = MutableStateFlow(prefs.isAllKeysSet())
    val isSetupComplete: StateFlow<Boolean> = _isSetupComplete

    // Analytics Stats
    val totalArticles = posts.map { it.size }
    val publishedToday = posts.map { list -> 
        val today = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        list.count { it.status == PostStatus.PUBLISHED && it.createdAt > today }
    }
    val failedArticles = posts.map { it.count { p -> p.status == PostStatus.FAILED } }
    val queueCount = posts.map { it.count { p -> p.status == PostStatus.PENDING || p.status == PostStatus.PROCESSING } }

    init {
        observeWorkStatus()
    }

    private fun observeWorkStatus() {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkLiveData(PublishWorker.WORK_NAME)
                .asFlow()
                .collect { workInfos ->
                    _isLoading.value = workInfos.any { it.state == WorkInfo.State.RUNNING }
                }
        }
    }

    fun updateSetupStatus() {
        _isSetupComplete.value = prefs.isAllKeysSet()
    }

    fun triggerPublishWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<PublishWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            PublishWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
