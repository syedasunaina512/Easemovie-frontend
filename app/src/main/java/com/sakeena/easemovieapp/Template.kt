package com.sakeena.easemovieapp

data class Template(
    val id: String = "",
    val title: String = "",
    val complexity: String = "",
    val imageRes: Int = 0,
    val rating: String = "4.0",
    val promptTemplate: String = "",
    val style: String = "Anime",
    val description: String = ""
)
