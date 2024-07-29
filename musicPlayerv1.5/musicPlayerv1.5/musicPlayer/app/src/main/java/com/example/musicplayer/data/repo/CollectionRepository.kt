package com.example.musicplayer.data.repo

import com.example.musicplayer.data.dao.CollectionDao
import com.example.musicplayer.data.entites.Album
import com.example.musicplayer.data.entites.Collection
import com.example.musicplayer.data.entites.Song

class CollectionRepository(private val collectionDao: CollectionDao) {

    // Add a collection
    suspend fun addCollection(collection: Collection) {
        collectionDao.addCollection(collection)
    }

    // Get a collection by song list
    suspend fun getSongList(songList: List<Song>): Collection? {
        return collectionDao.getSongList(songList)
    }

    // Get a collection by album list
    suspend fun getAlbumList(albumList: List<Album>): Collection? {
        return collectionDao.getAlbumList(albumList)
    }

    // Get all collections
    suspend fun getAllCollections(): List<Collection> {
        return collectionDao.getCollection()
    }

    // Update a collection
    suspend fun updateCollection(collection: Collection) {
        collectionDao.updateCollection(collection)
    }

    // Delete a collection
    suspend fun deleteCollection(collection: Collection) {
        collectionDao.deleteCollection(collection)
    }
}
