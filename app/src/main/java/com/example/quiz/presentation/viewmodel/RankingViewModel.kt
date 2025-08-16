package com.example.quiz.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.database.QuizDatabase
import com.example.quiz.data.model.QuizSession
import com.example.quiz.data.model.User
import com.example.quiz.data.repository.FirestoreQuizSessionRepository
import com.example.quiz.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RankingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = QuizDatabase.getDatabase(application)
    private val sessionRepository = FirestoreQuizSessionRepository()
    private val userRepository = UserRepository(database.userDao())

    private val _topScores = MutableStateFlow<List<QuizSession>>(emptyList())
    val topScores: StateFlow<List<QuizSession>> = _topScores

    private val _topUsers = MutableStateFlow<List<User>>(emptyList())
    val topUsers: StateFlow<List<User>> = _topUsers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadRankings() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // For now, just show empty lists until we implement global rankings
            _topScores.value = emptyList()
            _topUsers.value = emptyList()
            
            _isLoading.value = false
        }
    }
}
