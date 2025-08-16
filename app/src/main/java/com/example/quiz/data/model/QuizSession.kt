package com.example.quiz.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "quiz_sessions")
data class QuizSession(
    @PrimaryKey
    val id: String,
    val userId: String,
    val category: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val totalPoints: Int,
    val completedAt: Date,
    val timeSpentSeconds: Int
) {
    val accuracy: Double
        get() = if (totalQuestions > 0) (correctAnswers.toDouble() / totalQuestions) * 100 else 0.0
}
