package com.hulk.homescreen.utils

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.hulk.homescreen.adapter.VideosAdapter
import com.hulk.homescreen.model.VideoView

/**
 * A [DiffUtil] class for getting the changes in the new list
 *
 * @param newVideos The list of new videos
 * @param oldVideos The list of old videos
 */
class VideoDiffUtils(newVideos: List<VideoView>, oldVideos: List<VideoView>) : DiffUtil.Callback() {

    private val mNewVideos = mutableListOf<VideoView>().apply { addAll(newVideos) }
    private val mOldVideos = mutableListOf<VideoView>().apply { addAll(oldVideos) }

    override fun getOldListSize(): Int {
        return mOldVideos.size
    }

    override fun getNewListSize(): Int {
        return mNewVideos.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Video changed if the url is different as url is the primary key
        return mOldVideos[oldItemPosition].url == mNewVideos[newItemPosition].url
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldVideos[oldItemPosition] == mNewVideos[newItemPosition]
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val newVideo: VideoView = mNewVideos[newItemPosition]
        val oldVideo: VideoView = mOldVideos[oldItemPosition]

        return when{
            newVideo.isDownloaded != oldVideo.isDownloaded -> {
                VideosAdapter.Payload.STATE_CHANGE
            }
            newVideo.videoShowing != oldVideo.videoShowing -> {
                VideosAdapter.Payload.VIEW_CHANGE
            }
            else -> null
        }
    }
}