package com.autoblog.ai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.autoblog.ai.data.model.Article
import com.autoblog.ai.utils.LocalStorage
import com.autoblog.ai.utils.PreferencesManager
import com.autoblog.ai.workers.PublishWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val _posts = MutableStateFlow<List<Article>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSetupComplete = MutableStateFlow(false)
    val isSetupComplete = _isSetupComplete.asStateFlow()

    private val workManager = WorkManager.getInstance(application)
    private val preferencesManager = PreferencesManager(application)

    init {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkLiveData(PublishWorker.WORK_NAME)
                .asFlow()
                .map { workInfos ->
                    workInfos.any { it.state == WorkInfo.State.RUNNING }
                }
                .collect { running ->
                    _isLoading.value = running
                }
        }
        updateSetupStatus()
    }

    fun updateSetupStatus() {
        _isSetupComplete.value = preferencesManager.isAllKeysSet()
    }

    fun loadPosts(storage: LocalStorage) {
        viewModelScope.launch {
            _posts.value = storage.loadArticles()
        }
    }

    fun triggerPublishWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val publishWorkRequest = OneTimeWorkRequestBuilder<PublishWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            PublishWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            publishWorkRequest
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return DashboardViewModel(application) as T
            }
        }
    }
}
