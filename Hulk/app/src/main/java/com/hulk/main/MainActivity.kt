package com.hulk.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.hulk.R
import com.hulk.download.DownloadService
import com.hulk.homescreen.fragment.HomeFragment
import com.hulk.main.viewmodel.MainViewModel
import com.hulk.main.viewmodel.MainViewModelFactory
import com.hulk.videoplayer.fragment.VideoFragment


class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel : MainViewModel

    private var toolbar : Toolbar? = null
    private var appbarLayout : AppBarLayout? = null

    private var isVideoSelected = false

    // Observer to listen to the clicked videos
    private val selectedVideoObserver = Observer<com.hulk.storage.room.entity.Video?>{ video ->
        if(video != null){ // A video is selected, update UI and show VideoPlayerFragment
            isVideoSelected = true
            setActionBarBackButton(true)
            showVideoFragment(video.url, video.path)
            invalidateOptionsMenu()
        }else{ // No video selected, update UI and show HomeFragment (List of available videos)
            isVideoSelected = false
            setActionBarBackButton(false)
            showHomeFragment()
            invalidateOptionsMenu()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        setupToolbar()
        initViewModel()
        observe()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater

        if(isVideoSelected){ // Inflate menu for video player screen
            inflater.inflate(R.menu.video_menu, menu)
        }else{ // Inflate menu for home screen
            inflater.inflate(R.menu.home_menu, menu)

            val menuItem = menu.findItem(R.id.home_menu_offline)
            val mySwitch = menuItem.actionView as SwitchCompat

            // Listen to the offline/online switch events
            mySwitch.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                mainViewModel.setOfflineMode(isChecked)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            // Action bar back button
            android.R.id.home -> {
                val feature = mainViewModel.getSelectedVideo()
                if(feature != null) mainViewModel.setSelectedVideo(null)
                true
            }

            // Save button in VideoPlayerFragment
            R.id.video_menu_save -> {
                // Cannot download multiple videos
                if(DownloadService.IS_SERVICE_STARTED){
                    Toast.makeText(this, getString(R.string.parallel_download_error), Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, getString(R.string.save_video_info), Toast.LENGTH_SHORT).show()
                    performDownload()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val feature = mainViewModel.getSelectedVideo()
        if(feature != null) mainViewModel.setSelectedVideo(null)
        else super.onBackPressed()
    }

    private fun initViews(){
        toolbar = findViewById(R.id.main_toolbar)
        appbarLayout = findViewById(R.id.main_app_bar_layout)
    }

    private fun setupToolbar(){
        setSupportActionBar(toolbar)
    }

    private fun setActionBarBackButton(shouldShow : Boolean){
        supportActionBar?.setDisplayHomeAsUpEnabled(shouldShow)
    }

    private fun initViewModel(){
        mainViewModel = ViewModelProvider(this
            , MainViewModelFactory()
        ).get(MainViewModel::class.java)
    }

    private fun observe(){
        mainViewModel.selectedVideo.observe(this, selectedVideoObserver)
    }

    private fun showHomeFragment(){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_fragment_container, HomeFragment.newInstance())
        fragmentTransaction.commit()
    }

    // Start DownloadService to download selected video
    private fun performDownload(){
        mainViewModel.getSelectedVideo()?.let{ video ->
            startService(video.url)
        }
    }

    private fun showVideoFragment(url : String?, path : String?){
        if(url == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        else{
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.main_fragment_container, VideoFragment.newInstance(url, path))
            fragmentTransaction.commit()
        }
    }

    private fun startService(u : String){
        if(!DownloadService.IS_SERVICE_STARTED){
            Intent(this, DownloadService::class.java).also { intent ->
                intent.action = DownloadService.ACTION_START_SERVICE
                intent.putExtra(DownloadService.KEY_URL, u)

                startForegroundService(intent)
            }
        }
    }

    private fun stopService(){
        if(DownloadService.IS_SERVICE_STARTED){
            Intent(this, DownloadService::class.java).also { intent ->
                intent.action = DownloadService.ACTION_STOP_SERVICE

                startForegroundService(intent)
            }
        }
    }
}