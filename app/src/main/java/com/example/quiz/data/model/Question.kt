package com.example.quiz.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey
    val id: String,
    val category: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: Int,
    val difficulty: String = "medium",
    val points: Int = 10
)
