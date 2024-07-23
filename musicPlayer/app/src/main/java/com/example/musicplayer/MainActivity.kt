package com.example.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
                PlaylistDetailScreen(playlistId, Modifier.padding(innerPadding))
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
fun PlaylistDetailScreen(playlistId: Int, modifier: Modifier = Modifier) {
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
                    leadingContent = { Icon(Icons.Filled.MusicNote, contentDescription = null) }
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