package com.example.quiz.data.repository

import com.example.quiz.data.model.QuizSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date

data class SessionStats(
    val totalSessions: Int,
    val totalPoints: Int,
    val averageAccuracy: Double
)

class FirestoreQuizSessionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val sessionsCollection = firestore.collection("quiz_sessions")
    private val usersCollection = firestore.collection("users")

    suspend fun saveQuizSession(session: QuizSession) {
        try {
            val sessionData = hashMapOf(
                "userId" to session.userId,
                "category" to session.category,
                "totalQuestions" to session.totalQuestions,
                "correctAnswers" to session.correctAnswers,
                "totalPoints" to session.totalPoints,
                "timeSpentSeconds" to session.timeSpentSeconds,
                "completedAt" to com.google.firebase.Timestamp(session.completedAt)
            )
            
            sessionsCollection.add(sessionData).await()
            
            // Atualiza as estatísticas do usuário
            updateUserStats(session.userId)
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun getUserSessionsFlow(userId: String): Flow<List<QuizSession>> = flow {
        try {
            val querySnapshot = sessionsCollection
                .whereEqualTo("userId", userId)
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val sessions = querySnapshot.documents.mapNotNull { document ->
                try {
                    val timestamp = document.getTimestamp("completedAt")
                    QuizSession(
                        id = document.id,
                        userId = document.getString("userId") ?: "",
                        category = document.getString("category") ?: "",
                        totalQuestions = document.getLong("totalQuestions")?.toInt() ?: 0,
                        correctAnswers = document.getLong("correctAnswers")?.toInt() ?: 0,
                        totalPoints = document.getLong("totalPoints")?.toInt() ?: 0,
                        timeSpentSeconds = document.getLong("timeSpentSeconds")?.toInt() ?: 0,
                        completedAt = timestamp?.toDate() ?: Date()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            emit(sessions)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun getUserStatsFlow(userId: String): Flow<SessionStats?> = flow {
        try {
            val querySnapshot = sessionsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                emit(null)
                return@flow
            }

            val sessions = querySnapshot.documents
            val totalSessions = sessions.size
            val totalPoints = sessions.sumOf { 
                (it.getLong("totalPoints") ?: 0).toInt() 
            }
            
            // Calculate average accuracy from session data
            val accuracies = sessions.mapNotNull { doc ->
                val correct = (doc.getLong("correctAnswers") ?: 0).toInt()
                val total = (doc.getLong("totalQuestions") ?: 0).toInt()
                if (total > 0) (correct.toDouble() / total) * 100 else null
            }
            val averageAccuracy = if (accuracies.isNotEmpty()) accuracies.average() else 0.0

            emit(SessionStats(totalSessions, totalPoints, averageAccuracy))
        } catch (e: Exception) {
            emit(null)
        }
    }

    fun getTopSessionsFlow(limit: Int = 10): Flow<List<QuizSession>> = flow {
        try {
            val querySnapshot = sessionsCollection
                .orderBy("totalPoints", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val sessions = querySnapshot.documents.mapNotNull { document ->
                try {
                    val timestamp = document.getTimestamp("completedAt")
                    QuizSession(
                        id = document.id,
                        userId = document.getString("userId") ?: "",
                        category = document.getString("category") ?: "",
                        totalQuestions = document.getLong("totalQuestions")?.toInt() ?: 0,
                        correctAnswers = document.getLong("correctAnswers")?.toInt() ?: 0,
                        totalPoints = document.getLong("totalPoints")?.toInt() ?: 0,
                        timeSpentSeconds = document.getLong("timeSpentSeconds")?.toInt() ?: 0,
                        completedAt = timestamp?.toDate() ?: Date()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            emit(sessions)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    private suspend fun updateUserStats(userId: String) {
        try {
            // Busca todas as sessões do usuário
            val userSessions = sessionsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (userSessions.isEmpty) return

            val totalSessions = userSessions.size()
            val totalPoints = userSessions.documents.sumOf { 
                (it.getLong("totalPoints") ?: 0).toInt() 
            }
            
            // Calculate average accuracy
            val accuracies = userSessions.documents.mapNotNull { doc ->
                val correct = (doc.getLong("correctAnswers") ?: 0).toInt()
                val total = (doc.getLong("totalQuestions") ?: 0).toInt()
                if (total > 0) (correct.toDouble() / total) * 100 else null
            }
            val averageAccuracy = if (accuracies.isNotEmpty()) accuracies.average() else 0.0

            // Busca o usuário pelo userId 
            val userQuery = usersCollection.document(userId).get().await()
            if (userQuery.exists()) {
                usersCollection.document(userId).update(
                    mapOf(
                        "totalQuizzes" to totalSessions,
                        "totalPoints" to totalPoints,
                        "averageAccuracy" to averageAccuracy
                    )
                ).await()
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}
