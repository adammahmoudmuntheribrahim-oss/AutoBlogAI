package com.autoblog.ai.data.api

import com.autoblog.ai.data.model.ArticleItem
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RssRepository @Inject constructor(private val okHttpClient: OkHttpClient) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://example.com/") // Base URL is ignored for @Url, but required
        .client(okHttpClient)
        .addConverterFactory(SimpleXmlConverterFactory.create())
        .build()

    private val rssService = retrofit.create(RssService::class.java)

    suspend fun fetchArticles(rssUrl: String): List<ArticleItem> {
        return try {
            val response = rssService.getRssFeed(rssUrl)
            if (response.isSuccessful) {
                response.body()?.channel?.articles ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
