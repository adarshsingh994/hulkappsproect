package com.hulk.homescreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hulk.network.Request
import com.hulk.storage.room.database.HulkDatabase

class HomeViewModelFactory(private val request : Request, private val db : HulkDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(request, db) as T
    }
}