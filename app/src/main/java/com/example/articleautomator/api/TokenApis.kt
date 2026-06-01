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
