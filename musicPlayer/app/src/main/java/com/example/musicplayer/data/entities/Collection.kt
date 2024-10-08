package com.example.musicplayer.data.entities

import android.provider.MediaStore.Audio.Albums
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collectionTable")
data class Collection(
    @PrimaryKey(autoGenerate = true) val id: Int,
    //val songList: List<Song>,
    //val albumList: List<Albums>
    //this is a sealed class to use in artist class
    //A collection is an artists' collection of songs and albums
)
