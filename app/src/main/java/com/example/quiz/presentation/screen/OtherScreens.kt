package com.example.quiz.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quiz.presentation.viewmodel.HistoryViewModel
import com.example.quiz.presentation.viewmodel.RankingViewModel
import com.example.quiz.presentation.viewmodel.RankingType
import com.example.quiz.data.repository.QuizPerformance
import com.example.quiz.ui.theme.GradientStart
import com.example.quiz.ui.theme.GradientEnd
import com.example.quiz.ui.theme.PrimaryBlue40
import kotlin.math.max
import kotlin.math.min


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    onNavigateBack: () -> Unit,
    viewModel: RankingViewModel = viewModel()
) {
    val userRankings by viewModel.userRankings.collectAsState()
    val selectedRankingType by viewModel.selectedRankingType.collectAsState()

    val sortedUsers = remember(userRankings, selectedRankingType) {
        when (selectedRankingType) {
            RankingType.POINTS -> userRankings.sortedByDescending { it.totalPoints }
            RankingType.QUESTIONS -> userRankings.sortedByDescending { it.totalQuestions }
        }
    }

    LaunchedEffect(Unit) { viewModel.loadRankings() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(listOf(GradientStart, GradientEnd))
            )
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
                Text(
                    text = "Ranking",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }


            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        onClick = { viewModel.selectRankingType(RankingType.POINTS) },
                        label = { Text("Maior Pontuação") },
                        selected = selectedRankingType == RankingType.POINTS,
                        leadingIcon = if (selectedRankingType == RankingType.POINTS) {
                            { Icon(Icons.Default.Star, contentDescription = null) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { viewModel.selectRankingType(RankingType.QUESTIONS) },
                        label = { Text("Mais Perguntas") },
                        selected = selectedRankingType == RankingType.QUESTIONS,
                        leadingIcon = if (selectedRankingType == RankingType.QUESTIONS) {
                            { Icon(Icons.Default.Leaderboard, contentDescription = null) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = "Top Jogadores",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (sortedUsers.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Nenhum dado disponível",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Faça alguns quizzes para aparecer no ranking!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {

                sortedUsers.take(10).forEachIndexed { index, userRanking ->
                    val topColor = when (index) {
                        0 -> PrimaryBlue40.copy(alpha = 0.15f)
                        1 -> PrimaryBlue40.copy(alpha = 0.10f)
                        2 -> PrimaryBlue40.copy(alpha = 0.05f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.0f)
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (index < 3) 8.dp else 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(topColor)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (index) {
                                    0 -> "🥇"
                                    1 -> "🥈"
                                    2 -> "🥉"
                                    else -> "${index + 1}º"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(48.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userRanking.nickname,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${userRanking.totalQuizzes} quizzes • ${userRanking.averageAccuracy.toInt()}% precisão",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when (selectedRankingType) {
                                    RankingType.POINTS -> "${userRanking.totalPoints} pts"
                                    RankingType.QUESTIONS -> "${userRanking.totalQuestions} perg"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Text(
                text = "Atualiza automaticamente a cada sessão concluída.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    historyViewModel: HistoryViewModel = viewModel()
) {
    val stats by historyViewModel.stats.collectAsState()
    val isLoading by historyViewModel.isLoading.collectAsState()

    LaunchedEffect(userId) {
        android.util.Log.d("DashboardScreen", "DashboardScreen started with userId: $userId")
        historyViewModel.loadUserHistory(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            stats?.let { sessionStats ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Card(
                        modifier = Modifier.weight(1f),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${sessionStats.totalSessions}",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total Quizzes",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }


                    Card(
                        modifier = Modifier.weight(1f),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${sessionStats.averageAccuracy.toInt()}%",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Avg Accuracy",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                if (sessionStats.performanceData.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Desempenho por Quiz",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            PerformanceChart(
                                performances = sessionStats.performanceData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }


                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${sessionStats.totalPoints}",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total Points Earned",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } ?: run {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nenhuma estatística disponível",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Complete seu primeiro quiz para ver suas estatísticas aqui!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceChart(
    performances: List<QuizPerformance>,
    modifier: Modifier = Modifier
) {

    val trimmed = remember(performances) {
        if (performances.size > 10) performances.takeLast(10) else performances
    }
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40f
        
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding
        
    if (trimmed.isEmpty()) return@Canvas
        

        val gridColor = Color.Gray.copy(alpha = 0.3f)
        

        for (i in 0..4) {
            val y = padding + (chartHeight * i / 4)
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
            )
        }
        

    val maxPoints = if (trimmed.size > 5) 5 else trimmed.size
        for (i in 0..maxPoints) {
            val x = padding + (chartWidth * i / maxPoints)
            drawLine(
                color = gridColor,
                start = Offset(x, padding),
                end = Offset(x, height - padding),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
            )
        }
        

        if (trimmed.size > 1) {
            val path = Path()
            
            trimmed.forEachIndexed { index, performance ->
                val x = padding + (chartWidth * index / (trimmed.size - 1))
                val y = padding + chartHeight - (chartHeight * (performance.accuracy / 100.0).toFloat())
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
        

        trimmed.forEachIndexed { index, performance ->
            val x = padding + (chartWidth * index / max(1, trimmed.size - 1))
            val y = padding + chartHeight - (chartHeight * (performance.accuracy / 100.0).toFloat())
            

            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
            

            drawCircle(
                color = surfaceColor,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
    

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Mais antigo  ⇢⇢  Mais recente",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
