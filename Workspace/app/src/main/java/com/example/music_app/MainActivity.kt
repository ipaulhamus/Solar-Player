package com.example.music_app

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.music_app.ui.theme.Blue
import com.example.music_app.ui.theme.LightBlue
import com.example.music_app.ui.theme.LightOrange
import com.example.music_app.ui.theme.Music_AppTheme
import com.example.music_app.ui.theme.NavyBlue
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    public var songList = mutableStateListOf<Triple<String, String, String>>()
    public var audioFiles = mutableListOf<String>()
    public var currentSongIndex = mutableStateOf(0)

    public var volume = mutableStateOf(1f) // Default volume set to 100%

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // MediaPlayer instance to be used throughout the app
            var mediaPlayer by remember { mutableStateOf(MediaPlayer()) }
            Music_AppTheme {
                //Navigation controller
                val screensController = rememberNavController()
                NavHost(screensController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            navController = screensController,
                            mediaPlayer = mediaPlayer,
                            songList = songList,
                            audioFiles = audioFiles,
                            currentSongIndex = currentSongIndex,
                            volume = volume
                        )
                    }
                    composable("details") {
                        DetailsScreen(
                            navController = screensController,
                            mediaPlayer = mediaPlayer,
                            songList = songList,
                            audioFiles = audioFiles,
                            currentSongIndex = currentSongIndex,
                            volume = volume
                    ) }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController, mediaPlayer: MediaPlayer,
               songList: MutableList<Triple<String, String, String>> = mutableListOf(), audioFiles: MutableList<String> = mutableListOf(),
                currentSongIndex: MutableState<Int>,
                volume: MutableState<Float>) {
    // Values to reference throughout the player
    val context = LocalContext.current
    val mediaMetadataRetriever = MediaMetadataRetriever()
    // Mutable variable for checking if the music is playing
    val isPlaying = remember { mutableStateOf(mediaPlayer.isPlaying) }

    LaunchedEffect(Unit) {

        if(mediaPlayer.isPlaying) {
            mediaPlayer.setVolume(volume.value, volume.value)
        }

        val assetManager = context.assets

        if(audioFiles.isNotEmpty()) {
            println("Audio files already loaded, skipping asset scan.")
            return@LaunchedEffect
        }

        try {
            val assetsFiles = assetManager.list("") ?: emptyArray()
            audioFiles.addAll(assetsFiles.filter {
                it.endsWith(".mp3") || it.endsWith(".wav")
            })

            println("Found ${audioFiles.size} audio files")

            audioFiles.forEach { file ->
                try {
                    println("Processing audio file: $file")
                    assetManager.openFd(file).use { descriptor ->
                        mediaMetadataRetriever.setDataSource(
                            descriptor.fileDescriptor,
                            descriptor.startOffset,
                            descriptor.length
                        )
                        val title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: file
                        val artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
                        songList.add(Triple(title, artist, file))
                        println("Added song: $title by $artist")
                    }
                } catch (e: Exception) {
                    println("Error processing file $file: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Error listing audio files: ${e.message}")
        } finally {
            mediaMetadataRetriever.release()
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyBlue)
                .padding(paddingValues)
        ) {
            BottomBanner(navController = navController,
                         songs = songList, currentSongIndex = currentSongIndex, isPlaying = isPlaying)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(NavyBlue)
                    .padding(bottom = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                SongList(
                    songs = songList,
                    audioFiles = audioFiles,
                    mediaPlayer = mediaPlayer,
                    modifier = Modifier.padding(16.dp),
                    currentSongIndex = currentSongIndex,
                    isPlaying = isPlaying
                )
            }
        }
    }
}

@Composable
fun TopBanner()
{
    Row(
        modifier = Modifier
            .zIndex(1f)
            .padding(bottom= 8.dp, start = 8.dp, end = 8.dp)
            .background(Color.DarkGray)
            .border(BorderStroke(1.dp, Color.LightGray), shape = RoundedCornerShape(8.dp)),
            horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Music App",
            color = Color.LightGray,
            modifier = Modifier
                .padding(start = 8.dp, bottom = 4.dp, top = 8.dp)
        )
    }
}

