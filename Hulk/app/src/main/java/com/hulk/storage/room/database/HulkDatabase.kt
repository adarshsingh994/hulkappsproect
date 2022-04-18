package com.hulk.storage.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hulk.storage.room.dao.VideoDao
import com.hulk.storage.room.entity.Video

@Database(
    entities = [Video::class],
    version = 1
)
abstract class HulkDatabase : RoomDatabase(){

    companion object {
        const val AUTOGENERATEID = 0L

        @Volatile private var instance: HulkDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it} }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            HulkDatabase::class.java, "hulk-database.db")
            .build()
    }

    abstract fun getVideoDao() : VideoDao
}