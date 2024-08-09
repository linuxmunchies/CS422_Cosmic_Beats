package com.example.musicplayer

import android.content.ContentUris
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.musicplayer.data.database.SongDatabase
import com.example.musicplayer.data.entities.Song
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.Manifest
import android.app.PendingIntent.getActivity
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import android.os.Build
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import com.example.musicplayer.data.repo.SongRepository
import kotlinx.coroutines.runBlocking


class MainActivity : ComponentActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private var equalizer: Equalizer? = null

    companion object {
        private const val AUDIO_PERMISSION_CODE = 102
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermission(Manifest.permission.READ_MEDIA_AUDIO, AUDIO_PERMISSION_CODE)

        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener {
            equalizer = Equalizer(0, it.audioSessionId).apply {
                enabled = true
            }
        }
        setContent {
            MusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MusicPlayerApp(mediaPlayer,equalizer)
                }
            }
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                // Requesting the permission
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            } else {
                Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show()
                // Proceed with the operation that requires the permission
            }
        } else {
            // For devices running versions lower than Tiramisu, use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), requestCode)
            } else {
                Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show()
                // Proceed with the operation that requires the permission
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AUDIO_PERMISSION_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Audio Permission Granted", Toast.LENGTH_SHORT).show()
                    // Permission granted
                } else {
                    Toast.makeText(this, "Audio Permission Denied", Toast.LENGTH_SHORT).show()
                    // Permission denied, panic
                }
            }
        }
    }
}

@Composable
fun LoadSongsIntoDatabase() {

    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val db = SongDatabase.getDatabase(context)
    val songDao = db.songDao()

    //Delete all songs for testing
    runOnIO {
        songDao.deleteSongAll()
    }
    //horrible hack for testing to pause to make sure we deleted everything
    Thread.sleep(1000)

    //Set up the database of songs
    val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION
    )

    //We're looking for all songs, so don't filter them
    val selection = null
    val selectionArgs = null
    val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

    val query = contentResolver.query(
        collection,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )

    if (query == null) {
        println("Query returned null")
        return
    }

    if (!query.moveToFirst()) {
        println("Cursor is empty")
        return
    }

    query?.use { cursor ->
        // Cache column indices.
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

        while (cursor.moveToNext()) {
            // Get values of columns for a given song.
            val id = cursor.getLong(idColumn)
            val title = cursor.getString(titleColumn)
            val artist = cursor.getString(artistColumn)
            val duration = cursor.getFloat(durationColumn)
            val contentUri: Uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                id
            )
            print(id.toString() + title.toString() + artist.toString())
            //Add the song to the database
            runOnIO {
                songDao.addSong(Song(id, title, artist, duration, contentUri.toString()))
            }
        }
    }
}

