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
    fun addArtist(artist: Artist)
    //adds an artist to our artist table

    @Query("SELECT * FROM artistTable WHERE name= :name")
    fun getArtistName(name: String): Artist?

    //@Query("SELECT * FROM artistTable WHERE collectionList= :collectionList")
    //suspend fun getArtistCollection(collectionList: List<Collection>): Artist?

    @Query("SELECT * FROM artistTable")
    fun getAllArtists(): List<Artist>

    @Update
    fun updateArtist(artist: Artist)

    @Delete
    fun deleteArtist(artist: Artist)

}