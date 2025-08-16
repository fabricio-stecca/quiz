package com.example.quiz.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val nickname: String,
    val totalQuizzes: Int = 0,
    val totalPoints: Int = 0,
    val averageAccuracy: Double = 0.0
)
