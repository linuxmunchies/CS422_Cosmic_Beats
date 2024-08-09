package com.example.musicplayer

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
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

        val prefs = getSharedPreferences("com.musicPlayer.app", MODE_PRIVATE)

        mediaPlayer = MediaPlayer()

        setContent {
            MusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MusicPlayerApp(mediaPlayer,equalizer, prefs)
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
fun loadSongsIntoDatabase(): MutableList<Song> {

    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val db = SongDatabase.getDatabase(context)
    val songDao = db.songDao()

    //Delete all songs
    runOnIO {
        songDao.deleteSongAll()
    }
    //horrible hack for testing to pause to make sure we deleted everything
    Thread.sleep(2000)

    //Get the list ready to return once it is filled
    var songList: MutableList<Song> = mutableListOf()

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
            songList.add(Song(id, title, artist, duration, contentUri.toString()))
        }
    }
    return songList
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
fun MusicPlayerApp(mediaPlayer: MediaPlayer, equalizer: Equalizer?, prefs: SharedPreferences) {
    val navController = rememberNavController()
    val context = LocalContext.current

    var songsInDatabase: MutableList<Song> = mutableListOf()
    val savedVersion = prefs.getLong("savedVersion", 0)

    //Load songs into our database, checking if any new songs have been added
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //If our version of Android supports it, check if there's been updates to the MediaStore
        val mediaVersion = MediaStore.getGeneration(context,MediaStore.getExternalVolumeNames(context).elementAt(0).toString())
        if (mediaVersion == savedVersion){
            //mediaState is up to date, just get the list of songs
            songsInDatabase = getSongsFromDatabase()
        } else {
            //Our database is out of date, rebuild it!
            songsInDatabase = loadSongsIntoDatabase()
            //horrible waiting hack since we don't have time to properly deal with threads
            Thread.sleep(2000)
            prefs.edit().putLong("savedVersion", mediaVersion).apply()
        }
    } else {
        //We can't check if we are out of date in this case, so always rebuild
        songsInDatabase = loadSongsIntoDatabase()
        //horrible waiting hack since we don't have time to properly deal with threads
        Thread.sleep(2000)
    }

    //state for current song
    var currentSongIndex by remember { mutableStateOf(0) }

    val equalizer = Equalizer(1, mediaPlayer.audioSessionId)

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
                        when (playlistId) {
                            0 -> songsInDatabase.sortBy { it.title }
                            1 -> songsInDatabase.sortBy { it.artist }
                            2 -> songsInDatabase.sortByDescending { it.duration }
                            3 -> songsInDatabase.sortBy { it.duration }
                            4 -> songsInDatabase.sortBy { it.id }
                            else -> { // panic, we are out of range
                            }
                        }
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
                        playSongFromUri(context, mediaPlayer, uri)
                        equalizer.apply {
                            setEnabled(true)
                        }
                        mediaPlayer.start()
                        navController.navigate("songDetail/$songTitle/$artistName")
                        val songIndex = songsInDatabase.indexOfFirst { it.title == songTitle && it.artist == artistName }
                        if (songIndex >= 0) {
                            currentSongIndex = songIndex
                        }
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

//For getting songs without adding them to the database
@Composable
fun getSongsFromDatabase(): MutableList<Song> {
    val context = LocalContext.current

    val db = SongDatabase.getDatabase(context)
    val songDao = db.songDao()
    var songList: MutableList<Song> = mutableListOf()

    runOnIO {
        songList = songDao.getAllSongs().toMutableList()
    }
    Thread.sleep(1000)
    return songList
}

