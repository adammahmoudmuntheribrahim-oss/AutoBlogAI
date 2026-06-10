package com.example.articleautomator.di

import com.example.articleautomator.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetryInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request()
        var response: okhttp3.Response? = null
        var exception: IOException? = null
        var attempt = 1
        val maxAttempts = 3

        while (attempt <= maxAttempts) {
            try {
                response = chain.proceed(request)
                if (response.isSuccessful || response.code == 401 || response.code == 404) {
                    return@Interceptor response
                }
            } catch (e: IOException) {
                exception = e
            }

            if (attempt == maxAttempts) break

            val backoffDelay = 2000L * attempt
            try {
                Thread.sleep(backoffDelay)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
            attempt++
        }

        response ?: throw exception ?: IOException("Unknown network error")
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(retryInterceptor: Interceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor(retryInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideGeminiApi(client: OkHttpClient): GeminiApiService = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeminiApiService::class.java)

    @Provides
    @Singleton
    fun provideBloggerApi(client: OkHttpClient): BloggerApiService = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BloggerApiService::class.java)

    @Provides
    @Singleton
    fun providePinterestApi(client: OkHttpClient): PinterestApiService = Retrofit.Builder()
        .baseUrl("https://api.pinterest.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PinterestApiService::class.java)

    @Provides
    @Singleton
    fun provideGoogleTokenApi(client: OkHttpClient): GoogleTokenApi = Retrofit.Builder()
        .baseUrl("https://oauth2.googleapis.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GoogleTokenApi::class.java)

    @Provides
    @Singleton
    fun providePinterestTokenApi(client: OkHttpClient): PinterestTokenApi = Retrofit.Builder()
        .baseUrl("https://api.pinterest.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PinterestTokenApi::class.java)

    @Provides
    @Singleton
    fun provideWordPressApi(client: OkHttpClient): WordPressApiService = Retrofit.Builder()
        .baseUrl("https://placeholder.com/") // سيتم استخدام @Url أو تغيير الرابط في الطلب إذا لزم الأمر، لكن هنا سنعتمد على أن المستخدم يدخل الرابط الكامل
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WordPressApiService::class.java)
}
