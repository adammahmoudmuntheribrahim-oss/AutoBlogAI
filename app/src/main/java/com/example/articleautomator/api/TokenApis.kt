package com.example.articleautomator.api

import retrofit2.http.*

data class TokenResponse(
    val access_token: String,
    val refresh_token: String?,
    val expires_in: Long
)

interface GoogleTokenApi {
    @POST("token")
    @FormUrlEncoded
    suspend fun refreshToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("refresh_token") refreshToken: String,
        @Field("grant_type") grantType: String = "refresh_token"
    ): TokenResponse

    @POST("token")
    @FormUrlEncoded
    suspend fun exchangeCode(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("code_verifier") codeVerifier: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ): TokenResponse
}

interface PinterestTokenApi {
    @POST("v5/oauth/token")
    @FormUrlEncoded
    suspend fun refreshToken(
        @Header("Authorization") authorization: String,
        @Field("refresh_token") refreshToken: String,
        @Field("grant_type") grantType: String = "refresh_token"
    ): TokenResponse

    @POST("v5/oauth/token")
    @FormUrlEncoded
    suspend fun exchangeToken(
        @Header("Authorization") authorization: String,
        @Field("code") code: String,
        @Field("code_verifier") codeVerifier: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ): TokenResponse
}

// Facebook token API (Graph API)
interface FacebookTokenApi {
    // Exchange short-lived token for long-lived token
    @GET("/oauth/access_token")
    suspend fun exchangeToken(
        @Query("grant_type") grantType: String = "fb_exchange_token",
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("fb_exchange_token") shortLivedToken: String
    ): TokenResponse

    // Generic access token endpoint (authorization_code)
    @FormUrlEncoded
    @POST("/oauth/access_token")
    suspend fun exchangeCode(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ): TokenResponse
}

// LinkedIn token API
interface LinkedInTokenApi {
    @FormUrlEncoded
    @POST("/oauth/v2/accessToken")
    suspend fun exchangeCode(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): TokenResponse

    @FormUrlEncoded
    @POST("/oauth/v2/accessToken")
    suspend fun refreshToken(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): TokenResponse
}

// X / Twitter OAuth2 token API (placeholder for OAuth2 flows)
interface XTokenApi {
    @FormUrlEncoded
    @POST("/2/oauth2/token")
    suspend fun exchangeCode(
        @Field("code") code: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("client_id") clientId: String,
        @Field("code_verifier") codeVerifier: String,
        @Field("redirect_uri") redirectUri: String
    ): TokenResponse

    @FormUrlEncoded
    @POST("/2/oauth2/token")
    suspend fun refreshToken(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String
    ): TokenResponse
}
