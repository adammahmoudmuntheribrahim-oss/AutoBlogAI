package com.example.articleautomator.workflow

interface SocialPublisher {
    suspend fun getAuthUrl(): String
    suspend fun authenticateWithCode(code: String)
    suspend fun publishPost(blogUrl: String, title: String, excerpt: String, imageUrl: String?): String
    fun isConnected(): Boolean
}
