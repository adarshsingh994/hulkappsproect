package com.hulk.homescreen.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.ExoPlayer
import com.hulk.R
import com.hulk.Util.logd
import com.hulk.homescreen.model.VideoView
import com.hulk.homescreen.utils.VideoDiffUtils
import com.hulk.homescreen.viewholder.VideoViewHolder
import com.hulk.homescreen.viewholder.VideoViewHolderListener


class VideosAdapter(private val glide : RequestManager
            , private val listener : VideoAdapterListener)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(), VideoViewHolderListener {


    // Available list of videos
    private var videos = ArrayList<VideoView>()
    // Index of video which is currently playing. -1 if no video is playing
    private var currentPlayingIndex = -1
    // ExoPlayer object for the current video playing
    private var currentPlayer : ExoPlayer? = null
    // It maps the url to its last known seek position
    private val urlSeekPositionMap = HashMap<String, Long>()

    // Payloads to observe changes in dataset
    enum class Payload{
        VIEW_CHANGE,
        STATE_CHANGE
    }

    override fun getItemCount(): Int = videos.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VideoViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_view_holder, parent, false), glide)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val item = videos[position]
        bindViewHolder(holder as VideoViewHolder, item, payloads, position)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        (holder as VideoViewHolder).unbind()
    }

    override fun onClick(position: Int) {
        listener.onVideoClicked(position)
    }

    // Creates a new list with objects at new memory location for DiffUtils class
    fun setTransactions(list : List<VideoView>){
        val callback = VideoDiffUtils(list, videos)
        val result = DiffUtil.calculateDiff(callback)
        videos.clear()
        videos.addAll(
            list.map {
                VideoView(it.url
                    , it.path
                    , it.title
                    , it.subTitle
                    , it.description
                    , it.thumbnailUrl
                    , it.isDownloaded
                    , it.videoShowing)
            }
        )
        result.dispatchUpdatesTo(this)
    }

    // Play the video at the specified index and stops the previously playing video
    fun playVideoAtIndex(index : Int){
        val l = ArrayList<VideoView>()
        l.addAll(
            videos.map {
                VideoView(it.url
                    , it.path
                    , it.title
                    , it.subTitle
                    , it.description
                    , it.thumbnailUrl
                    , it.isDownloaded
                    , it.videoShowing)
            }
        )

        // Process previous video
        if(currentPlayingIndex != -1){
            // Save the seek position
            if(currentPlayingIndex < l.size){
                val url = l[currentPlayingIndex].url
                val currentSeekPosition = currentPlayer?.currentPosition
                if(url != null && currentSeekPosition != null)
                    urlSeekPositionMap[url] = currentSeekPosition

                // Stop playing old video
                l[currentPlayingIndex]?.videoShowing = false
            }
        }

        // Update the current video playing index
        currentPlayingIndex = index

        // Start playing new video
        l[index]?.videoShowing = true
        "Setting transactions".logd("LJGSGNSLNSLGNLGNSLGN")
        setTransactions(l)
    }

    // Release ExoPlayer if not being used
    fun releasePlayer(){
        currentPlayer?.release()
    }

    private fun bindViewHolder(holder: VideoViewHolder,
                               video: VideoView,
                               payloads: MutableList<Any>,
                               position : Int){
        if(payloads.isEmpty()){ // Binds the view holder
            holder.bind(video, position, this)
        } else{ // Updates the view according to changes in object using DiffUtils
            payloads.forEach{ payload ->
                when(payload){
                    Payload.STATE_CHANGE -> { // Downloaded state
                        holder.setDownloaded(video.isDownloaded)
                    }

                    Payload.VIEW_CHANGE -> { // Shows thumbnail view or player view
                        // Pause previously playing video
                        if(currentPlayer != null) {
                            currentPlayer?.pause()
                        }
                        // Start playing new video
                        currentPlayer = ExoPlayer.Builder(holder.itemView.context).build()
                        holder.setPlayerView(currentPlayer!!
                            , video.videoShowing
                            , video.url
                            , video.path
                            , urlSeekPositionMap[video.url] ?: 0L)
                    }
                }
            }
        }
    }

}