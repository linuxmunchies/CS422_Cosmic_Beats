package com.example.musicplayer.data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.musicplayer.data.entities.Album
import com.example.musicplayer.data.entities.Collection
import com.example.musicplayer.data.entities.Song

@Dao
interface CollectionDao {

    @Insert
    fun addCollection(collection: Collection)

    //@Query("SELECT * FROM collectionTable WHERE songList= :songList")
    //suspend fun getSongList(songList: List<Song>): Collection?

    //@Query("SELECT * FROM collectionTable WHERE albumList= albumList")
    //suspend fun getAlbumList(albumList: List<Album>): Collection?

    @Query("SELECT * FROM collectionTable")
    fun getCollection(): List<Collection>

    @Update
    fun updateCollection(collection: Collection)

    @Delete
    fun deleteCollection(collection: Collection)

}