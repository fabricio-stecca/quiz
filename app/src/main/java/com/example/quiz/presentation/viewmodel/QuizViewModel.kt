package com.example.quiz.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.model.Question
import com.example.quiz.data.model.QuizSession
import com.example.quiz.data.repository.FirestoreQuestionRepository
import com.example.quiz.data.repository.FirestoreQuizSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class QuizUiState(
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, List<String>> = emptyMap(),
    val isLoading: Boolean = false,
    val isQuizCompleted: Boolean = false,
    val timeRemaining: Int = 300,
    val score: QuizScore? = null,
    val startTime: Long = 0L
)

data class QuizScore(
    val correctAnswers: Int,
    val totalQuestions: Int,
    val accuracy: Double,
    val totalPoints: Int,
    val timeSpent: Int
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    
    private val questionRepository = FirestoreQuestionRepository()
    private val sessionRepository = FirestoreQuizSessionRepository()
    
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun loadQuestions(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val questions = questionRepository.getQuestionsByCategory(category)
                _uiState.value = _uiState.value.copy(
                    questions = questions,
                    isLoading = false,
                    startTime = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun selectAnswer(questionIndex: Int, answers: List<String>) {
        val currentAnswers = _uiState.value.selectedAnswers.toMutableMap()
        currentAnswers[questionIndex] = answers
        _uiState.value = _uiState.value.copy(selectedAnswers = currentAnswers)
    }

    fun goToNextQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        val totalQuestions = _uiState.value.questions.size
        
        if (currentIndex < totalQuestions - 1) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = currentIndex + 1)
        }
    }

    fun goToPreviousQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        
        if (currentIndex > 0) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = currentIndex - 1)
        }
    }

    fun previousQuestion() = goToPreviousQuestion()
    

    fun nextQuestion() = goToNextQuestion()
    

    fun finishQuiz(userId: String, category: String) = completeQuiz(userId, category)

    fun updateTimer(newTime: Int) {
        _uiState.value = _uiState.value.copy(timeRemaining = newTime)
    }

    fun completeQuiz(userId: String, category: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val questions = state.questions
            val answers = state.selectedAnswers
            
            var correctCount = 0
            
            questions.forEachIndexed { index, question ->
                val selectedAnswers = answers[index] ?: emptyList()
                val correctAnswerText = question.options.getOrNull(question.correctAnswer) ?: ""
                if (selectedAnswers.contains(correctAnswerText)) {
                    correctCount++
                }
            }
            
            val accuracy = if (questions.isNotEmpty()) {
                (correctCount.toDouble() / questions.size) * 100
            } else 0.0
            

            val currentTime = System.currentTimeMillis()
            val timeSpent = if (state.startTime > 0) {
                ((currentTime - state.startTime) / 1000).toInt()
            } else {
                0
            }
            
            val totalPoints = calculatePoints(correctCount, questions.size, timeSpent)
            
            val score = QuizScore(
                correctAnswers = correctCount,
                totalQuestions = questions.size,
                accuracy = accuracy,
                totalPoints = totalPoints,
                timeSpent = timeSpent
            )
            

            val session = QuizSession(
                id = "",
                userId = userId,
                category = category,
                totalQuestions = questions.size,
                correctAnswers = correctCount,
                totalPoints = totalPoints,
                timeSpentSeconds = timeSpent,
                completedAt = Date()
            )
            
            sessionRepository.saveQuizSession(session)
            
            _uiState.value = _uiState.value.copy(
                isQuizCompleted = true,
                score = score
            )
        }
    }

    private fun calculatePoints(correct: Int, total: Int, timeSpent: Int): Int {
        val basePoints = correct * 10

        val avgTimePerQuestion = if (total > 0) timeSpent / total else timeSpent
        val timeBonus = if (avgTimePerQuestion < 30) {
            val bonus = (30 - avgTimePerQuestion) / 6
            (bonus * correct).coerceAtLeast(0)
        } else 0
        return basePoints + timeBonus
    }

    fun resetQuiz() {
        _uiState.value = QuizUiState()
    }
}
