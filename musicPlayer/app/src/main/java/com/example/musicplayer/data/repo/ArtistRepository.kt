package com.example.musicplayer.data.repo

import com.example.musicplayer.data.dao.ArtistDao
import com.example.musicplayer.data.entites.Artist

class ArtistRepository(private val artistDao: ArtistDao) {

    // Add an artist
    suspend fun addArtist(artist: Artist) {
        artistDao.addArtist(artist)
    }

    // Get an artist by name
    suspend fun getArtistName(name: String): Artist? {
        return artistDao.getArtistName(name)
    }

    // Get all artists
    suspend fun getAllArtists(): List<Artist> {
        return artistDao.getAllArtists()
    }

    // Update an artist
    suspend fun updateArtist(artist: Artist) {
        artistDao.updateArtist(artist)
    }

    // Delete an artist
    suspend fun deleteArtist(artist: Artist) {
        artistDao.deleteArtist(artist)
    }
}
