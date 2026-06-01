package com.example.articleautomator.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(private val logDao: LogDao) {
    val allLogs: Flow<List<LogEntry>> = logDao.getAllLogs()

    suspend fun addLog(message: String, status: String = "INFO") {
        logDao.insert(LogEntry(message = message, status = status))
    }
}
