package com.sakeena.easemovieapp.api

data class SegmentRequest(
    val text: String
)

data class SceneDto(
    val index: Int,
    val text: String,
    val mood: String,
    val camera: String
)

data class SegmentResponse(
    val scenes: List<SceneDto>
)

data class ImageResponse(
    val image_path: String
)

data class VideoRequest(
    val scenes: List<String>,
    val style: String
)

data class VideoResponse(
    val video_url: String
)
