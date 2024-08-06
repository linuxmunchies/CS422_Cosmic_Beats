package com.example.musicplayer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.musicplayer.data.entities.Song

@Dao
interface SongDao {

    @Query("SELECT * FROM song")
    suspend fun getAllSongs(): List<Song>

    @Update
    suspend fun updateSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Insert
    suspend fun addSong(song: Song)
    //adds a song to our song table

    @Query("SELECT * FROM song WHERE title= :title")
    suspend fun getSong(title: String): Song?
    //suspension is for not blocking main thread for better user exp
    //gets song title

    @Query("SELECT * FROM song WHERE artist= :artist")
    suspend fun getSongArtist(artist: String): List<Song>
    //suspension is for not blocking main thread for better user exp
    //gets song artist

    @Query("SELECT * FROM song WHERE duration= :duration")
    suspend fun getSongDuration(duration: Float): Song?
    //gets song duration
}