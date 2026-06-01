package com.example.articleautomator.di

import android.content.Context
import androidx.room.Room
import com.example.articleautomator.data.AppDatabase
import com.example.articleautomator.data.LogDao
import com.example.articleautomator.data.PublishedArticleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "articles_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePublishedArticleDao(db: AppDatabase): PublishedArticleDao = db.publishedArticleDao()

    @Provides
    fun provideLogDao(db: AppDatabase): LogDao = db.logDao()
}
