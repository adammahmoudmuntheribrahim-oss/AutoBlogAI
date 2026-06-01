package com.example.articleautomator

import androidx.lifecycle.*
import com.example.articleautomator.data.LogEntry
import com.example.articleautomator.data.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val logRepository: LogRepository
) : ViewModel() {

    val logs: LiveData<List<LogEntry>> = logRepository.allLogs.asLiveData()

    fun clearLogs() {
        viewModelScope.launch {
            // Add clear functionality if needed in DAO
        }
    }
}