//Element of the song list
@Composable
fun SongListElement(
    title: String,
    artist: String,
    fileName: String,
    context: Context,
    modifier: Modifier = Modifier,
    audioFiles: List<String>,
    mediaPlayer: MediaPlayer,
    currentSongIndex: MutableState<Int>,
    isPlaying: MutableState<Boolean>
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1f)
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, LightBlue))
            .background(color = LightBlue),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title + "\n(" + artist + ")",
            color = NavyBlue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 8.dp, bottom = 4.dp, top = 8.dp, end = 1.dp)
                .weight(2f)
        )
        Button(
            modifier = Modifier
                .padding(8.dp),
            onClick = {
                isPlaying.value = true
                playSong(context, mediaPlayer, fileName, audioFiles, currentSongIndex)
                // Update the current song index to the one being played
                currentSongIndex.value = audioFiles.indexOfFirst { it.contains(fileName, ignoreCase = true) }
                println("New song index: ${audioFiles.indexOfFirst { it.contains(fileName, ignoreCase = true) }}")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = LightOrange,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Play",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

//Scrollable song list
@Composable
fun SongList(
    songs: List<Triple<String, String, String>>,
    modifier: Modifier = Modifier,
    audioFiles: List<String>,
    mediaPlayer: MediaPlayer,
    currentSongIndex: MutableState<Int>,
    isPlaying: MutableState<Boolean>
) {
    //Vertically scrollable list that only composes and lays out the currently visible items
    LazyColumn(modifier = modifier) {
        items(songs) { song ->
            SongListElement(
                title = song.first,
                artist = song.second,
                fileName = song.third,
                audioFiles = audioFiles,
                mediaPlayer = mediaPlayer,
                context = LocalContext.current,
                currentSongIndex = currentSongIndex,
                isPlaying = isPlaying,
            )
        }
    }
}

@Composable
fun BottomBanner(navController: NavController,
                 songs: MutableList<Triple<String, String, String>> = mutableListOf<Triple<String, String, String>>(),
                 currentSongIndex: MutableState<Int>,
                 isPlaying: MutableState<Boolean>) {
    Box(
        modifier = Modifier
            .zIndex(2f)
            .fillMaxSize()
            .padding(16.dp)
            .padding(bottom = 16.dp)
        .background(Color.Black.copy(alpha = 0f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color.LightGray), shape = RoundedCornerShape(12.dp))
                .fillMaxWidth()
                .background(Blue)
        ) {
            Column(
                modifier = Modifier
                    .size(240.dp, 110.dp)
                    .padding(bottom = 8.dp, top = 6.dp),
                horizontalAlignment = AbsoluteAlignment.Left,
                verticalArrangement = Arrangement.Center
            )
            {
                Text(
                    text = if (isPlaying.value) songs[currentSongIndex.value].first else "Select a song to play",
                    color = LightBlue,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .alpha(1f)
                        .padding(start = 8.dp, bottom = 4.dp, top = 4.dp)
                )
                Text(
                    text = if (isPlaying.value) songs[currentSongIndex.value].second else "No song selected",
                    color = LightOrange,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .alpha(0.9f)
                        .padding(start = 8.dp, bottom = 4.dp, top = 4.dp)
                )
            }
            if(isPlaying.value == true)
            {
                Button(
                    onClick = { navController.navigate("details") },

                    modifier = Modifier
                        .zIndex(2f)
                        .padding(16.dp)
                        .align(Alignment.CenterEnd),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightOrange,
                        contentColor = Color.White
                    ),
                ) {
                    Text(
                        text = "Details\nScreen",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

        }
    }

}

fun playSong(
    context: Context,
    mediaPlayer: MediaPlayer,
    songFileName: String,
    audioFiles: List<String>,
    currentSongIndex: MutableState<Int>
) {
    try {
        // First validate the index is within bounds and adjust if necessary
        if (currentSongIndex.value < 0 || currentSongIndex.value >= audioFiles.size) {
            currentSongIndex.value = 0
        }

        // Find and play the requested song
        audioFiles.firstOrNull { it.equals(songFileName, ignoreCase = true) }?.let { file ->
            val assetManager = context.assets
            println("Attempting to play song: $songFileName from file: $file")
            val afd = assetManager.openFd(file)
            mediaPlayer.reset()
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mediaPlayer.prepare()
            mediaPlayer.start()
            afd.close()

            mediaPlayer.setOnCompletionListener {
                println("Song completed: $songFileName")
                try {
                    // Calculate next index and validate it's within bounds

                    val nextIndex = (currentSongIndex.value + 1)
                    if (nextIndex < audioFiles.size) {
                        currentSongIndex.value = nextIndex
                        val nextSongFile = audioFiles[nextIndex]
                        playSong(context, mediaPlayer, nextSongFile, audioFiles, currentSongIndex)
                    } else {
                        // Reset to beginning of playlist
                        currentSongIndex.value = 0
                        val nextSongFile = audioFiles[0]
                        playSong(context, mediaPlayer, nextSongFile, audioFiles, currentSongIndex)
                    }
                } catch (e: Exception) {
                    println("Error during auto-play: ${e.message}")
                }
            }

            println("Playing song: $songFileName")
        } ?: throw Exception("Song file not found: $songFileName")
    } catch (e: Exception) {
        println("Error playing song: ${e.message}")
    }
}

