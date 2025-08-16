package com.example.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.quiz.data.database.QuizDatabase
import com.example.quiz.data.repository.QuestionRepository
import com.example.quiz.presentation.navigation.QuizNavigation
import com.example.quiz.ui.theme.QuizTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Inicializar questões no Firestore
        initializeQuestions()
        
        setContent {
            QuizTheme {
                QuizNavigation()
            }
        }
    }
    
    private fun initializeQuestions() {
        lifecycleScope.launch {
            try {
                val database = QuizDatabase.getDatabase(this@MainActivity)
                val questionRepository = QuestionRepository(database.questionDao())
                
                // Adicionar questões ao Firestore (só vai adicionar se não existirem)
                questionRepository.addNewQuizzesToFirestore()
                
                // Sincronizar questões localmente
                questionRepository.syncQuestionsFromFirebase()
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}