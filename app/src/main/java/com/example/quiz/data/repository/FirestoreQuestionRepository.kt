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
            // Converter nome da categoria para ID do documento
            val categoryId = category.lowercase()
                .replace(" ", "_")
                .replace(Regex("[^a-z0-9_]"), "")
            
            // Buscar questões da sub-coleção
            val querySnapshot = questionsCollection
                .document(categoryId)
                .collection("perguntas")
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                try {
                    Question(
                        id = document.id,
                        questionText = document.getString("questionText") ?: "",
                        options = (document.get("options") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        correctAnswer = (document.getLong("correctAnswer") ?: 0).toInt(),
                        category = document.getString("category") ?: category,
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
            // Buscar documentos da coleção questions (que são as categorias)
            val querySnapshot = questionsCollection.get().await()
            val categories = querySnapshot.documents
                .mapNotNull { doc ->
                    // Verificar se o documento tem sub-coleção de perguntas
                    val categoryId = doc.id
                    val categoryName = doc.getString("name") ?: categoryId.replace("_", " ")
                        .split(" ")
                        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                    categoryName
                }
                .filter { it.isNotBlank() }
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

            // Adiciona perguntas de exemplo em inglês
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

    suspend fun addQuizBrasil() {
        try {
            // Quiz específico sobre o Brasil - 5 perguntas
            val quizBrasil = listOf(
                mapOf(
                    "question" to "Qual é a capital do Brasil?",
                    "options" to listOf("São Paulo", "Rio de Janeiro", "Brasília", "Salvador"),
                    "correctAnswerIndex" to 2,
                    "category" to "Quiz Brasil",
                    "difficulty" to "easy"
                ),
                mapOf(
                    "question" to "Qual é o maior estado brasileiro em área territorial?",
                    "options" to listOf("Bahia", "Minas Gerais", "Amazonas", "Pará"),
                    "correctAnswerIndex" to 2,
                    "category" to "Quiz Brasil",
                    "difficulty" to "medium"
                ),
                mapOf(
                    "question" to "Em que ano o Brasil foi descoberto pelos portugueses?",
                    "options" to listOf("1498", "1500", "1502", "1505"),
                    "correctAnswerIndex" to 1,
                    "category" to "Quiz Brasil",
                    "difficulty" to "easy"
                ),
                mapOf(
                    "question" to "Qual é a moeda oficial do Brasil?",
                    "options" to listOf("Peso", "Real", "Cruzeiro", "Dólar"),
                    "correctAnswerIndex" to 1,
                    "category" to "Quiz Brasil",
                    "difficulty" to "easy"
                ),
                mapOf(
                    "question" to "Qual dessas cidades NÃO é uma capital de estado brasileiro?",
                    "options" to listOf("Campinas", "Curitiba", "Porto Alegre", "Recife"),
                    "correctAnswerIndex" to 0,
                    "category" to "Quiz Brasil",
                    "difficulty" to "medium"
                )
            )

            // Adiciona cada pergunta do quiz ao Firestore
            quizBrasil.forEach { questionData ->
                questionsCollection.add(questionData).await()
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }
}
