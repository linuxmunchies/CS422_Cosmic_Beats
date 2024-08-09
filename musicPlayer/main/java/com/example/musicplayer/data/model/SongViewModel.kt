package com.example.musicplayer.data.model

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.database.SongDatabase
import com.example.musicplayer.data.repo.SongRepository
import kotlinx.coroutines.launch
import java.io.File

class SongViewModel (application: Application) : AndroidViewModel(application) {
    private val songRepository: SongRepository

    init {
        val songDao = SongDatabase.getDatabase(application).songDao()
        songRepository = SongRepository(songDao)
    }

    fun addSong(filePath: String, title: String, artist: String, duration: Float) = viewModelScope.launch {
        val fileData = File(filePath).readBytes()
        //val song = Song(title = title, artist = artist, duration = duration, fileData = fileData)
        //songRepository.addSong(song)
    }

    fun retrieveSong(title: String, context: Context) = viewModelScope.launch {
        val retrievedSong = songRepository.getSong(title)
        if (retrievedSong != null) {
            //val outputFile = File(context.filesDir, "${retrievedSong.title}.mp3")
            //outputFile.writeBytes(retrievedSong.fileData)
        }
    }
}