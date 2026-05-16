package com.autoblog.ai.data.model

data class Article(
    val id: Int = 0,
    val title: String,
    val link: String,
    val content: String,
    val imageUrl: String,
    val tags: String,
    val published: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
