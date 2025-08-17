package com.example.quiz.data.repository

import android.util.Log
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
    val averageAccuracy: Double,
    val performanceData: List<QuizPerformance> = emptyList()
)

data class QuizPerformance(
    val quizNumber: Int,
    val accuracy: Double,
    val points: Int,
    val category: String,
    val date: String
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
                .get()
                .await()
            /*
            ChatGPT- inicio
            Esse erro esta aparecendo java.lang.ClassCastException: java.lang.Long cannot be cast to java.lang.Integer, oq poderia ser ?
             */
            val sessions = querySnapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null
                    val timestamp = data["completedAt"] as? com.google.firebase.Timestamp
                    val completedAt = timestamp?.toDate() ?: Date()
                    
                    QuizSession(
                        id = document.id,
                        userId = data["userId"] as? String ?: "",
                        category = data["category"] as? String ?: "",
                        totalQuestions = (data["totalQuestions"] as? Long)?.toInt() ?: 0,
                        correctAnswers = (data["correctAnswers"] as? Long)?.toInt() ?: 0,
                        totalPoints = (data["totalPoints"] as? Long)?.toInt() ?: 0,
                        timeSpentSeconds = (data["timeSpentSeconds"] as? Long)?.toInt() ?: 0,
                        completedAt = completedAt
                    )
                } catch (e: Exception) {
                    null
                }
            }
            /*
            ChatGPT - final
             */

            // Ordenar por data decrescente (mais recentes primeiro)
            val sortedSessions = sessions.sortedByDescending { it.completedAt }
            emit(sortedSessions)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun getUserStats(userId: String): Flow<SessionStats?> = flow {
        try {
            Log.d("FirestoreRepository", "Getting stats for userId: $userId")
            
            val userSessions = sessionsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            Log.d("FirestoreRepository", "Found ${userSessions.size()} sessions")

            if (userSessions.isEmpty) {
                Log.d("FirestoreRepository", "No sessions found, emitting null")
                emit(null)
                return@flow
            }

            val totalSessions = userSessions.size()
            val totalPoints = userSessions.documents.sumOf { 
                (it.getLong("totalPoints") ?: 0).toInt() 
            }
            
            Log.d("FirestoreRepository", "totalSessions: $totalSessions, totalPoints: $totalPoints")
            
            // Calculate average accuracy and performance data
            val performanceData = mutableListOf<QuizPerformance>()
            val accuracies = userSessions.documents.mapIndexedNotNull { index, doc ->
                val correct = (doc.getLong("correctAnswers") ?: 0).toInt()
                val total = (doc.getLong("totalQuestions") ?: 0).toInt()
                val points = (doc.getLong("totalPoints") ?: 0).toInt()
                val category = doc.getString("category") ?: "Unknown"
                
                Log.d("FirestoreRepository", "Session $index: correct=$correct, total=$total, points=$points, category=$category")
                
                if (total > 0) {
                    val accuracy = (correct.toDouble() / total) * 100
                    
                    // Add to performance data
                    performanceData.add(
                        QuizPerformance(
                            quizNumber = index + 1,
                            accuracy = accuracy,
                            points = points,
                            category = category,
                            date = "Quiz ${index + 1}"
                        )
                    )
                    
                    accuracy
                } else null
            }
            val averageAccuracy = if (accuracies.isNotEmpty()) accuracies.average() else 0.0

            Log.d("FirestoreRepository", "averageAccuracy: $averageAccuracy, performanceData size: ${performanceData.size}")

            val stats = SessionStats(totalSessions, totalPoints, averageAccuracy, performanceData)
            Log.d("FirestoreRepository", "Emitting stats: $stats")
            emit(stats)
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error getting user stats", e)
            emit(null)
        }
    }

    suspend fun updateUserStats(userId: String) {
        try {
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

    fun getAllSessionsFlow(): Flow<List<QuizSession>> = flow {
        try {
            val querySnapshot = sessionsCollection
                .get()
                .await()

            val sessions = querySnapshot.documents.mapNotNull { document ->
                try {
                    val timestamp = document.getTimestamp("completedAt")
                    val correctAnswers = (document.getLong("correctAnswers") ?: 0).toInt()
                    val totalQuestions = (document.getLong("totalQuestions") ?: 0).toInt()

                    QuizSession(
                        id = document.id,
                        userId = document.getString("userId") ?: "",
                        category = document.getString("category") ?: "",
                        totalQuestions = totalQuestions,
                        correctAnswers = correctAnswers,
                        totalPoints = (document.getLong("totalPoints") ?: 0).toInt(),
                        timeSpentSeconds = (document.getLong("timeSpentSeconds") ?: 0).toInt(),
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
}