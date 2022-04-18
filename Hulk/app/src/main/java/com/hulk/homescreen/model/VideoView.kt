package com.hulk.homescreen.model

data class VideoView(
                val url : String?
                , val path : String?
                , val title : String?
                , val subTitle : String?
                , val description : String?
                , val thumbnailUrl : String?
                , var isDownloaded : Boolean
                , var videoShowing : Boolean)