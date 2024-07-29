package com.example.musicplayer

import android.R.attr.onClick
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.musicplayer.ui.theme.MusicPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MusicPlayerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerApp() {
    val navController = rememberNavController()

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
        bottomBar = { BottomPlayerBar() }
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
                    onSongClick = { songTitle, artistName ->
                        navController.navigate("songDetail/$songTitle/$artistName")
                    },
                    Modifier.padding(innerPadding)
                )
            }
            composable(
                "songDetail/{songTitle}/{artistName}",
                arguments = listOf(
                    navArgument("songTitle") { type = NavType.StringType },
                    navArgument("artistName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val songlistId = backStackEntry.arguments?.getInt("songlistId") ?: 0
                val songTitle = backStackEntry.arguments?.getString("songTitle") ?: ""
                val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
                SongDetailScreen(
                    songlistId,
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
    onSongClick: (String, String) -> Unit,
    modifier: Modifier = Modifier) {
    val songs = remember {
        listOf(
            "No Scrubs" to "TLC",
            "Took a Turn" to "Loukeman",
            "I'm God" to "Clams Casino",
            "Break It Off" to "PinkPantheress",
            "Pushin' Keys" to "DJ Swisherman",
            "Maximum Style" to "Tom And Jerry",
            "Andreaen Sand Dunes" to "Drexciya",
            "In 2 Minds" to "Kromestar",
            "Pull Over" to "Trina",
            "Massage Situation" to "Flying Lotus",
            "Ponto Suspeito" to "Lokowat",
            "Sonic Boom" to "SEGA SOUND TEAM",
            "Mutate and Survive" to "Oliver Ho",
            "minipops67" to "Aphex Twin",
            "BEAT THE POLICE" to "Adolf Nomura"
        )
    }

    Column(modifier) {
        Text(
            "Playlist ${playlistId + 1}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(songs) { (title, artist) ->
                ListItem(
                    headlineContent = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    supportingContent = { Text(artist, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingContent = { Icon(Icons.Filled.MusicNote, contentDescription = null) },
                    modifier = Modifier.clickable { onSongClick(title, artist) }
                )
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomPlayerBar() {
    var isPlaying by remember { mutableStateOf(false) }

    BottomAppBar(
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Skip to previous")
                    }
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Skip to next")
                    }
                }
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        "Currently Playing Song",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Artist Name",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}


@Composable
fun SongDetailScreen(songlistId: Int, songTitle: String, artistName: String, modifier: Modifier = Modifier) {
    var isPlaying by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0f) }
    var sliderPosition1 by remember { mutableStateOf(0f) }
    var sliderPosition2 by remember { mutableStateOf(0f) }
    var sliderPosition3 by remember { mutableStateOf(0f) }
    var sliderPosition4 by remember { mutableStateOf(0f) }
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
                onValueChange = { sliderPosition = it },
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
                onValueChange = { sliderPosition1 = it },
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
                onValueChange = { sliderPosition2 = it },
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
                onValueChange = { sliderPosition3 = it },
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
                onValueChange = { sliderPosition4 = it },
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
//            IconButton(onClick = { /* Previous song action */ }) {
//                Icon(Icons.Filled.SkipPrevious, contentDescription = "Skip to previous")
//            }
//            IconButton(onClick = { isPlaying = !isPlaying }) {
//                Icon(
//                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
//                    contentDescription = if (isPlaying) "Pause" else "Play"
//                )
//            }
//            IconButton(onClick = { /* Next song action */ }) {
//                Icon(Icons.Filled.SkipNext, contentDescription = "Skip to next")
//            }
            ElevatedButton(onClick = {  }) {
                Text("Flat")
            }
            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(onClick = {  }) {
                Text("Bass Boot")
            }
            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(onClick = {  }) {
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

