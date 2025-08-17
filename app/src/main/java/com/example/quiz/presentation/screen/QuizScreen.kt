package com.example.quiz.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quiz.presentation.viewmodel.QuizViewModel
import com.example.quiz.ui.theme.*
import kotlinx.coroutines.delay

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
        LoadingScreen()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Área rolável (header + pergunta)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                QuizHeader(
                    category = category,
                    currentQuestion = currentQuestionIndex + 1,
                    totalQuestions = questions.size,
                    onNavigateBack = onNavigateBack
                )

                Spacer(modifier = Modifier.height(24.dp))

                QuestionCard(
                    question = currentQuestion,
                    selectedAnswerIndex = selectedAnswers[currentQuestionIndex]?.firstOrNull()?.let { selectedText ->
                        currentQuestion.options.indexOf(selectedText).takeIf { it >= 0 }
                    },
                    onAnswerSelected = { answerIdx ->
                        val answerText = currentQuestion.options.getOrNull(answerIdx)
                        if (answerText != null) {
                            viewModel.selectAnswer(currentQuestionIndex, listOf(answerText))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navegação (fixada na parte inferior)
            QuizNavigation(
                currentQuestion = currentQuestionIndex + 1,
                totalQuestions = questions.size,
                hasSelectedAnswer = !selectedAnswers[currentQuestionIndex].isNullOrEmpty(),
                onPrevious = { viewModel.goToPreviousQuestion() },
                onNext = { viewModel.goToNextQuestion() },
                onFinish = { viewModel.completeQuiz(userId, category) }
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Carregando perguntas...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun QuizHeader(
    category: String,
    currentQuestion: Int,
    totalQuestions: Int,
    onNavigateBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pergunta $currentQuestion de $totalQuestions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de progresso animada
            val progress by animateFloatAsState(
                targetValue = currentQuestion.toFloat() / totalQuestions.toFloat(),
                animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                label = "progress"
            )
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SuccessGreen,
                trackColor = NeutralGray200,
            )
        }
    }
}

@Composable
private fun QuestionCard(
    question: com.example.quiz.data.model.Question,
    selectedAnswerIndex: Int?,
    onAnswerSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(AccentOrange40, AccentOrange80)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${question.points} pontos",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = AccentOrange40
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.2
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Opções de resposta
            question.options.forEachIndexed { index, option ->
                Spacer(modifier = Modifier.height(12.dp))
                
                AnswerOptionCard(
                    option = option,
                    isSelected = selectedAnswerIndex == index,
                    onClick = { onAnswerSelected(index) }
                )
            }
        }
    }
}

@Composable
private fun AnswerOptionCard(
    option: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Implementação customizada para evitar "retângulo interno" e manter alinhamento perfeito.
    val shape = RoundedCornerShape(16.dp)
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryBlue40.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
        label = "optionBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryBlue40 else Color.Transparent,
        label = "optionBorder"
    )
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(shape) // garante bordas arredondadas pra background + ripple
            .background(bgColor)
            .border(width = 2.dp, color = borderColor, shape = shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PrimaryBlue40,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) PrimaryBlue40 else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuizNavigation(
    currentQuestion: Int,
    totalQuestions: Int,
    hasSelectedAnswer: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Botão Anterior
        if (currentQuestion > 1) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )
                    ),
                    width = 2.dp
                )
            ) {
                Text(
                    text = "Anterior",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Botão Próximo/Finalizar
        if (currentQuestion < totalQuestions) {
            Button(
                onClick = onNext,
                enabled = hasSelectedAnswer,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasSelectedAnswer) SuccessGreen else NeutralGray300,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Próxima",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Button(
                onClick = onFinish,
                enabled = hasSelectedAnswer,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasSelectedAnswer) AccentOrange40 else NeutralGray300,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Finalizar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
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
    val accuracy = result.accuracy
    val statusColor = when {
        accuracy >= 80 -> SuccessGreen
        accuracy >= 60 -> AccentOrange40
        else -> ErrorRed
    }
    
    val statusMessage = when {
        accuracy >= 80 -> "Excelente!"
        accuracy >= 60 -> "Bom trabalho!"
        else -> "Continue tentando!"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(listOf(GradientStart, GradientEnd))
            )
            .padding(16.dp)
    ) {
        // Conteúdo rolável para telas menores
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ícone de resultado
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = statusColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    
                    Text(
                        text = "Quiz Finalizado",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(28.dp))

                    // Estatísticas (cards alinhados e com pesos iguais)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Pontuação",
                            value = "${accuracy.toInt()}%",
                            color = statusColor,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Acertos",
                            value = "${result.correctAnswers}/${result.totalQuestions}",
                            color = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Pontos",
                            value = "${result.totalPoints}",
                            color = AccentOrange40,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    
                    // Botões
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onRetakeQuiz,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue40
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Refazer Quiz",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Voltar ao Menu",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clip(shape)
            .background(color.copy(alpha = 0.10f))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.55f)), shape)
            .padding(vertical = 14.dp, horizontal = 12.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
