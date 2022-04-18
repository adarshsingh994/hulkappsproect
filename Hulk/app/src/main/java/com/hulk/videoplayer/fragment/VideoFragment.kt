package com.hulk.videoplayer.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.hulk.R

private const val ARG_URL = "url"
private const val ARG_PATH = "path"

/**
 * This class plays the video using [ExoPlayer] library.
 * Make sure that url is not null
 */
class VideoFragment : Fragment() {
    // Url of the video. Cannot be null
    private var url: String? = null

    // The download path. Can be null
    private var path: String? = null

    companion object {
        @JvmStatic
        fun newInstance(u: String, p : String?) =
            VideoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, u)
                    putString(ARG_PATH, p)
                }
            }
    }

    private var playerView : StyledPlayerView? = null
    private var player : ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString(ARG_URL)
            path = it.getString(ARG_PATH)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_video, container, false)
        initViews(rootView)
        setupExoPlayer(url)
        return rootView
    }

    override fun onPause() {
        super.onPause()
        player?.release()
    }

    private fun initViews(view : View){
        playerView = view.findViewById(R.id.video_fragment_player_view)
    }

    private fun setupExoPlayer(url : String?){
        if(url == null) return

        player = ExoPlayer.Builder(requireContext()).build()
        playerView?.player = player

        // Play from downloaded path if available else play from provided url
        val videoUri = Uri.parse(path ?: url.replace("http", "https"))

        val mediaItem: MediaItem = MediaItem.fromUri(videoUri)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }
}