package com.hulk.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel() : ViewModel() {
    val selectedVideo = MutableLiveData<com.hulk.storage.room.entity.Video?>(null)
    val offlineMode = MutableLiveData<Boolean>(false)

    fun getSelectedVideo() : com.hulk.storage.room.entity.Video? {
        return selectedVideo.value
    }

    fun setSelectedVideo(video : com.hulk.storage.room.entity.Video?){
        selectedVideo.postValue(video)
    }

    fun setOfflineMode(isOfflineMode : Boolean){
        offlineMode.value = isOfflineMode
    }
}