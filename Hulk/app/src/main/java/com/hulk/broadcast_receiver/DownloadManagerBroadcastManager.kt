package com.hulk.broadcast_receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * This is the broadcast receiver which listens to [DownloadManager]'s broadcast
 *
 * @param _listener is an interface used to communicate the result from [DownloadManager] to the listener
 */
class DownloadManagerBroadcastReceiver(private val _listener : DownloadManagerBroadcastListener) : BroadcastReceiver() {

    var listener : DownloadManagerBroadcastListener? = null

    init {
        listener = this._listener
    }

    // Listener interface for callback
    interface DownloadManagerBroadcastListener{
        fun onDownloadStatusChanged(id : Long)
    }

    fun getIntentFilter() : IntentFilter {
        return IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let{
            val id = it.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            listener?.onDownloadStatusChanged(id)
        }
    }

}