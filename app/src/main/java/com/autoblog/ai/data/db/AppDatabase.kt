package com.autoblog.ai.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.autoblog.ai.data.model.Article

@Database(entities = [Article::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
}
