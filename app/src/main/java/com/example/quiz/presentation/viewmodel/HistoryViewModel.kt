package com.example.quiz.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.model.QuizSession
import com.example.quiz.data.repository.FirestoreQuizSessionRepository
import com.example.quiz.data.repository.SessionStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionRepository = FirestoreQuizSessionRepository()

    private val _sessions = MutableStateFlow<List<QuizSession>>(emptyList())
    val sessions: StateFlow<List<QuizSession>> = _sessions

    private val _stats = MutableStateFlow<SessionStats?>(null)
    val stats: StateFlow<SessionStats?> = _stats

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadUserHistory(userId: String) {
        Log.d("HistoryViewModel", "Loading history for userId: $userId")
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                launch {
                    sessionRepository.getUserSessionsFlow(userId).collect { sessionList ->
                        Log.d("HistoryViewModel", "Received ${sessionList.size} sessions")
                        _sessions.value = sessionList
                    }
                }
                

                launch {
                    sessionRepository.getUserStats(userId).collect { sessionStats ->
                        Log.d("HistoryViewModel", "Received stats: $sessionStats")
                        _stats.value = sessionStats
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error loading user history", e)
                _isLoading.value = false
            }
        }
    }

    fun loadUserHistoryByCategory(userId: String, category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            

            sessionRepository.getUserSessionsFlow(userId).collect { sessionList ->
                val filteredSessions = sessionList.filter { it.category == category }
                _sessions.value = filteredSessions
            }
            
            _isLoading.value = false
        }
    }
}
