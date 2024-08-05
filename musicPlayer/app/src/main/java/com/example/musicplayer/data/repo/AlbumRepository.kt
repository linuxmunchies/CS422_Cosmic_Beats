package com.example.musicplayer.data.repo

import com.example.musicplayer.data.dao.AlbumDao
import com.example.musicplayer.data.Entities.Album

class AlbumRepository(private val albumDao: AlbumDao) {

    // Add an album
    suspend fun addAlbum(album: Album) {
        albumDao.addAlbum(album)
    }

    // Get an album by title
    suspend fun getAlbumTitle(title: String): Album? {
        return albumDao.getAlbumTitle(title)
    }

    // Get all albums
    suspend fun getAllAlbums(): List<Album> {
        return albumDao.getAllAlbums()
    }

    // Update an album
    suspend fun updateAlbum(album: Album) {
        albumDao.updateAlbum(album)
    }

    // Delete an album
    suspend fun deleteAlbum(album: Album) {
        albumDao.deleteAlbum(album)
    }
}
