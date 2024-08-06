package com.example.musicplayer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlistTable")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val queueList: List<Song>
)