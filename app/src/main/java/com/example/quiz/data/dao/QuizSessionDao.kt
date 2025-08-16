package com.example.quiz.data.dao

import androidx.room.*
import com.example.quiz.data.model.QuizSession
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizSessionDao {
    @Query("SELECT * FROM quiz_sessions WHERE userId = :userId ORDER BY completedAt DESC")
    fun getSessionsByUser(userId: String): Flow<List<QuizSession>>

    @Query("SELECT * FROM quiz_sessions WHERE userId = :userId AND category = :category ORDER BY completedAt DESC")
    fun getSessionsByUserAndCategory(userId: String, category: String): Flow<List<QuizSession>>

    @Query("SELECT * FROM quiz_sessions ORDER BY totalPoints DESC LIMIT :limit")
    fun getTopScores(limit: Int = 10): Flow<List<QuizSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: QuizSession)

    @Update
    suspend fun updateSession(session: QuizSession)

    @Delete
    suspend fun deleteSession(session: QuizSession)

    @Query("SELECT COUNT(*) FROM quiz_sessions WHERE userId = :userId")
    suspend fun getSessionCountByUser(userId: String): Int

    @Query("SELECT AVG(correctAnswers * 1.0 / totalQuestions) * 100 FROM quiz_sessions WHERE userId = :userId")
    suspend fun getAverageAccuracyByUser(userId: String): Double?

    @Query("SELECT SUM(totalPoints) FROM quiz_sessions WHERE userId = :userId")
    suspend fun getTotalPointsByUser(userId: String): Int?
}
