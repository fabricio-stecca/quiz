package com.example.quiz.data.repository

import com.example.quiz.data.model.Question
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreQuestionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val questionsCollection = firestore.collection("questions")

    suspend fun getQuestionsByCategory(category: String): List<Question> {
        return try {
            val querySnapshot = questionsCollection
                .whereEqualTo("category", category)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                try {
                    Question(
                        id = document.id,
                        questionText = document.getString("question") ?: "",
                        options = (document.get("options") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        correctAnswer = (document.getLong("correctAnswerIndex") ?: 0).toInt(),
                        category = document.getString("category") ?: "",
                        difficulty = document.getString("difficulty") ?: "medium"
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAllCategoriesFlow(): Flow<List<String>> = flow {
        try {
            val querySnapshot = questionsCollection.get().await()
            val categories = querySnapshot.documents
                .mapNotNull { it.getString("category") }
                .distinct()
            emit(categories)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun addSampleQuestions() {
        try {
            // Verifica se já existem perguntas
            val existingQuestions = questionsCollection.limit(1).get().await()
            if (!existingQuestions.isEmpty) return

            // Adiciona perguntas de exemplo
            val sampleQuestions = listOf(
                mapOf(
                    "question" to "What is the capital of Brazil?",
                    "options" to listOf("São Paulo", "Rio de Janeiro", "Brasília", "Salvador"),
                    "correctAnswerIndex" to 2,
                    "category" to "Geography",
                    "difficulty" to "easy"
                ),
                mapOf(
                    "question" to "Which planet is known as the Red Planet?",
                    "options" to listOf("Venus", "Mars", "Jupiter", "Saturn"),
                    "correctAnswerIndex" to 1,
                    "category" to "Science",
                    "difficulty" to "easy"
                ),
                mapOf(
                    "question" to "Who painted the Mona Lisa?",
                    "options" to listOf("Vincent van Gogh", "Pablo Picasso", "Leonardo da Vinci", "Michelangelo"),
                    "correctAnswerIndex" to 2,
                    "category" to "Art",
                    "difficulty" to "medium"
                ),
                mapOf(
                    "question" to "What is 15 x 8?",
                    "options" to listOf("120", "125", "115", "130"),
                    "correctAnswerIndex" to 0,
                    "category" to "Mathematics",
                    "difficulty" to "easy"
                ),
                mapOf(
                    "question" to "Which programming language is known for its use in Android development?",
                    "options" to listOf("Python", "JavaScript", "Kotlin", "C++"),
                    "correctAnswerIndex" to 2,
                    "category" to "Technology",
                    "difficulty" to "medium"
                ),
                mapOf(
                    "question" to "What is the largest ocean on Earth?",
                    "options" to listOf("Atlantic Ocean", "Indian Ocean", "Arctic Ocean", "Pacific Ocean"),
                    "correctAnswerIndex" to 3,
                    "category" to "Geography",
                    "difficulty" to "easy"
                ),
                mapOf(
                    "question" to "Which element has the chemical symbol 'O'?",
                    "options" to listOf("Gold", "Oxygen", "Silver", "Hydrogen"),
                    "correctAnswerIndex" to 1,
                    "category" to "Science",
                    "difficulty" to "easy"
                ),
                mapOf(
                    "question" to "Who wrote 'Romeo and Juliet'?",
                    "options" to listOf("Charles Dickens", "William Shakespeare", "Jane Austen", "Mark Twain"),
                    "correctAnswerIndex" to 1,
                    "category" to "Literature",
                    "difficulty" to "medium"
                )
            )

            // Adiciona cada pergunta ao Firestore
            sampleQuestions.forEach { questionData ->
                questionsCollection.add(questionData).await()
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }
}
