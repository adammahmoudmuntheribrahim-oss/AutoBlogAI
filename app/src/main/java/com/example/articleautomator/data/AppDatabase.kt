package com.example.articleautomator.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PublishedArticle::class, LogEntry::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun publishedArticleDao(): PublishedArticleDao
    abstract fun logDao(): LogDao
}
