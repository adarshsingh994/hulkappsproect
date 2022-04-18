package com.hulk.pojo

data class Video(
    var description: String? = null,
    var sources: ArrayList<String>? = null,
    var subtitle: String? = null,
    var thumb: String? = null,
    var title: String? = null
)