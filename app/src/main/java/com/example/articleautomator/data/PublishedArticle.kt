package com.example.articleautomator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "published_articles")
data class PublishedArticle(
    @PrimaryKey val guid: String,
    val contentHash: String, // Added for anti-duplicate
    val publishedDate: Long = System.currentTimeMillis()
)
