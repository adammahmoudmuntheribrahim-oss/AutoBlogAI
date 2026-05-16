package com.autoblog.ai.data.api

import com.autoblog.ai.data.model.RssFeed
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface RssService {
    @GET
    suspend fun getRssFeed(@Url url: String): Response<RssFeed>
}
