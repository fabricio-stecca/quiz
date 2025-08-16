package com.example.quiz.data.repository

import com.example.quiz.data.dao.QuestionDao
import com.example.quiz.data.model.Question
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
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
            val questions = fetchQuestionsFromFirestore()
            questionDao.deleteAllQuestions()
            questionDao.insertQuestions(questions)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchQuestionsFromFirestore(): List<Question> {
        return try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val questions = mutableListOf<Question>()
            
            // Buscar questões de "Conhecimentos Gerais"
            val conhecimentosGerais = firestore.collection("questions")
                .document("conhecimentos_gerais")
                .collection("perguntas")
                .get()
                .await()
                
            for (doc in conhecimentosGerais.documents) {
                try {
                    val options = doc.get("options") as? List<String> ?: continue
                    val question = Question(
                        id = doc.id,
                        category = "Conhecimentos Gerais",
                        questionText = doc.getString("questionText") ?: continue,
                        options = options,
                        correctAnswer = (doc.getLong("correctAnswer") ?: 0).toInt(),
                        difficulty = doc.getString("difficulty") ?: "medium",
                        points = (doc.getLong("points") ?: 10).toInt()
                    )
                    questions.add(question)
                } catch (e: Exception) {
                    // Skip malformed questions
                }
            }
            
            // Buscar questões de "Ciências"
            val ciencias = firestore.collection("questions")
                .document("ciencias")
                .collection("perguntas")
                .get()
                .await()
                
            for (doc in ciencias.documents) {
                try {
                    val options = doc.get("options") as? List<String> ?: continue
                    val question = Question(
                        id = doc.id,
                        category = "Ciências",
                        questionText = doc.getString("questionText") ?: continue,
                        options = options,
                        correctAnswer = (doc.getLong("correctAnswer") ?: 0).toInt(),
                        difficulty = doc.getString("difficulty") ?: "medium",
                        points = (doc.getLong("points") ?: 10).toInt()
                    )
                    questions.add(question)
                } catch (e: Exception) {
                    // Skip malformed questions
                }
            }
            
            questions
        } catch (e: Exception) {
            emptyList()
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

    suspend fun addNewQuizzesToFirestore() {
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            
            // Verificar se já existem questões
            val existingQuestions = firestore.collection("questions")
                .document("conhecimentos_gerais")
                .collection("perguntas")
                .limit(1)
                .get()
                .await()
                
            if (!existingQuestions.isEmpty) {
                return // Questões já existem, não adicionar novamente
            }
            
            // Quiz 1: Conhecimentos Gerais
            val quiz1Questions = listOf(
                mapOf(
                    "category" to "Conhecimentos Gerais",
                    "questionText" to "Qual é a capital do Brasil?",
                    "options" to listOf("São Paulo", "Rio de Janeiro", "Brasília", "Salvador"),
                    "correctAnswer" to 2,
                    "difficulty" to "easy",
                    "points" to 10
                ),
                mapOf(
                    "category" to "Conhecimentos Gerais", 
                    "questionText" to "Quantos continentes existem no mundo?",
                    "options" to listOf("5", "6", "7", "8"),
                    "correctAnswer" to 2,
                    "difficulty" to "easy",
                    "points" to 10
                ),
                mapOf(
                    "category" to "Conhecimentos Gerais",
                    "questionText" to "Qual é o maior oceano do mundo?",
                    "options" to listOf("Atlântico", "Índico", "Ártico", "Pacífico"),
                    "correctAnswer" to 3,
                    "difficulty" to "medium",
                    "points" to 15
                ),
                mapOf(
                    "category" to "Conhecimentos Gerais",
                    "questionText" to "Em que ano o homem pisou na Lua pela primeira vez?",
                    "options" to listOf("1967", "1968", "1969", "1970"),
                    "correctAnswer" to 2,
                    "difficulty" to "medium",
                    "points" to 15
                )
            )

            // Quiz 2: Ciências
            val quiz2Questions = listOf(
                mapOf(
                    "category" to "Ciências",
                    "questionText" to "Qual é a fórmula química da água?",
                    "options" to listOf("CO2", "H2O", "O2", "NaCl"),
                    "correctAnswer" to 1,
                    "difficulty" to "easy",
                    "points" to 10
                ),
                mapOf(
                    "category" to "Ciências",
                    "questionText" to "Qual planeta é conhecido como 'Planeta Vermelho'?",
                    "options" to listOf("Vênus", "Júpiter", "Marte", "Saturno"),
                    "correctAnswer" to 2,
                    "difficulty" to "easy", 
                    "points" to 10
                ),
                mapOf(
                    "category" to "Ciências",
                    "questionText" to "Quantos ossos tem o corpo humano adulto?",
                    "options" to listOf("206", "208", "210", "212"),
                    "correctAnswer" to 0,
                    "difficulty" to "medium",
                    "points" to 15
                ),
                mapOf(
                    "category" to "Ciências",
                    "questionText" to "Qual é o elemento químico mais abundante no universo?",
                    "options" to listOf("Oxigênio", "Carbono", "Hidrogênio", "Nitrogênio"),
                    "correctAnswer" to 2,
                    "difficulty" to "medium",
                    "points" to 15
                )
            )

            // Adicionar questões ao Firestore organizadas por categoria
            for ((index, questionData) in quiz1Questions.withIndex()) {
                firestore.collection("questions")
                    .document("conhecimentos_gerais")
                    .collection("perguntas")
                    .document("questao_${index + 1}")
                    .set(questionData)
            }
            
            for ((index, questionData) in quiz2Questions.withIndex()) {
                firestore.collection("questions")
                    .document("ciencias")
                    .collection("perguntas")
                    .document("questao_${index + 1}")
                    .set(questionData)
            }
            
        } catch (e: Exception) {
            // Handle error silently
        }
    }
}
