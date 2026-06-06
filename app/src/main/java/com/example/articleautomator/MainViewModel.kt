package com.example.articleautomator

import androidx.lifecycle.*
import com.example.articleautomator.data.LogEntry
import com.example.articleautomator.data.LogRepository
import com.example.articleautomator.data.PublishedArticleDao
import com.example.articleautomator.workflow.RssFetcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val rssFetcher: RssFetcher,
    private val publishedDao: PublishedArticleDao
) : ViewModel() {

    val logs: LiveData<List<LogEntry>> = logRepository.allLogs.asLiveData()

    private val _stats = MutableLiveData<DashboardStats>()
    val stats: LiveData<DashboardStats> = _stats

    fun refreshStats() {
        viewModelScope.launch {
            val total = publishedDao.getTotalPublishedCount()
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val today = publishedDao.getTodayPublishedCount(calendar.timeInMillis)
            _stats.postValue(DashboardStats(today, total))
        }
    }

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

data class DashboardStats(
    val todayCount: Int,
    val totalCount: Int
)
