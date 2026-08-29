package com.example.tvbrowser

data class Bookmark(
    val title: String,
    val url: String,
    // Setting this to null by default prevents errors in existing constructor calls
    val thumbnailPath: String? = null
)