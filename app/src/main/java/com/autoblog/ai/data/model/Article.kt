package com.autoblog.ai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PostStatus {
    PENDING,
    PROCESSING,
    FAILED,
    PUBLISHED
}

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val originalLink: String,
    val content: String,
    val imageUrl: String? = null,
    val tags: String? = null,
    val status: PostStatus = PostStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val failureReason: String? = null,
    
    // SEO Metadata
    val seoTitle: String? = null,
    val metaDescription: String? = null,
    val keywords: String? = null,
    val slug: String? = null,
    val faqSchema: String? = null
)