@Composable
fun PlaylistsScreen(onPlaylistClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    val playlists = remember {
        listOf(
            "Alphabetical (Title)",
            "Alphabetical (Artist)",
            "Duration (Longest)",
            "Duration (Shortest)",
            "Song ID"
        )
    }

    LazyColumn(modifier) {
        items(playlists.withIndex().toList()) { (index, playlist) ->
            ListItem(
                headlineContent = { Text(playlist, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                //supportingContent = { Text("${playlist.second} songs", maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
            //"Playlist ${playlistId + 1}"
            when (playlistId) {
                0 -> "Alphabetical (Title)"
                1 -> "Alphabetical (Artist)"
                2 -> "Duration (Longest)"
                3 -> "Duration (Shortest)"
                4 -> "Song ID"
                else -> { // panic, we are out of range
                    "Unknown"
                }
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(data) { (id, title, artist, duration, uri) ->
                ListItem(
                    headlineContent = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    supportingContent = {
                        Row {
                            Text(artist, maxLines = 1, overflow = TextOverflow.Ellipsis);
                            Spacer(Modifier.weight(1f));
                            Text(formatDuration(duration), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    leadingContent = { Icon(Icons.Filled.MusicNote, contentDescription = null) },
                    modifier = Modifier.clickable { onSongClick(title, artist, uri) }
                )
                HorizontalDivider()
            }
        }
    }
}

fun formatDuration(duration: Float) :String {
    val totalSeconds = (duration / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%01d:%02d", minutes, seconds)
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

    //There's a crash on first time startup since there's no songs in the list to grab a title from.
    //We stop that here!
    var noSongs = true
    if (songList.size != 0) {
        noSongs = false
    }

    BottomAppBar(
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (!noSongs) {
                            val newSongIndex =
                                (currentSongIndex - 1 + songList.size) % songList.size
                            onSongChange(newSongIndex)
                            isPlaying = true
                        }
                    }) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Skip to previous")
                    }
                    IconButton(onClick = {
                        //Check to make sure we are playing something to avoid crashing
                        if (!noSongs) {
                            isPlaying = !isPlaying
                            if (isPlaying) {
                                mediaPlayer.start()
                            } else {
                                mediaPlayer.pause()
                            }
                        }
                    }) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }
                    IconButton(onClick = {
                        if (!noSongs) {
                            val newSongIndex = (currentSongIndex + 1) % songList.size
                            onSongChange(newSongIndex)
                            isPlaying = true
                        }
                    }) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Skip to next")
                    }
                }
                Column(modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)) {
                    Text(
                        if (noSongs) {
                            "No track playing"
                        } else {
                            songList[currentSongIndex].title
                        },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (noSongs) {
                            "No track playing"
                        } else {
                            songList[currentSongIndex].artist
                        },
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
fun SongDetailScreen(mediaPlayer: MediaPlayer,equalizer: Equalizer, songTitle: String, artistName: String, modifier: Modifier = Modifier) {

    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0.5f) }
    var sliderPosition1 by remember { mutableStateOf(0.5f) }
    var sliderPosition2 by remember { mutableStateOf(0.5f) }
    var sliderPosition3 by remember { mutableStateOf(0.5f) }
    var sliderPosition4 by remember { mutableStateOf(0.5f) }

    fun updateEqualizerBand(band: Short, level: Float) {
        equalizer.let {
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
                    sliderPosition1 = it
                    updateEqualizerBand(1, it)
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
                    sliderPosition2 = it
                    updateEqualizerBand(2, it)
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
                    sliderPosition3 = it
                    updateEqualizerBand(3, it)
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
                sliderPosition = 0.5f
                sliderPosition1 = 0.5f
                sliderPosition2 = 0.5f
                sliderPosition3 = 0.5f
                sliderPosition4 = 0.5f
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
                sliderPosition = 1f
                sliderPosition1 = 1f
                sliderPosition2 = 1f
                sliderPosition3 = 1f
                sliderPosition4 = 1f
            }) {
                Text("Bass Boost")
            }
            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(onClick = {
                updateEqualizerBand(0, 0f)
                updateEqualizerBand(1, 0.3f)
                updateEqualizerBand(2, 0.5f)
                updateEqualizerBand(3, 0.7f)
                updateEqualizerBand(4, 1f)
                sliderPosition = 0f
                sliderPosition1 = 0.3f
                sliderPosition2 = 0.5f
                sliderPosition3 = 0.7f
                sliderPosition4 = 1f
            }) {
                Text("Treble Boost")
            }
        }
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center,
//            verticalAlignment = Alignment.CenterVertically
//        ){
//            ElevatedButton(onClick = {  }) {
//                Text("Save")
//            }
//        }
    }
}

