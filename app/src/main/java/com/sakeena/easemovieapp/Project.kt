package com.sakeena.easemovieapp

data class Project(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val style: String = "",
    val scenes: List<SceneModel> = emptyList()
)
