package com.example.quiz.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.database.QuizDatabase
import com.example.quiz.data.model.QuizSession
import com.example.quiz.data.repository.FirestoreQuizSessionRepository
import com.example.quiz.data.repository.SessionStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = QuizDatabase.getDatabase(application)
        private val sessionRepository = FirestoreQuizSessionRepository()

    private val _sessions = MutableStateFlow<List<QuizSession>>(emptyList())
    val sessions: StateFlow<List<QuizSession>> = _sessions

    private val _stats = MutableStateFlow<SessionStats?>(null)
    val stats: StateFlow<SessionStats?> = _stats

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadUserHistory(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Load sessions
            sessionRepository.getUserSessionsFlow(userId).collect { sessionList ->
                _sessions.value = sessionList
            }
            
            // Load stats
            sessionRepository.getUserStatsFlow(userId).collect { sessionStats ->
                _stats.value = sessionStats
            }
            
            _isLoading.value = false
        }
    }

    fun loadUserHistoryByCategory(userId: String, category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Use general user sessions for now - can be filtered later
            sessionRepository.getUserSessionsFlow(userId).collect { sessionList ->
                val filteredSessions = sessionList.filter { it.category == category }
                _sessions.value = filteredSessions
            }
            
            _isLoading.value = false
        }
    }
}
