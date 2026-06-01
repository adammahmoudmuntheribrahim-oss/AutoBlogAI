package com.example.articleautomator.model

data class BlogPost(
    val title: String,
    val content: String,
    val labels: List<String> = emptyList(),
    val metaDescription: String = ""
)
