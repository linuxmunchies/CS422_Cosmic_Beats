package com.example.musicplayer.data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.musicplayer.data.entities.Album

@Dao
interface AlbumDao {

    @Insert
    fun addAlbum(album: Album)
    //adds an album to our album table

    @Query("SELECT * FROM albumTable WHERE title= :title")
    fun getAlbumTitle(title: String): Album?
    //gets album title

    @Query("SELECT * FROM albumTable WHERE title= :title")
    fun getAlbumArtist(title: String): Album?
    //gets album artist

    @Query("SELECT * FROM albumTable")
    fun getAllAlbums(): List<Album>

    @Update
    fun updateAlbum(album: Album)

    @Delete
    fun deleteAlbum(album: Album)

}
