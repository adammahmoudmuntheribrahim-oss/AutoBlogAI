package com.autoblog.ai.data.db

import androidx.room.*
import com.autoblog.ai.data.model.Article
import com.autoblog.ai.data.model.PostStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY createdAt DESC")
    fun getAllArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE status = :status")
    suspend fun getArticlesByStatus(status: PostStatus): List<Article>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article): Long

    @Update
    suspend fun updateArticle(article: Article)

    @Query("SELECT EXISTS(SELECT 1 FROM articles WHERE originalLink = :link LIMIT 1)")
    suspend fun isDuplicate(link: String): Boolean

    @Query("DELETE FROM articles")
    suspend fun clearAll()
}
