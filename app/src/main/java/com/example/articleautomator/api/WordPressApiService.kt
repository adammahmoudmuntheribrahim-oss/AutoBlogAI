package com.example.articleautomator.api

import retrofit2.http.*

data class WordPressPostRequest(
    val title: String,
    val content: String,
    val status: String = "publish",
    val format: String = "standard"
)

data class WordPressResponse(
    val id: Int,
    val link: String
)

interface WordPressApiService {
    @POST
    suspend fun createPost(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body post: WordPressPostRequest
    ): WordPressResponse
}
