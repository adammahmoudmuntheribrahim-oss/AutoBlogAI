package com.example.articleautomator

import androidx.lifecycle.*
import com.example.articleautomator.data.LogEntry
import com.example.articleautomator.data.LogRepository
import com.example.articleautomator.workflow.RssFetcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val rssFetcher: RssFetcher
) : ViewModel() {

    val logs: LiveData<List<LogEntry>> = logRepository.allLogs.asLiveData()

    suspend fun validateRssUrl(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val item = rssFetcher.fetchLatest(url)
            item != null
        } catch (e: Exception) {
            false
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            // Add clear functionality if needed in DAO
        }
    }
}
