package com.example.musicplayer.data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.musicplayer.data.entities.Artist
import com.example.musicplayer.data.entities.Collection

@Dao
interface ArtistDao {

    @Insert
    suspend fun addArtist(artist: Artist)
    //adds an artist to our artist table

    @Query("SELECT * FROM artistTable WHERE name= :name")
    suspend fun getArtistName(name: String): Artist?

    @Query("SELECT * FROM artistTable WHERE collectionList= :collectionList")
    suspend fun getArtistCollection(collectionList: List<Collection>): Artist?

    @Query("SELECT * FROM artistTable")
    suspend fun getAllArtists(): List<Artist>

    @Update
    suspend fun updateArtist(artist: Artist)

    @Delete
    suspend fun deleteArtist(artist: Artist)

}