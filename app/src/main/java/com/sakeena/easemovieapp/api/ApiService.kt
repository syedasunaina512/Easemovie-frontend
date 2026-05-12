package com.sakeena.easemovieapp.api

import retrofit2.http.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface ApiService {

    @POST("segment")
    suspend fun splitScenes(
        @Body request: SegmentRequest
    ): SegmentResponse

    @POST("generate_image")
    suspend fun generateImage(
        @Query("text") text: String,
        @Query("style") style: String,
        @Query("emotion") emotion: String
    ): ImageResponse

    @POST("generate_video")
    suspend fun generateVideo(
        @Body request: VideoRequest
    ): VideoResponse

    @POST("generate_voice")
    suspend fun generateNarration(
        @Body request: VoiceRequest
    ): VoiceResponse
}

object ApiClient {
    private const val BASE_URL = "https://easemovie-backend.onrender.com/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
