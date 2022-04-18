package com.hulk.storage.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "videos")
data class Video(

    @PrimaryKey
    @NotNull
    @ColumnInfo(name = "url")
    val url : String,

    @ColumnInfo(name = "description")
    var description: String? = null,

    @ColumnInfo(name = "subtitle")
    var subtitle: String? = null,

    @ColumnInfo(name = "thumbnail")
    var thumbnail: String? = null,

    @ColumnInfo(name = "title")
    var title: String? = null,

    @ColumnInfo(name = "path")
    val path : String?,

    @ColumnInfo(name = "extension")
    val extension : String?,

    @ColumnInfo(name="state")
    val state : Int?,

    @ColumnInfo(name = "progress")
    val progress : Int?
){
    companion object{
        const val STATE_PENDING = 1
        const val STATE_ONGOING = 2
        const val STATE_SUCCESS = 3
        const val STATE_FAILED = 4
    }
}