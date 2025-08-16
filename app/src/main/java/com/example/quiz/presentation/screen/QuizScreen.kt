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
    onQuizComplete: () -> Unit,
    viewModel: QuizViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    val questions = uiState.questions
    val currentQuestionIndex = uiState.currentQuestionIndex
    val selectedAnswers = uiState.selectedAnswers
    val isQuizCompleted = uiState.isQuizCompleted
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

    // Handle quiz completion
    LaunchedEffect(isQuizCompleted) {
        if (isQuizCompleted) {
            onQuizComplete()
        }
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
            result = quizResult!!,
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
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "${currentQuestionIndex + 1} / ${questions.size}",
                style = MaterialTheme.typography.titleMedium
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
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Next/Finish button
            if (currentQuestionIndex < questions.size - 1) {
                Button(
                    onClick = { viewModel.goToNextQuestion() },
                    enabled = selectedAnswers[currentQuestionIndex]?.isNotEmpty() == true
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next"
                    )
                }
            } else {
                Button(
                    onClick = { viewModel.completeQuiz(userId, category) },
                    enabled = selectedAnswers.all { it.value.isNotEmpty() }
                ) {
                    Text("Finish Quiz")
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
            text = "Quiz Completed!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Score",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${result.correctAnswers} / ${result.totalQuestions}",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Accuracy:")
                    Text("${result.accuracy.toInt()}%")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Points:")
                    Text("${result.totalPoints}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Time:")
                    Text("${result.timeSpent}s")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onRetakeQuiz,
                modifier = Modifier.weight(1f)
            ) {
                Text("Retake Quiz")
            }

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back to Home")
            }
        }
    }
}
