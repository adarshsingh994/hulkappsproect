package com.hulk.notification

import com.hulk.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hulk.download.DownloadService

/**
 * This is a helper class to create notifications
 *
 * @param context object to get [NotificationManager] object
 */
class NotificationManager(private val context: Context) {

    companion object {
        const val DOWNLOADSERVICECHANNELID = "Download service"
        const val DOWNLOADSERVICENOTIFICATIONID = 8736
    }

    // Creates notification channel
    fun createNotificationChannel(channelId : String, chanelName : String, channelDescription: String, isImportanceHigh: Boolean = false) {
        val importance = if(isImportanceHigh) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(channelId, chanelName, importance)
        channel.description = channelDescription
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    // Returns notification which is shown when [DownloadService] starts
    fun getDownloadServiceNotification(channelId : String) : Notification{
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(context.getString(R.string.downloading_text))
            .build()
    }
}