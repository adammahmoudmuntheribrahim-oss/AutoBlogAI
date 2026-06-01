package com.example.articleautomator.api

import retrofit2.http.*

data class BloggerPost(
    val kind: String = "blogger#post",
    val title: String,
    val content: String,
    val labels: List<String>? = null
)

data class BloggerResponse(val id: String, val url: String)

interface BloggerApiService {
    @POST("v3/blogs/{blogId}/posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Path("blogId") blogId: String,
        @Body post: BloggerPost
    ): BloggerResponse
}
