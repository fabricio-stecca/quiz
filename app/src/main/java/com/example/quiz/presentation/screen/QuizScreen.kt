package com.example.quiz.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quiz.presentation.viewmodel.QuizViewModel

data class QuizResult(
    val correctAnswers: Int,
    val totalQuestions: Int,
    val accuracy: Double,
    val totalPoints: Int,
    val timeSpent: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    category: String,
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val questions = uiState.questions
    val currentQuestionIndex = uiState.currentQuestionIndex
    val selectedAnswers = uiState.selectedAnswers
    val quizResult = uiState.score?.let { score ->
        QuizResult(
            correctAnswers = score.correctAnswers,
            totalQuestions = score.totalQuestions,
            accuracy = score.accuracy,
            totalPoints = score.totalPoints,
            timeSpent = score.timeSpent
        )
    }

    // Load questions when the screen is first composed
    LaunchedEffect(category) {
        viewModel.loadQuestions(category)
    }

    if (questions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (quizResult != null) {
        QuizResultScreen(
            result = quizResult,
            onNavigateBack = onNavigateBack,
            onRetakeQuiz = {
                viewModel.resetQuiz()
                viewModel.loadQuestions(category)
            }
        )
        return
    }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    if (currentQuestion == null) {
        onNavigateBack()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar with Exit Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exit Button
            OutlinedButton(
                onClick = onNavigateBack,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Exit Quiz"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sair")
            }

            // Progress indicator
            Text(
                text = "${currentQuestionIndex + 1} / ${questions.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = category,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Question
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = currentQuestion.questionText,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Answer options
        val selectedAnswersList = selectedAnswers[currentQuestionIndex] ?: emptyList()

        currentQuestion.options.forEachIndexed { index, option ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                onClick = { 
                    viewModel.selectAnswer(currentQuestionIndex, listOf(option))
                },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedAnswersList.contains(option)) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = "${('A' + index)} $option",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Previous button
            if (currentQuestionIndex > 0) {
                OutlinedButton(
                    onClick = { viewModel.goToPreviousQuestion() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Anterior")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Check if all questions are answered
            val allQuestionsAnswered = (0 until questions.size).all { questionIndex ->
                selectedAnswers[questionIndex]?.isNotEmpty() == true
            }

            // Next/Finish button logic
            when {
                // Not on last question - show Next button
                currentQuestionIndex < questions.size - 1 -> {
                    Button(
                        onClick = { viewModel.goToNextQuestion() },
                        enabled = selectedAnswers[currentQuestionIndex]?.isNotEmpty() == true
                    ) {
                        Text("Próxima")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next"
                        )
                    }
                }
                // On last question and all questions answered - show Finish button
                allQuestionsAnswered -> {
                    Button(
                        onClick = { viewModel.completeQuiz(userId, category) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Finalizar Quiz")
                    }
                }
                // On last question but not all answered - show disabled finish button with message
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { },
                            enabled = false
                        ) {
                            Text("Finalizar Quiz")
                        }
                        Text(
                            text = "Responda todas as perguntas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizResultScreen(
    result: QuizResult,
    onNavigateBack: () -> Unit,
    onRetakeQuiz: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quiz Finalizado!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sua Pontuação",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${result.correctAnswers} / ${result.totalQuestions}",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Precisão:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${result.accuracy.toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pontos:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${result.totalPoints}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tempo:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${result.timeSpent}s",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Home"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voltar ao Menu Principal")
            }
            
            OutlinedButton(
                onClick = onRetakeQuiz,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refazer Quiz")
            }
        }
    }
}
