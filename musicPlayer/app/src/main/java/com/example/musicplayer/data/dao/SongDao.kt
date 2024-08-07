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
    fun getAllSongs(): List<Song>

    @Update
    fun updateSong(song: Song)

    @Delete
    fun deleteSong(song: Song)

    @Insert
    fun addSong(song: Song)
    //adds a song to our song table

    @Query("SELECT * FROM song WHERE title= :title")
    fun getSong(title: String): Song?
    //suspension is for not blocking main thread for better user exp
    //gets song title

    @Query("SELECT * FROM song WHERE artist= :artist")
    fun getSongArtist(artist: String): List<Song>
    //suspension is for not blocking main thread for better user exp
    //gets song artist

    @Query("SELECT * FROM song WHERE duration= :duration")
    fun getSongDuration(duration: Float): Song?
    //gets song duration

    @Query("delete from song")
    fun deleteSongAll()
}