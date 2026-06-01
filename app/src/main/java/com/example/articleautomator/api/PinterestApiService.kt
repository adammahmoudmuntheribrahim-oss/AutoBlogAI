package com.example.articleautomator.api

import retrofit2.http.*

data class PinRequest(
    val title: String,
    val description: String,
    val link: String?,
    val media_source: MediaSource,
    val board_id: String
)
data class MediaSource(val source_type: String = "image_url", val url: String)
data class PinResponse(val id: String)

interface PinterestApiService {
    @POST("v5/pins")
    suspend fun createPin(
        @Header("Authorization") token: String,
        @Body pinRequest: PinRequest
    ): PinResponse
}
