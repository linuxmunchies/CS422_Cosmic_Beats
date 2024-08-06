package com.example.musicplayer.data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.musicplayer.data.entities.Playlist

@Dao
interface PlaylistDao {

    @Insert
    suspend fun addPlaylist(playlist: Playlist)
    //adds a playlist to our playlist table

    @Query("SELECT * FROM playlistTable WHERE title= :title")
    suspend fun getlaylistTitle(title: String): Playlist?
    //gets Playlist title

    @Query("SELECT * FROM playlistTable")
    suspend fun getAllPlaylists(): List<Playlist>

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

}