package com.hulk.homescreen.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.hulk.Util.logd
import com.hulk.Util.url
import com.hulk.homescreen.model.VideoView
import com.hulk.network.Request
import com.hulk.pojo.Root
import com.hulk.storage.room.database.HulkDatabase
import com.hulk.storage.room.entity.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeViewModel(private val request : Request, private val db : HulkDatabase) : ViewModel() {

    // Listen to videos in database
    val videos = db.getVideoDao().listenForVideos()

    // Listen to data fetch progress
    val isLoading : MutableLiveData<Boolean> = MutableLiveData(true)

    // Return video at specified index, null if not found
    fun getVideoAt(position : Int) : Video?{
        return videos.value?.get(position)
    }

    // Converts database's Video object to UI's video object
    fun toVideoView(list : List<Video>, videoShowingIndex : Int) : ArrayList<VideoView>{
        val l = ArrayList<VideoView>()
        list.forEachIndexed {index, video ->
            l.add(
                VideoView(video.url
                    , video.path
                    , video.title
                    , video.subtitle
                    , video.description
                    , getThumbnailUrl(video.thumbnail!!, video.url)
                    , video.path != null
                    , index == videoShowingIndex)
            )
        }

        return l
    }

    /**
     * Since the api only contains the sub url, this function helps to
     * create a full thumbnail url by concatenating the sub url with endpoint url
     * at appropriate place
     *
     * @param thumbSubUrl The sub url of thumbnail
     * @param videoUrl Full url of the video
     *
     * @return the full url of thumbnail, null if url is invalid
     */
    private fun getThumbnailUrl(thumbSubUrl : String, videoUrl : String) : String?{
        val lastSlashIndex = videoUrl.indexOfLast { it == '/' }
        return if(lastSlashIndex != -1) "${videoUrl.substring(0, lastSlashIndex)}/$thumbSubUrl"
        else null
    }

    // Removes videos which are not downloaded from database
    fun removeOnlineVideos(){
        GlobalScope.launch(Dispatchers.IO) {
            db.getVideoDao().deleteUndownloadedVideos()
        }
    }

    // Fetch available videos from endpoint
    fun getVideos(){
        request.run(url){ response, error ->
            // Hide progress
            GlobalScope.launch(Dispatchers.Main) { isLoading.value = false }

            // No error occurred
            if(error == null){
                // Convert response json to Object
                Gson().fromJson(response, Root::class.java).categories?.forEach { category ->
                    // Since only one category in dataset
                    category.videos?.forEach { video ->
                        // Only 1 source available for the video in dataset
                        val u = video.sources?.get(0)

                        // Add the fetched videos to database
                        db.getVideoDao().addVideo(Video(
                            u!!
                            , video.description
                            , video.subtitle
                            , video.thumb
                            , video.title
                            , null
                            , u.substring(u.lastIndexOf(".").inc())
                            , null
                            , 0))
                    }
                }
            }
        }
    }
}