package com.example.musicplayer.data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.musicplayer.data.entites.Album
import com.example.musicplayer.data.entites.Collection
import com.example.musicplayer.data.entites.Song

@Dao
interface CollectionDao {

    @Insert
    suspend fun addCollection(collection: Collection)

    @Query("SELECT * FROM collectionTable WHERE songList= :songList")
    suspend fun getSongList(songList: List<Song>): Collection?

    @Query("SELECT * FROM collectionTable WHERE albumList= albumList")
    suspend fun getAlbumList(albumList: List<Album>): Collection?

    @Query("SELECT * FROM collectionTable")
    suspend fun getCollection(): List<Collection>

    @Update
    suspend fun updateCollection(collection: Collection)

    @Delete
    suspend fun deleteCollection(collection: Collection)

}