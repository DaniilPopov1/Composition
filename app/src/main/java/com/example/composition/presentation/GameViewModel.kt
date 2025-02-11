package com.example.composition.presentation

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.composition.R
import com.example.composition.data.GameRepositoryImpl
import com.example.composition.domain.entity.GameResult
import com.example.composition.domain.entity.GameSettings
import com.example.composition.domain.entity.Level
import com.example.composition.domain.entity.Question
import com.example.composition.domain.usecases.GenerateQuestionUseCase
import com.example.composition.domain.usecases.GetGameSettingsUseCase

class GameViewModel(
    private val application: Application,
    private val level: Level
): ViewModel() {

    private val repository = GameRepositoryImpl

    private val generateQuestionUseCase = GenerateQuestionUseCase(repository)
    private val getGameSettingsUseCase = GetGameSettingsUseCase(repository)

    private lateinit var gameSettings: GameSettings

    private var timer: CountDownTimer? = null

    private val _formattedTime = MutableLiveData<String>()
    val formattedTime: LiveData<String>
        get() = _formattedTime

    private val _question = MutableLiveData<Question>()
    val question: LiveData<Question>
        get() = _question

    private val _percentOfRightAnswer = MutableLiveData<Int>()
    val percentOfRightAnswer: LiveData<Int>
        get() = _percentOfRightAnswer

    private val _progressAnswers = MutableLiveData<String>()
    val progressAnswers: LiveData<String>
        get() = _progressAnswers

    private val _enoughCountOfRightAnswers = MutableLiveData<Boolean>()
    val enoughCountOfRightAnswers: LiveData<Boolean>
        get() = _enoughCountOfRightAnswers

    private val _enoughPercentOfRightAnswers = MutableLiveData<Boolean>()
    val enoughPercentOfRightAnswers: LiveData<Boolean>
        get() = _enoughPercentOfRightAnswers

    private val _minPercent = MutableLiveData<Int>()
    val minPercent: LiveData<Int>
        get() = _minPercent

    private val _gameResult = MutableLiveData<GameResult>()
    val gameResult: LiveData<GameResult>
        get() = _gameResult

    private var countOfRightAnswers = 0
    private var countOfQuestions = 0

    init {
        startGame()
    }

    private fun startGame(){
        getGameSettings()
        startTimer()
        generateQuestion()
        updateProgress()
    }

    private fun startTimer(){
        timer = object : CountDownTimer(
            gameSettings.gameTimeInSeconds * MILLIS_IN_SECONDS,
            MILLIS_IN_SECONDS
        ){
            override fun onFinish() {
                finishGame()
            }

            override fun onTick(millisUntilFinished: Long) {
                _formattedTime.value = formatTime(millisUntilFinished)
            }
        }
        timer?.start()
    }

    private fun formatTime(millisUntilFinished: Long): String{
        val seconds = millisUntilFinished / MILLIS_IN_SECONDS
        val minutes = seconds / SECONDS_IN_MINUTE
        val leftSeconds = seconds - (minutes * SECONDS_IN_MINUTE)
        return String.format("%02d:%02d", minutes, leftSeconds)
    }

    private fun finishGame(){
        _gameResult.value = GameResult(
            enoughCountOfRightAnswers.value == true && enoughPercentOfRightAnswers.value == true,
            countOfRightAnswers,
            countOfQuestions,
            gameSettings
        )
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
    
    private fun getGameSettings(){
        this.gameSettings = getGameSettingsUseCase(level)
        _minPercent.value = gameSettings.minPercentOfRightAnswers
    }

    private fun generateQuestion(){
        _question.value = generateQuestionUseCase(gameSettings.maxSumValue)
    }

    fun chooseAnswer(number: Int) {
        checkAnswer(number)
        updateProgress()
        generateQuestion()
    }

    private fun checkAnswer(number: Int){
        val rightAnswer = question.value?.rightAnswer
        if (number == rightAnswer){
            countOfRightAnswers++
        }
        countOfQuestions++
    }

    private fun updateProgress(){
        val percent = calculatePercentOfRightAnswers()
        _percentOfRightAnswer.value = percent
        _progressAnswers.value = String.format(
            application.resources.getString(R.string.progress_answers),
            countOfRightAnswers.toString(),
            gameSettings.minCountOfRightAnswers.toString()
        )
        _enoughCountOfRightAnswers.value = countOfRightAnswers >= gameSettings.minCountOfRightAnswers
        _enoughPercentOfRightAnswers.value = percent >= gameSettings.minPercentOfRightAnswers
    }

    private fun calculatePercentOfRightAnswers():Int{
        if (countOfQuestions == 0){
            return 0
        }
        return (countOfRightAnswers / countOfQuestions.toDouble() * 100).toInt()
    }

    companion object{
        private const val MILLIS_IN_SECONDS = 1000L
        private const val SECONDS_IN_MINUTE = 60
    }
}