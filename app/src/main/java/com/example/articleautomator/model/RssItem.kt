package com.example.articleautomator.model

data class RssItem(
    val title: String,
    val link: String,
    val description: String,
    val guid: String,
    val contentHash: String = "" // Added for better anti-duplicate
)
