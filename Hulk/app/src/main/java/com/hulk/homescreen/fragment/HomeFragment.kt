package com.hulk.homescreen.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.hulk.MiddleItemFinder
import com.hulk.MiddleItemFinder.MiddleItemCallback
import com.hulk.R
import com.hulk.Util.logd
import com.hulk.homescreen.adapter.VideoAdapterListener
import com.hulk.homescreen.adapter.VideosAdapter
import com.hulk.homescreen.viewmodel.HomeViewModel
import com.hulk.homescreen.viewmodel.HomeViewModelFactory
import com.hulk.main.viewmodel.MainViewModel
import com.hulk.main.viewmodel.MainViewModelFactory
import com.hulk.network.Request
import com.hulk.storage.room.database.HulkDatabase


class HomeFragment : Fragment(), VideoAdapterListener {
    private var param1: String? = null
    private var param2: String? = null

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    // Index of middle item
    private var videoShowingIndex = -1

    // Shows progress bar when video is fetching
    private val isLoadingObserver = Observer<Boolean> { isLoading ->
        progressBar?.visibility = if(isLoading) View.VISIBLE else View.GONE
    }

    // Shows list of videos
    private val videosObserver = Observer<List<com.hulk.storage.room.entity.Video>?> { videos ->
        videosAdapter?.setTransactions(homeViewModel.toVideoView(videos, videoShowingIndex))
    }

    // Listens to offline mode switch callback
    // If offline show only videos which are download else show videos from server
    private val offlineModeObserver = Observer<Boolean> { offlineMode ->
        if(offlineMode) homeViewModel.removeOnlineVideos()
        else homeViewModel.getVideos()
    }

    private var videoRecyclerView : RecyclerView? = null
    private var progressBar : ProgressBar? = null
    private var errorTextView : TextView? = null

    private var videosAdapter : VideosAdapter? = null
    private lateinit var homeViewModel : HomeViewModel
    private lateinit var mainViewModel : MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        videosAdapter = VideosAdapter(Glide.with(requireContext()), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        initView(rootView)
        setupRecyclerView()
        initViewModels()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
    }

    override fun onVideoClicked(position: Int) {
        mainViewModel.setSelectedVideo(homeViewModel.getVideoAt(position))
    }

    override fun onPause() {
        super.onPause()
        videosAdapter?.releasePlayer()
    }

    private fun initView(view : View){
        videoRecyclerView = view.findViewById(R.id.home_fragment_videos_recycler_view)
        progressBar = view.findViewById(R.id.home_fragment_progress_bar)
        errorTextView = view.findViewById(R.id.home_fragment_error_text_view)
    }

    private fun setupRecyclerView(){
        // Notifies about the element which is at the center of screen
        val callback: MiddleItemCallback = object : MiddleItemCallback {
            override fun scrollFinished(middleElement: Int) {
                // Middle element found, play video at that index
                val index = middleElement.inc()
                videoShowingIndex = index
                videosAdapter?.playVideoAtIndex(index)
            }
        }

        val lManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        with(videoRecyclerView!!){
            layoutManager = lManager
            addOnScrollListener(
                MiddleItemFinder(context, lManager, callback, RecyclerView.SCROLL_STATE_IDLE)
            )
            adapter = videosAdapter
        }
    }

    private fun initViewModels(){
        homeViewModel = ViewModelProvider(
            requireActivity(), HomeViewModelFactory(Request(), HulkDatabase(requireContext()))
        ).get(HomeViewModel::class.java)

        mainViewModel = ViewModelProvider(
            requireActivity(), MainViewModelFactory()
        ).get(MainViewModel::class.java)
    }

    private fun observe(){
        homeViewModel.videos.observe(viewLifecycleOwner, videosObserver)
        homeViewModel.isLoading.observe(viewLifecycleOwner, isLoadingObserver)
        mainViewModel.offlineMode.observe(viewLifecycleOwner, offlineModeObserver)
    }
}