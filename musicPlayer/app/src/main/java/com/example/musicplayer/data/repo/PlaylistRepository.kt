package com.example.musicplayer.data.repo

import com.example.musicplayer.data.dao.PlaylistDao
import com.example.musicplayer.data.Entities.Playlist

class PlaylistRepository(private val playlistDao: PlaylistDao) {

    // Add a playlist
    suspend fun addPlaylist(playlist: Playlist) {
        playlistDao.addPlaylist(playlist)
    }

    // Get a playlist by title
    suspend fun getPlaylistTitle(title: String): Playlist? {
        return playlistDao.getlaylistTitle(title)
    }

    // Get all playlists
    suspend fun getAllPlaylists(): List<Playlist> {
        return playlistDao.getAllPlaylists()
    }

    // Update a playlist
    suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist)
    }

    // Delete a playlist
    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }
}
