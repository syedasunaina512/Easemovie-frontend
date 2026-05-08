package com.sakeena.easemovieapp

data class SceneModel(
    val index: Int = 0,
    var text: String = "",
    var mood: String = "neutral",
    var camera: String = "normal",
    var imagePath: String? = null,
    var description: String? = null
)
