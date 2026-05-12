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
    val style: String,
    val narration_url: String? = null
)

data class VideoResponse(
    val video_url: String
)

data class NarrationRequest(
    val text: String,
    val voice: String = "en-US-JennyNeural"
)

data class NarrationResponse(
    val audio_url: String
)
