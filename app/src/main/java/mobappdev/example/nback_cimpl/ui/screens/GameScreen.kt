package mobappdev.example.nback_cimpl.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import java.util.Locale

@Composable
fun GameScreen(
    vm: GameViewModel,
    onBackToHome: () -> Unit
) {
    val state by vm.gameState.collectAsState()
    val score by vm.score.collectAsState()
    val totalEvents = vm.totalEvents

    val context = LocalContext.current

    val letters = remember {
        listOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H')
    }

    val tts = remember {
        var engine: TextToSpeech? = null

        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.US
            }
        }

        engine
    }

    LaunchedEffect(state.gameType, state.currentEventNumber) {
        if (state.gameType == GameType.Audio && state.eventValue >= 0) {
            val index = state.eventValue % letters.size
            val letterStr = letters[index].toString()
            tts?.speak(
                letterStr,
                TextToSpeech.QUEUE_FLUSH,
                null,
                System.currentTimeMillis().toString()
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    val title = when (state.gameType) {
        GameType.Audio -> "Audio ${vm.nBack}-back"
        GameType.Visual -> "Visual ${vm.nBack}-back"
        GameType.AudioVisual -> "Audio + Visual ${vm.nBack}-back"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Event ${state.currentEventNumber} / $totalEvents",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Correct responses: $score",
                style = MaterialTheme.typography.bodyLarge
            )

            when (state.gameType) {
                GameType.Visual -> {
                    Grid3x3(highlightedIndex = state.eventValue)
                }
                GameType.Audio -> {
                    val currentLetter =
                        if (state.eventValue >= 0) {
                            val index = state.eventValue % letters.size
                            letters[index].toString()
                        } else {
                            ""
                        }

                    Text(
                        text = currentLetter,
                        style = MaterialTheme.typography.displayLarge
                    )
                }
                GameType.AudioVisual -> {
                    Grid3x3(highlightedIndex = state.eventValue)
                }
            }

            val isRoundFinished =
                !state.isRunning && state.currentEventNumber >= totalEvents

            val buttonBorder =
                if (state.isErrorFeedback) BorderStroke(2.dp, Color.Red) else null

            if (!isRoundFinished) {
                Button(
                    onClick = { vm.checkMatch() },
                    border = buttonBorder,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(text = "MATCH")
                }
            } else {
                Button(
                    onClick = { vm.startGame() },
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("PLAY AGAIN")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = "Back to home")
            }
        }
    }
}

@Composable
private fun Grid3x3(highlightedIndex: Int) {
    val cellSize = 80.dp
    val shape = RoundedCornerShape(8.dp)

    Column(
        modifier = Modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in 0 until 3) {
            Row {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    val isHighlighted = index == highlightedIndex

                    val color =
                        if (isHighlighted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(cellSize)
                            .background(color = color, shape = shape)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    GameScreen(vm = FakeVM(), onBackToHome = {})
}
