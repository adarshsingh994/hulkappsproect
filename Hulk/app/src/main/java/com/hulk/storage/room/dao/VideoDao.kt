package com.hulk.storage.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hulk.storage.room.entity.Video

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addVideo(video : Video) : Long

    @Transaction
    @Query("SELECT * FROM videos")
    fun getAllVideos() : List<Video>

    @Transaction
    @Query("SELECT * FROM videos WHERE videos.url LIKE :url")
    fun getVideos(url : String) : List<Video>

    @Transaction
    @Query("SELECT * FROM videos")
    fun listenForVideos() : LiveData<List<Video>?>

    @Query("UPDATE videos SET path = :path WHERE videos.url LIKE :url")
    fun updateVideoPath(url : String, path : String?)

    @Query("delete from videos where path is null")
    fun deleteUndownloadedVideos()

    @Delete
    fun removeVideo(item : Video) : Int
}