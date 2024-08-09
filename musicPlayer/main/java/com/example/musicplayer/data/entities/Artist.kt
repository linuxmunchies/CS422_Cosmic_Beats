package com.example.musicplayer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artistTable")
data class Artist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    //val collectionList: List<Collection>
    //collection is a sealed class to use here so an artist can hold single songs and albums
)