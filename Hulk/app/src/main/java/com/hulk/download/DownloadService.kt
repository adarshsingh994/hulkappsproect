package com.hulk.download

import android.app.DownloadManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.hulk.R
import com.hulk.notification.NotificationManager
import com.hulk.storage.room.database.HulkDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// This service contains the logic to download files and update database accordingly
class DownloadService : Service(), Downloader.DownloaderListener {

    companion object{
        var IS_SERVICE_STARTED = false

        const val ACTION_START_SERVICE = "start"
        const val ACTION_STOP_SERVICE = "stop"

        const val KEY_URL = "url"
    }

    inner class DownloadServiceBinder : Binder() {
        val service: DownloadService
            get() = this@DownloadService
    }

    private var ongoingDownloadCount = 0
    private val binder: IBinder = DownloadServiceBinder()
    private var notificationManager : NotificationManager? = null
    private var downloader : Downloader? = null
    private lateinit var db : HulkDatabase

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        IS_SERVICE_STARTED = true

        notificationManager = NotificationManager(applicationContext)

        downloader = Downloader(this, getSystemService(DOWNLOAD_SERVICE) as DownloadManager)

        // Register DownloadManagerBroadcastReceiver when service is created
        downloader?.let{
            val broadcastReceiver = it.getBroadcastReceiver()
            registerReceiver(broadcastReceiver, broadcastReceiver.getIntentFilter())
        }

        db = HulkDatabase(applicationContext)

        // Create a notification channel
        createNotificationChannel()

        // Required for foreground service
        setServiceStartNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        IS_SERVICE_STARTED = false

        // Unregister DownloadManagerBroadcastReceiver when service is destroyed
        downloader?.let{
            val broadcastReceiver = it.getBroadcastReceiver()
            unregisterReceiver(broadcastReceiver)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        // Unregister DownloadManagerBroadcastReceiver when service is destroyed
        downloader?.let{
            val broadcastReceiver = it.getBroadcastReceiver()
            unregisterReceiver(broadcastReceiver)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when(intent.action){
            ACTION_START_SERVICE -> intent.getStringExtra(KEY_URL)?.let{ attemptDownload(it) }
            ACTION_STOP_SERVICE -> stopService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // Called when file download is finished
    override fun onFileDownloaded(url : String, path : String?) {
        // Update database with the downloaded path
        GlobalScope.launch(Dispatchers.IO) {
            updateVideo(url, path)
            ongoingDownloadCount -= 1 // Decrement ongoing count
            stopServiceIfIdle()
        }
    }

    // Called when file download starts
    override fun onFileDownloadStarted(url : String) {
        ongoingDownloadCount += 1 // Increment ongoing count
    }

    // Called when file download is finished
    override fun onFileDownloadError(url : String) {
        ongoingDownloadCount -= 1 // Decrement ongoing count
        stopServiceIfIdle()
    }

    override fun onFileDownloadRunning(url : String) {}

    override fun onFileDownloadPending(url: String) {}

    override fun onFileDownloadPaused(url: String) {}

    override fun onFileAlreadyDownloaded() {}

    // Use [Downloader] to start downloading files
    private fun attemptDownload(url : String){
        GlobalScope.launch(Dispatchers.IO) {
            db.getVideoDao().getVideos(url)?.let{ videos ->
                // Since there can be only 1 video of a given url
                videos[0]?.let { video ->

                    // Video Download
                    downloader?.startDownloadingFile(applicationContext
                        , url.replace("http", "https")
                        , video.extension)
                }
            }
        }
    }

    // Stops service is no download is in progress
    private fun stopServiceIfIdle(){
        if(ongoingDownloadCount <= 0){
            stopService()
        }
    }

    private fun createNotificationChannel(){
        notificationManager?.createNotificationChannel(
            NotificationManager.DOWNLOADSERVICECHANNELID
            , getString(R.string.download_service_channel_name)
            , getString(R.string.download_service_channel_description))
    }

    private fun setServiceStartNotification(){
        val notification = notificationManager
            ?.getDownloadServiceNotification(
                NotificationManager.DOWNLOADSERVICECHANNELID
            )

        startForeground(
            NotificationManager.DOWNLOADSERVICENOTIFICATIONID,
            notification
        )
    }

    private fun stopService(){
        stopForeground(true)
        stopSelf()
    }

    // Function to update the saved path in database
    private suspend fun updateVideo(url : String, savedPath : String?){
        db.getVideoDao().updateVideoPath(url.replace("https", "http"), savedPath)
    }
}