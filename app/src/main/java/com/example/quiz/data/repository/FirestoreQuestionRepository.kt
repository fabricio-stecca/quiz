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
            /*
            Gemini - inicio
            Esta dando errado  criar um ID de documento para o Firestore, palavras com acentos estam falhando na criação de um ID, como posso arrumar?
             */
            // Converter nome da categoria para ID do documento
            val categoryId = category.lowercase()
                .replace(" ", "_")
                .replace(Regex("[^a-z0-9_]"), "")
            /*
            Gemini - fim
             */

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

            val querySnapshot = questionsCollection.get().await()
            val categories = querySnapshot.documents
                .mapNotNull { doc ->

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

            val existingQuestions = questionsCollection.limit(1).get().await()
            if (!existingQuestions.isEmpty) return


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


            sampleQuestions.forEach { questionData ->
                questionsCollection.add(questionData).await()
            }
        } catch (e: Exception) {
            // Handle Error
        }
    }

    suspend fun addQuizBrasil() {
        try {

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
            /*
            Gemini - inicio
           Pq nao esta adicionando o quiz no firestore , apenas alguns estão sendo salvos?
           quizBrasil.forEach { questionData ->
                questionsCollection.add(questionData)
            }


             */
            // Adiciona cada pergunta do quiz ao Firestore
            quizBrasil.forEach { questionData ->
                questionsCollection.add(questionData).await()
            }
        } catch (e: Exception) {
            // Handle error silently
        }
        /*
        Gemini- final
         */
    }
}
