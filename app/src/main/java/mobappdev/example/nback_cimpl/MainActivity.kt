package mobappdev.example.nback_cimpl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import mobappdev.example.nback_cimpl.ui.screens.GameScreen
import mobappdev.example.nback_cimpl.ui.screens.HomeScreen
import mobappdev.example.nback_cimpl.ui.theme.NBack_CImplTheme
import mobappdev.example.nback_cimpl.ui.viewmodels.GameVM

class MainActivity : ComponentActivity() {

    private enum class Screen {
        Home,
        Game
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NBack_CImplTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val gameViewModel: GameVM = viewModel(
                        factory = GameVM.Factory
                    )

                    var currentScreen by remember { mutableStateOf(Screen.Home) }

                    when (currentScreen) {
                        Screen.Home -> HomeScreen(
                            vm = gameViewModel,
                            onStartGame = { currentScreen = Screen.Game }
                        )

                        Screen.Game -> GameScreen(
                            vm = gameViewModel,
                            onBackToHome = { currentScreen = Screen.Home }
                        )
                    }
                }
            }
        }
    }
}
