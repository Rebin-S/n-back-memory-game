package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>

    val nBack: Int
    val totalEvents: Int
    val eventIntervalMillis: Long

    fun setGameType(gameType: GameType)
    fun startGame()
    fun checkMatch()
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
) : GameViewModel, ViewModel() {

    override val nBack: Int = 2
    override val totalEvents: Int = 20
    override val eventIntervalMillis: Long = 2000L

    private val visualCombinations: Int = 9
    private val audioLetterCount: Int = 8
    private val matchPercentage: Int = 30

    private val nBackHelper = NBackHelper()

    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int> = _score.asStateFlow()

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int> = _highscore.asStateFlow()

    private var job: Job? = null

    private var events: IntArray = intArrayOf()
    private var currentIndex: Int = -1
    private var hasAnsweredCurrent: Boolean = false

    override fun setGameType(gameType: GameType) {
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {
        job?.cancel()

        _score.value = 0
        currentIndex = -1
        hasAnsweredCurrent = false

        _gameState.value = _gameState.value.copy(
            eventValue = -1,
            currentEventNumber = 0,
            isErrorFeedback = false,
            isRunning = true
        )

        val combinations = when (gameState.value.gameType) {
            GameType.Audio -> audioLetterCount
            GameType.Visual,
            GameType.AudioVisual -> visualCombinations
        }

        events = nBackHelper
            .generateNBackString(totalEvents, combinations, matchPercentage, nBack)

        Log.d("GameVM", "Generated sequence: ${events.contentToString()}")

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame()
                GameType.Visual -> runVisualGame()
                GameType.AudioVisual -> runAudioVisualGame()
            }

            _gameState.value = _gameState.value.copy(isRunning = false)

            if (_score.value > _highscore.value) {
                _highscore.value = _score.value
            }
        }
    }

    override fun checkMatch() {
        if (currentIndex < nBack) return
        if (hasAnsweredCurrent) return

        hasAnsweredCurrent = true

        val isMatch = events[currentIndex] == events[currentIndex - nBack]

        if (isMatch) {
            _score.value = _score.value + 1
            _gameState.value = _gameState.value.copy(isErrorFeedback = false)
        } else {
            _gameState.value = _gameState.value.copy(isErrorFeedback = true)
            viewModelScope.launch {
                delay(300)
                _gameState.value = _gameState.value.copy(isErrorFeedback = false)
            }
        }
    }

    private suspend fun runAudioGame() {
        for ((index, value) in events.withIndex()) {
            currentIndex = index
            hasAnsweredCurrent = false

            _gameState.value = _gameState.value.copy(
                eventValue = value,
                currentEventNumber = index + 1,
                isErrorFeedback = false
            )

            delay(eventIntervalMillis)
        }
    }

    private suspend fun runVisualGame() {
        for ((index, value) in events.withIndex()) {
            currentIndex = index
            hasAnsweredCurrent = false

            _gameState.value = _gameState.value.copy(
                eventValue = value,
                currentEventNumber = index + 1,
                isErrorFeedback = false
            )

            delay(eventIntervalMillis)
        }
    }

    private suspend fun runAudioVisualGame() {
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }

    init {
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}


enum class GameType {
    Audio,
    Visual,
    AudioVisual
}

data class GameState(
    val gameType: GameType = GameType.Visual,
    val eventValue: Int = -1,
    val currentEventNumber: Int = 0,
    val isErrorFeedback: Boolean = false,
    val isRunning: Boolean = false
)

class FakeVM : GameViewModel {
    override val gameState: StateFlow<GameState> =
        MutableStateFlow(GameState()).asStateFlow()

    override val score: StateFlow<Int> =
        MutableStateFlow(2).asStateFlow()

    override val highscore: StateFlow<Int> =
        MutableStateFlow(42).asStateFlow()

    override val nBack: Int = 2
    override val totalEvents: Int = 20
    override val eventIntervalMillis: Long = 2000L

    override fun setGameType(gameType: GameType) {}
    override fun startGame() {}
    override fun checkMatch() {}
}
