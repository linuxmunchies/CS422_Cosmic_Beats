package com.example.musicplayer.data.entites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albumTable")
data class Album(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val songList: List<Song>
)