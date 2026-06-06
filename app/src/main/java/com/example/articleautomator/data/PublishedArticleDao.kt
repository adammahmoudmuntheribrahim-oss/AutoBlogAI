package com.example.articleautomator.data

import androidx.room.*

@Dao
interface PublishedArticleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(article: PublishedArticle)

    @Query("SELECT EXISTS(SELECT 1 FROM published_articles WHERE guid = :guid OR contentHash = :hash)")
    suspend fun isPublished(guid: String, hash: String): Boolean

    @Query("SELECT COUNT(*) FROM published_articles")
    suspend fun getTotalPublishedCount(): Int

    @Query("SELECT COUNT(*) FROM published_articles WHERE publishedDate >= :startOfDay")
    suspend fun getTodayPublishedCount(startOfDay: Long): Int
}
