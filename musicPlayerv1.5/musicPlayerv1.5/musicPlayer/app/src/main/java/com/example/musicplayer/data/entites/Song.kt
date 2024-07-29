package com.example.musicplayer.data.entites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songTable")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val duration: Float,
    val fileData: ByteArray
    //storing mp3 file data
    )