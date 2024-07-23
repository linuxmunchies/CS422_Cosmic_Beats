package com.example.musicplayer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.musicplayer.data.entites.Song

@Dao
interface SongDao {

    @Insert
    suspend fun addSong(song: Song)
    //adds a song to our song table

    @Query("SELECT * FROM songTable WHERE title= :title")
    suspend fun getSong(title: String): Song?
    //suspension is for not blocking main thread for better user exp
    //gets song title

    @Query("SELECT * FROM songTable WHERE title= :title")
    suspend fun getSongArtist(title: String): Song?
    //suspension is for not blocking main thread for better user exp
    //gets song artist

    @Query("SELECT * FROM songTable WHERE duration= :duration")
    suspend fun getSongDuration(duration: Float): Song?
    //gets song duration

    @Query("SELECT * FROM songTable")
    suspend fun getAllSongs(): List<Song>

    @Update
    suspend fun updateSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

}