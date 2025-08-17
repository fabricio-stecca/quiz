package com.example.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.quiz.data.database.QuizDatabase
import com.example.quiz.data.repository.QuestionRepository
import com.example.quiz.presentation.navigation.QuizNavigation
import com.example.quiz.ui.theme.QuizTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*ChatGPT - inicio
            Quando o app inicia as barras de notificação e do sistema(em baixo)
            continuam aparecendo e atrapalham o layout mesmo usando enableEdgeToEdge(),como remover elas?

        */
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        /*ChatGPT - final
        */

        initializeQuestions()

        
        setContent {
            QuizTheme {
                // Usando a nova navegação com design moderno
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