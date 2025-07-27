package com.example.music_app

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.lights.Light
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.example.music_app.ui.theme.Beige
import com.example.music_app.ui.theme.Blue
import com.example.music_app.ui.theme.LightBlue
import com.example.music_app.ui.theme.LightOrange
import com.example.music_app.ui.theme.NavyBlue

@Composable
fun DetailsScreen(
    navController: NavHostController, mediaPlayer: MediaPlayer,
    audioFiles: List<String>, songList: List<Triple<String, String, String>>,
    currentSongIndex: MutableState<Int>,
    volume: MutableState<Float>) {

    var songProgress by remember { mutableStateOf(0) }
    var isPaused = remember { mutableStateOf(false) }

    val songLength = mediaPlayer.duration
    val context = LocalContext.current

    println("DetailsScreen: currentSongIndex = $currentSongIndex")

    LaunchedEffect(Unit) {
        try {
            while (true) {
                if (mediaPlayer.isPlaying) {
                    songProgress = fetchSongProgress(mediaPlayer)
                }
                kotlinx.coroutines.delay(100)
            }
        } catch (e: Exception) {
            println("Progress tracking stopped: ${e.message}")
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBlue)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .height(800.dp)
                .width(700.dp)
                .background(LightBlue, shape = RoundedCornerShape(16.dp))
                .padding(16.dp),

        )
        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Now Playing:",
                color = Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 6.dp, start = 16.dp, end = 16.dp)
                    .zIndex(1f), // Ensure text is above the background
            )
            Text(
                text = "${songList[currentSongIndex.value].first}",
                color = NavyBlue,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .zIndex(1f) // Ensure text is above the background
            )
            Text(
                text = "${songList[currentSongIndex.value].second}",
                color = Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .zIndex(1f) // Ensure text is above the background
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = NavyBlue,
                thickness = 3.dp
            )
            ProgressSlider(
                progress = songProgress,
                onProgressChange = { newProgress ->
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.seekTo(newProgress)
                    }
                },
                valueRange = 0f..songLength.toFloat()
            )
            ControlButtons(
                mediaPlayer = mediaPlayer,
                audioFiles = audioFiles,
                currentSongIndex = currentSongIndex,
                context = context,
                isPaused = isPaused
            )
            VolumeSlider(
                volume = volume.value,
                onVolumeChange = { newVolume ->
                    volume.value = newVolume
                    mediaPlayer.setVolume(volume.value, volume.value)
                }

            )
            Text(
                text = if (currentSongIndex.value >= songList.size - 1) "Next Up:\n${songList[0].first}" else "Next Up:\n${songList[currentSongIndex.value + 1].first}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = NavyBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                color = NavyBlue,
                thickness = 3.dp
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (mediaPlayer.isLooping) {
                            mediaPlayer.isLooping = false
                        } else {
                            mediaPlayer.isLooping = true
                        }
                    },
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .width(180.dp)
                        .height(50.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue,
                        contentColor = Color.White
                    ),
                ) {
                    Text(if (mediaPlayer.isLooping) "List Playback" else "Loop Song",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { navController.navigate("home") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                )
                {
                    Text(text = "Back",
                        color = Color.White,
                        modifier = Modifier
                            .zIndex(1f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ProgressSlider(
    progress: Int,
    onProgressChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Slider(
        value = progress.toFloat(),
        onValueChange = { onProgressChange(it.toInt()) },
        valueRange = valueRange,
        modifier = Modifier
            .padding(top = 16.dp, bottom = 8.dp),
        colors = SliderDefaults.colors(
            thumbColor = NavyBlue,
            activeTrackColor = LightOrange,
            inactiveTrackColor = Beige
        )
    )
    Text(
        //Using a time formatted string to display the song progress in minutes and seconds
        // Divided by 60000 for min and 1000 for seconds
        text = String.format("Song Progress: %d:%02d / %d:%02d",
            progress / 60000, (progress / 1000) % 60,
            (valueRange.endInclusive.toInt() / 60000), (valueRange.endInclusive.toInt() / 1000) % 60),
        color = Blue,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 10.dp, bottom = 12.dp, start = 16.dp)
    )
}

@Composable
fun VolumeSlider(
    volume: Float,
    onVolumeChange: (Float) -> Unit
) {
    Slider(
        value = volume,
        onValueChange = onVolumeChange,
        valueRange = 0f..1f,
        modifier = Modifier
            .width(150.dp)
            .padding(horizontal = 16.dp),
        colors = SliderDefaults.colors(
            thumbColor = LightOrange,
            activeTrackColor = Blue,
            inactiveTrackColor = Beige
        )
    )
    Text(
        text = String.format("Volume: %.0f%%", volume * 100),
        color = Blue,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun ControlButtons(mediaPlayer: MediaPlayer,
                   audioFiles: List<String>,
                   currentSongIndex: MutableState<Int>,
                   context: Context,
                   isPaused: MutableState<Boolean>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Button(
            onClick = { mediaPlayer.seekTo(0) },
            colors = ButtonDefaults.buttonColors(
                containerColor = LightOrange,
                contentColor = Color.White
            ),
        ) {
            Text(
                text = "<<",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Button(onClick = {
            if (mediaPlayer.isPlaying)
            {
                isPaused.value = true
                mediaPlayer.pause()
                println("Pausing song: ${audioFiles[currentSongIndex.value]}")
            } else {
                isPaused.value = false
                mediaPlayer.start()
                println("Resuming song: ${audioFiles[currentSongIndex.value]}")
            }
                         },
            colors = ButtonDefaults.buttonColors(
                containerColor = LightOrange,
                contentColor = Color.White
            ),
        ) {
            Text(if (isPaused.value) "Play" else "Pause",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
                )
        }
        Button(onClick = {
            val nextIndex = (currentSongIndex.value + 1) % audioFiles.size
            println("Current song index after next before function: $currentSongIndex")
            currentSongIndex.value = nextIndex
            playNextSong(mediaPlayer, audioFiles, currentSongIndex, context)
            println("Current song index after next: $nextIndex")
        },
            colors = ButtonDefaults.buttonColors(
                containerColor = LightOrange,
                contentColor = Color.White
            ),

        ) {
            Text(
                text = ">>",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
                )
        }
    }
}

fun playNextSong(
    mediaPlayer: MediaPlayer,
    audioFiles: List<String>,
    currentSongIndex: MutableState<Int>,
    context: Context
) {
    try {
        println("Playing next song. Current index: ${currentSongIndex.value}")
        val nextSong = audioFiles[currentSongIndex.value]
        println("Next audio file: $nextSong")
        val assetManager = context.assets
        val afd = assetManager.openFd(nextSong)
        mediaPlayer.reset()
        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        mediaPlayer.prepare()
        mediaPlayer.start()

        //OnComplete listener to play next song automatically from this function as well
        mediaPlayer.setOnCompletionListener {
            println("Song completed: $nextSong")
            var nextSongFile = ""

            //Set the next song file based on the current index
            if(currentSongIndex.value + 1 < audioFiles.size - 1) {
                nextSongFile = audioFiles[currentSongIndex.value + 1]
            } else {
                nextSongFile = audioFiles[0]
            }

            // Play next song in the list if available
            if (currentSongIndex.value + 1 < audioFiles.size - 1) {
                playSong(context, mediaPlayer, nextSongFile, audioFiles, currentSongIndex)
            } else {
                println("No more songs to play.")
            }

            // Reset the current song index to 0 if it reaches the end
            if (currentSongIndex.value >= audioFiles.size - 1) {
                currentSongIndex.value = 0

                println("Resetting to first song: ${currentSongIndex.value}")
                playSong(context, mediaPlayer, nextSongFile, audioFiles, currentSongIndex)
            } else {
                currentSongIndex.value += 1
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("Error playing next song: ${e.message}")
    }
}

fun fetchSongProgress(mediaPlayer: MediaPlayer): Int {
    return if (mediaPlayer.isPlaying) {
        mediaPlayer.currentPosition
    } else {
        0
    }
}