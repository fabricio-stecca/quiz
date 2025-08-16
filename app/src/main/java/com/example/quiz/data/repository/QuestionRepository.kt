package com.example.quiz.data.repository

import com.example.quiz.data.dao.QuestionDao
import com.example.quiz.data.model.Question
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class QuestionRepository(
    private val questionDao: QuestionDao
) {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val questionsRef = firebaseDatabase.getReference("questions")

    fun getAllQuestions(): Flow<List<Question>> = questionDao.getAllQuestions()

    fun getQuestionsByCategory(category: String): Flow<List<Question>> = 
        questionDao.getQuestionsByCategory(category)

    fun getAllCategories(): Flow<List<String>> = questionDao.getAllCategories()

    suspend fun syncQuestionsFromFirebase(): Result<Unit> {
        return try {
            val questions = fetchQuestionsFromFirebase()
            questionDao.deleteAllQuestions()
            questionDao.insertQuestions(questions)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchQuestionsFromFirebase(): List<Question> = 
        suspendCancellableCoroutine { continuation ->
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val questions = mutableListOf<Question>()
                        for (categorySnapshot in snapshot.children) {
                            val category = categorySnapshot.key ?: continue
                            for (questionSnapshot in categorySnapshot.children) {
                                val questionData = questionSnapshot.value as? Map<String, Any> ?: continue
                                
                                val question = Question(
                                    id = questionSnapshot.key ?: continue,
                                    category = category,
                                    questionText = questionData["questionText"] as? String ?: continue,
                                    options = (questionData["options"] as? List<*>)?.mapNotNull { it as? String } ?: continue,
                                    correctAnswer = (questionData["correctAnswer"] as? Number)?.toInt() ?: continue,
                                    difficulty = questionData["difficulty"] as? String ?: "medium",
                                    points = (questionData["points"] as? Number)?.toInt() ?: 10
                                )
                                questions.add(question)
                            }
                        }
                        continuation.resume(questions)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            }
            
            questionsRef.addListenerForSingleValueEvent(listener)
            
            continuation.invokeOnCancellation {
                questionsRef.removeEventListener(listener)
            }
        }

    suspend fun addSampleQuestions() {
        val sampleQuestions = listOf(
            Question(
                id = "math_1",
                category = "Mathematics",
                questionText = "What is 2 + 2?",
                options = listOf("3", "4", "5", "6"),
                correctAnswer = 1
            ),
            Question(
                id = "math_2",
                category = "Mathematics",
                questionText = "What is 10 - 3?",
                options = listOf("6", "7", "8", "9"),
                correctAnswer = 1
            ),
            Question(
                id = "science_1",
                category = "Science",
                questionText = "What is the chemical symbol for water?",
                options = listOf("H2O", "CO2", "NaCl", "O2"),
                correctAnswer = 0
            ),
            Question(
                id = "science_2",
                category = "Science",
                questionText = "How many planets are in our solar system?",
                options = listOf("7", "8", "9", "10"),
                correctAnswer = 1
            ),
            Question(
                id = "history_1",
                category = "History",
                questionText = "In which year did World War II end?",
                options = listOf("1944", "1945", "1946", "1947"),
                correctAnswer = 1
            )
        )
        questionDao.insertQuestions(sampleQuestions)
    }
}
