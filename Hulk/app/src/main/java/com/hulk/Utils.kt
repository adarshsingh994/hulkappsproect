package com.hulk

import android.util.Log

object Util {

    // Provided endpoint
    val url = "https://movies.free.beeceptor.com/movieslist"

    fun String.logd(tag : String){
        Log.d(tag, this)
    }
}