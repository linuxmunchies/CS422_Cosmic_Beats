package com.example.musicplayer.data.repo

import com.example.musicplayer.data.dao.SongDao
import com.example.musicplayer.data.Entities.Song

class SongRepository(private val songDao: SongDao) {

    // Add a song
    suspend fun addSong(song: Song) {
        songDao.addSong(song)
    }

    // Get a song by title
    suspend fun getSong(title: String): Song? {
        return songDao.getSong(title)
    }

    // Get a song by artist
    suspend fun getSongArtist(title: String): List<Song> {
        return songDao.getSongArtist(title)
    }

    // Get a song by duration
    suspend fun getSongDuration(duration: Float): Song? {
        return songDao.getSongDuration(duration)
    }

    // Get all songs
    suspend fun getAllSongs(): List<Song> {
        return songDao.getAllSongs()
    }

    // Update a song
    suspend fun updateSong(song: Song) {
        songDao.updateSong(song)
    }

    // Delete a song
    suspend fun deleteSong(song: Song) {
        songDao.deleteSong(song)
    }
}