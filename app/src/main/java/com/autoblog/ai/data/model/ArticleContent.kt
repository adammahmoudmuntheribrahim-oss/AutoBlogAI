package com.autoblog.ai.data.model

data class ArticleContent(
    val title: String,
    val content: String,
    val metaDescription: String? = null,
    val keywords: String? = null,
    val slug: String? = null,
    val faqSchema: String? = null
)
