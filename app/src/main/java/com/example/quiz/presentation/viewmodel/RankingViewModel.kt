package com.example.quiz.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.model.QuizSession
import com.example.quiz.data.model.User
import com.example.quiz.data.repository.FirestoreQuizSessionRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserRankingData(
    val userId: String,
    val nickname: String,
    val totalPoints: Int,
    val totalQuestions: Int,
    val totalQuizzes: Int,
    val averageAccuracy: Double
)

enum class RankingType {
    POINTS, QUESTIONS
}

class RankingViewModel(application: Application) : AndroidViewModel(application) {
    // Room database removido; agora dependemos somente do Firestore
    private val sessionRepository = FirestoreQuizSessionRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userRankings = MutableStateFlow<List<UserRankingData>>(emptyList())
    val userRankings: StateFlow<List<UserRankingData>> = _userRankings

    private val _selectedRankingType = MutableStateFlow(RankingType.POINTS)
    val selectedRankingType: StateFlow<RankingType> = _selectedRankingType

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun selectRankingType(type: RankingType) {
        _selectedRankingType.value = type
    }

    fun loadRankings() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Buscar todas as sessões do Firestore
                sessionRepository.getAllSessionsFlow().collect { allSessions ->
                    val userStats = mutableMapOf<String, UserRankingData>()
                    
                    // Agrupar sessões por usuário e calcular estatísticas
                    for (session in allSessions) {
                        val existing = userStats[session.userId]
                        if (existing == null) {
                            // Buscar nickname do usuário no Firestore
                            val userDoc = firestore.collection("users")
                                .document(session.userId)
                                .get()
                                .await()
                                
                            val nickname = if (userDoc.exists()) {
                                userDoc.getString("nickname") ?: "Usuario"
                            } else {
                                "Usuario"
                            }
                            
                            userStats[session.userId] = UserRankingData(
                                userId = session.userId,
                                nickname = nickname,
                                totalPoints = session.totalPoints,
                                totalQuestions = session.totalQuestions,
                                totalQuizzes = 1,
                                averageAccuracy = session.accuracy
                            )
                        } else {
                            val totalQuizzes = existing.totalQuizzes + 1
                            val newAverageAccuracy = ((existing.averageAccuracy * existing.totalQuizzes) + session.accuracy) / totalQuizzes
                            
                            userStats[session.userId] = existing.copy(
                                totalPoints = existing.totalPoints + session.totalPoints,
                                totalQuestions = existing.totalQuestions + session.totalQuestions,
                                totalQuizzes = totalQuizzes,
                                averageAccuracy = newAverageAccuracy
                            )
                        }
                    }
                    
                    _userRankings.value = userStats.values.toList()
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _userRankings.value = emptyList()
                _isLoading.value = false
            }
        }
    }
}
