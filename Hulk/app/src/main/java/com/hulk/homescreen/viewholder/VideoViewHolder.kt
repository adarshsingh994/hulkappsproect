package com.hulk.homescreen.viewholder

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.hulk.R
import com.hulk.Util.logd
import com.hulk.homescreen.model.VideoView

class VideoViewHolder(view : View
                      , private val glide : RequestManager)
    : RecyclerView.ViewHolder(view) {
    private val root : ConstraintLayout = itemView.findViewById(R.id.video_view_root)
    private val title : TextView = itemView.findViewById(R.id.video_view_title)
    private val description : TextView = itemView.findViewById(R.id.video_view_description)
    private val thumbnail : ImageView = itemView.findViewById(R.id.video_view_thumbnail)
    private val subTitle : TextView = itemView.findViewById(R.id.video_view_subtitle)
    private val downloadedIndicator : ImageView = itemView.findViewById(R.id.video_view_download_indicator)
    private val playerView : StyledPlayerView = itemView.findViewById(R.id.video_view_player_view)

    fun bind(video : VideoView
             , position : Int
             , listener : VideoViewHolderListener){
        title.text = video.title ?: "untitled"
        description.text = video.description ?: "null"
        subTitle.text = video.subTitle ?: "null"

        glide.load(video.thumbnailUrl?.replace("http", "https")) // Android 12+ requires only https
            .dontAnimate()
            .error(R.drawable.ic_launcher_background)
            .into(thumbnail)

        setDownloaded(video.isDownloaded)
        hidePlayerView()
        setListener(position, listener)
    }

    fun unbind(){
        glide.clear(thumbnail)
    }

    // Shows an indicator if video is downloaded
    fun setDownloaded(isDownloaded : Boolean){
        if(isDownloaded) downloadedIndicator.visibility = View.VISIBLE
        else downloadedIndicator.visibility = View.GONE
    }

    // Sets up ExoPlayer for the view if video playing else shows thumbnail
    fun setPlayerView(player : ExoPlayer, shouldShow : Boolean, url : String?, path : String?, seekPosition : Long){
        if(shouldShow){
            showPlayerView()
            setupExoPlayer(player, url, path, seekPosition)
        }else{
            hidePlayerView()
        }
    }

    private fun setupExoPlayer(player : ExoPlayer, url : String?, path : String?, seekPosition: Long){
        // Url is the primary datasource. Cannot play video if url is null
        if(url == null) return

        playerView.player = player

        // Play from saved path if available else play from url
        val videoUri = Uri.parse(path ?: url.replace("http", "https")) // android 12+ requires https
        val mediaItem: MediaItem = MediaItem.fromUri(videoUri)
        player.setMediaItem(mediaItem)
        player.seekTo(seekPosition ) // Seek to the previously played position if available
        player.playWhenReady = true
        player.prepare()
        player.play()
    }

    private fun showPlayerView(){
        playerView.visibility = View.VISIBLE
    }

    private fun hidePlayerView(){
        playerView.visibility = View.GONE
    }

    private fun setListener(position : Int, listener: VideoViewHolderListener){
        root.setOnClickListener {
            listener.onClick(position)
        }
    }
}