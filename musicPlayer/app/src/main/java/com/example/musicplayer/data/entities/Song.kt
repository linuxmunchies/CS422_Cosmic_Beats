package com.example.musicplayer.data.entities

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Song(
    @PrimaryKey val id: Long = 0,
    val title: String,
    val artist: String,
    val duration: Float,
    val uri: Uri
    )