fun runOnIO(lambda: suspend () -> Unit){
    runBlocking {
        CoroutineScope(Dispatchers.IO).launch {
            lambda()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerApp(mediaPlayer: MediaPlayer, equalizer: Equalizer?) {
    val navController = rememberNavController()
    val context = LocalContext.current

    //scan for changes
    MediaScannerConnection.scanFile(context, arrayOf(Environment.getExternalStorageDirectory().toString()), null, null)
    //Load songs into our database
    //TODO: Only do this when the MediaStore has changed, otherwise just use the existing one
    LoadSongsIntoDatabase()

    //Once everything is loaded into the database the first time, load it into the app so we don't have
    //to keep querying it every time, just at app launch
    val db = SongDatabase.getDatabase(context)
    val songDao = db.songDao()
    var songsInDatabase: MutableList<Song> = mutableListOf()
    runOnIO { songsInDatabase = songDao.getAllSongs().toMutableList() }
    //horrible waiting hack since we don't have time to properly deal with threads
    Thread.sleep(1000)

    //state for current song
    var currentSongIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cosmic Beats") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomPlayerBar(
                mediaPlayer,
                songsInDatabase,
                currentSongIndex,
                onSongChange = { newSongIndex ->
                    currentSongIndex = newSongIndex
                    val newSong = songsInDatabase[newSongIndex]
                    playSongFromUri(context, mediaPlayer, newSong.uri)
                    mediaPlayer.start()
                })

        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "playlists") {
            composable("playlists") {
                PlaylistsScreen(
                    onPlaylistClick = { playlistId ->
                        navController.navigate("playlist/$playlistId")
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(
                "playlist/{playlistId}",
                arguments = listOf(navArgument("playlistId") { type = NavType.IntType })
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getInt("playlistId") ?: 0
                PlaylistDetailScreen(
                    playlistId,
                    onSongClick = { songTitle, artistName, uri ->
                        navController.navigate("songDetail/$songTitle/$artistName")
                        val songIndex = songsInDatabase.indexOfFirst { it.title == songTitle && it.artist == artistName }
                        if (songIndex >= 0) {
                            currentSongIndex = songIndex
                        }
                        playSongFromUri(context, mediaPlayer, uri)
                        mediaPlayer.start()
                    },
                    Modifier.padding(innerPadding),
                    songsInDatabase
                )
            }
            composable(
                "songDetail/{songTitle}/{artistName}",
                arguments = listOf(
                    navArgument("songTitle") { type = NavType.StringType },
                    navArgument("artistName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val songTitle = backStackEntry.arguments?.getString("songTitle") ?: ""
                val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
                SongDetailScreen(
                    mediaPlayer,equalizer,
                    songTitle,
                    artistName,
                    Modifier.padding(innerPadding))
            }
        }
    }
}


@Composable
fun PlaylistsScreen(onPlaylistClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    val playlists = remember {
        listOf(
            "Favorites" to 15,
            "Rock Classics" to 20,
            "Chill Vibes" to 18,
            "Workout Mix" to 25,
            "Road Trip" to 30
        )
    }

    LazyColumn(modifier) {
        items(playlists.withIndex().toList()) { (index, playlist) ->
            ListItem(
                headlineContent = { Text(playlist.first, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingContent = { Text("${playlist.second} songs", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingContent = { Icon(Icons.Filled.PlaylistPlay, contentDescription = null) },
                modifier = Modifier.clickable { onPlaylistClick(index) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun PlaylistDetailScreen(
    playlistId: Int,
    onSongClick: (String, String, String) -> Unit,
    modifier: Modifier = Modifier,
    data: List<Song>) {

    Column(modifier) {
        Text(
            "Playlist ${playlistId + 1}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(data) { (id, title, artist, duration, uri) ->
                ListItem(
                    headlineContent = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    supportingContent = { Text(artist, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingContent = { Icon(Icons.Filled.MusicNote, contentDescription = null) },
                    modifier = Modifier.clickable { onSongClick(title, artist, uri) }
                )
                HorizontalDivider()
            }
        }
    }
}

//Set the given MediaPlayer to the given song uri, and get ready to play it
fun playSongFromUri(context: Context, mediaPlayer: MediaPlayer, uri: String){
    //Set the given MediaPlayer to the given song
    mediaPlayer.reset()
    mediaPlayer.apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        setDataSource(context, Uri.parse(uri))
        prepare()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomPlayerBar(mediaPlayer: MediaPlayer,
                    songList: MutableList<Song>,
                    currentSongIndex: Int,
                    onSongChange: (Int) -> Unit){

    var isPlaying by remember { mutableStateOf(false) }
    val context = LocalContext.current

    //I don't know how to get this from when someone clicks on a song in the list, since this bar is a
    //function and not a class so I can't make a function to get send it to here
//    var currentSong = posInSongList

    BottomAppBar(
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val newSongIndex = (currentSongIndex - 1 + songList.size) % songList.size
                        onSongChange(newSongIndex)
                        isPlaying = true
//                        mediaPlayer.start()
                    }) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Skip to previous")
                    }
                    IconButton(onClick = {
                        isPlaying = !isPlaying
                        if (isPlaying) {
                            if (!mediaPlayer.isPlaying) {
                                // Prepare and play the song if the player is not currently playing
                                playSongFromUri(context, mediaPlayer, songList[currentSongIndex].uri)
                            }
                            mediaPlayer.start()
                        } else {
                            mediaPlayer.pause()
                        }
                    }) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }
                    IconButton(onClick = {
                        val newSongIndex = (currentSongIndex + 1) % songList.size
//                        playSongFromUri(context, mediaPlayer, songList[currentSong].uri)
                        onSongChange(newSongIndex)
                        isPlaying = true
//                        mediaPlayer.start()
                    }) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Skip to next")
                    }
                }
                Column(modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)) {
                    Text(
                        //TODO: make this update
                        songList[currentSongIndex].title,
//                        "No track playing",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        //TODO: make this update
                        songList[currentSongIndex].artist,
//                        "",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    )
}


@Composable
fun SongDetailScreen(mediaPlayer: MediaPlayer,equalizer: Equalizer?, songTitle: String, artistName: String, modifier: Modifier = Modifier) {

    val context = LocalContext.current
//
//    val equalizer = remember {
//        val sessionId = mediaPlayer.audioSessionId
//        if (sessionId != AudioEffect.ERROR_BAD_VALUE) {
//            Equalizer(0, sessionId).apply {
//                enabled = true
//            }
//        } else {
//            null
//        }
//    }

    var isPlaying by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0f) }
    var sliderPosition1 by remember { mutableStateOf(0f) }
    var sliderPosition2 by remember { mutableStateOf(0f) }
    var sliderPosition3 by remember { mutableStateOf(0f) }
    var sliderPosition4 by remember { mutableStateOf(0f) }

    fun updateEqualizerBand(band: Short, level: Float) {
        equalizer?.let {
            val numberOfBands = it.numberOfBands
            if (band in 0 until numberOfBands) {
                val minLevel = it.bandLevelRange[0]
                val maxLevel = it.bandLevelRange[1]
                val newLevel = (minLevel + ((maxLevel - minLevel) * level)).toInt().toShort()
                try {
                    it.setBandLevel(band, newLevel)
                } catch (e: UnsupportedOperationException) {
                    Log.e("Equalizer", "Unsupported operation for band $band: ${e.message}")
                }
            }
        }
    }

//    val isEqualizerInitialized = equalizer != null && equalizer.numberOfBands > 0

    Column(modifier.padding(16.dp)) {
        Text(
            text = songTitle,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = artistName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Slider(
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it
                    updateEqualizerBand(0, it)
                },
                valueRange = 0f..1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Text(
                text = "60 Hz ${(sliderPosition * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Slider(
                value = sliderPosition1,
                onValueChange = {
//                    it ->
                    sliderPosition1 = it
                    updateEqualizerBand(1, it)
//                    equalizer.setBandLevel(1, ((sliderPosition1 * 60 - 30) * 100).toInt().toShort())
//                    mediaPlayer.pause()
//                    mediaPlayer.attachAuxEffect(equalizer.id)
//                    mediaPlayer.start()
//                    mediaPlayer.setAuxEffectSendLevel(sliderPosition1)
                                },
                valueRange = 0f..1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Text(
                text = "250 Hz ${(sliderPosition1 * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Slider(
                value = sliderPosition2,
                onValueChange = {
//                    it->
                    sliderPosition2 = it
                    updateEqualizerBand(2, it)
//                    equalizer.setBandLevel(2, ((sliderPosition2 * 60 - 30) * 100).toInt().toShort())
//                    mediaPlayer.pause()
//                    mediaPlayer.attachAuxEffect(equalizer.id)
//                    mediaPlayer.start()
//                    mediaPlayer.setAuxEffectSendLevel(sliderPosition2)
                    },
                valueRange = 0f..1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Text(
                text = "1k Hz ${(sliderPosition2 * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Slider(
                value = sliderPosition3,
                onValueChange = {
//                    it ->
                    sliderPosition3 = it
                    updateEqualizerBand(3, it)
//                    equalizer.let{it.setBandLevel(3, ((sliderPosition3 * 60 - 30) * 100).toInt().toShort())
//                        mediaPlayer.pause()
//                        mediaPlayer.attachAuxEffect(equalizer.id)
//                        mediaPlayer.start()
//                        mediaPlayer.setAuxEffectSendLevel(sliderPosition3)}
          },
                valueRange = 0f..1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Text(
                text = "4k Hz ${(sliderPosition3 * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Slider(
                value = sliderPosition4,
                onValueChange = {
                    sliderPosition4 = it
                    updateEqualizerBand(4, it)
                },
                valueRange = 0f..1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Text(
                text = "16k Hz ${(sliderPosition4 * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Equalizer Bar",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ElevatedButton(onClick = {
                updateEqualizerBand(0, 0.5f)
                updateEqualizerBand(1, 0.5f)
                updateEqualizerBand(2, 0.5f)
                updateEqualizerBand(3, 0.5f)
                updateEqualizerBand(4, 0.5f)
            }) {
                Text("Flat")
            }
            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(onClick = {
                updateEqualizerBand(0, 1f)
                updateEqualizerBand(1, 1f)
                updateEqualizerBand(2, 1f)
                updateEqualizerBand(3, 1f)
                updateEqualizerBand(4, 1f)
            }) {
                Text("Bass Boot")
            }
            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(onClick = {
                updateEqualizerBand(0, 0f)
                updateEqualizerBand(1, 0.3f)
                updateEqualizerBand(2, 0.5f)
                updateEqualizerBand(3, 0.7f)
                updateEqualizerBand(4, 1f)
            }) {
                Text("Treble Boost")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            ElevatedButton(onClick = {  }) {
                Text("Save")
            }
        }
    }
}

