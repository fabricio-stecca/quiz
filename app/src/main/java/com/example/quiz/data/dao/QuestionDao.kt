package com.example.quiz.data.dao

import androidx.room.*
import com.example.quiz.data.model.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE category = :category")
    fun getQuestionsByCategory(category: String): Flow<List<Question>>

    @Query("SELECT DISTINCT category FROM questions")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Query("DELETE FROM questions WHERE category = :category")
    suspend fun deleteQuestionsByCategory(category: String)

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()
}
