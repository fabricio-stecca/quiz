package com.example.quiz.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.example.quiz.ui.theme.GradientStart
import com.example.quiz.ui.theme.GradientEnd
import com.example.quiz.ui.theme.PrimaryBlue40
import com.example.quiz.ui.theme.SecondaryTeal40
import com.example.quiz.ui.theme.AccentOrange40
import kotlinx.coroutines.tasks.await

data class QuizInfo(
    val id: String,
    val category: String,
    val questionsCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onNavigateBack: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("main") }
    var selectedQuiz by remember { mutableStateOf<QuizInfo?>(null) }
    var existingQuizzes by remember { mutableStateOf<List<QuizInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    // Função para recarregar quizzes
    val reloadQuizzes = {
        refreshTrigger++
    }

    // Carregar quizzes existentes
    LaunchedEffect(currentScreen, refreshTrigger) {
        if (currentScreen == "main") {
            isLoading = true
            try {
                val quizzes = mutableListOf<QuizInfo>()
                val categoriesSnapshot = firestore.collection("questions").get().await()
                
                for (categoryDoc in categoriesSnapshot.documents) {
                    val category = categoryDoc.id
                    val questionsSnapshot = categoryDoc.reference
                        .collection("perguntas")
                        .get()
                        .await()
                    
                    quizzes.add(QuizInfo(
                        id = category,
                        category = category.replace("_", " ").split(" ")
                            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                        questionsCount = questionsSnapshot.size()
                    ))
                }
                existingQuizzes = quizzes
            } catch (e: Exception) {
                // Handle error
            }
            isLoading = false
        }
    }

    when (currentScreen) {
        "main" -> MainAdminScreen(
            existingQuizzes = existingQuizzes,
            isLoading = isLoading,
            onNavigateBack = onNavigateBack,
            onCreateNew = { currentScreen = "create" },
            onEditQuiz = { quiz ->
                selectedQuiz = quiz
                currentScreen = "edit"
            },
            onDeleteQuiz = { quiz ->
                scope.launch {
                    try {
                        // Deletar todas as questões da categoria
                        val questionsSnapshot = firestore
                            .collection("questions")
                            .document(quiz.id)
                            .collection("perguntas")
                            .get()
                            .await()
                        
                        for (questionDoc in questionsSnapshot.documents) {
                            questionDoc.reference.delete().await()
                        }
                        
                        // Deletar o documento da categoria
                        firestore.collection("questions")
                            .document(quiz.id)
                            .delete()
                            .await()
                        
                        // Recarregar lista
                        reloadQuizzes()
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            }
        )
        
        "create" -> CreateEditQuizScreen(
            isEditMode = false,
            existingQuiz = null,
            onBack = { currentScreen = "main" },
            onQuizSaved = { 
                reloadQuizzes()
                currentScreen = "main" 
            }
        )
        
        "edit" -> CreateEditQuizScreen(
            isEditMode = true,
            existingQuiz = selectedQuiz,
            onBack = { currentScreen = "main" },
            onQuizSaved = { 
                reloadQuizzes()
                currentScreen = "main" 
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAdminScreen(
    existingQuizzes: List<QuizInfo>,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onCreateNew: () -> Unit,
    onEditQuiz: (QuizInfo) -> Unit,
    onDeleteQuiz: (QuizInfo) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
            // Header moderno com gradiente
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Sair do Admin",
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Painel Admin",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Gerenciar quizzes e questões",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Button(
                        onClick = onCreateNew,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Criar Novo Quiz",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quizzes Existentes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            when {
                isLoading -> item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Carregando quizzes...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                existingQuizzes.isEmpty() -> item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Nenhum quiz encontrado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Crie seu primeiro quiz para começar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                else -> items(existingQuizzes) { quiz ->
                    QuizManagementCard(
                        quiz = quiz,
                        onEdit = { onEditQuiz(quiz) },
                        onDelete = { onDeleteQuiz(quiz) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizManagementCard(
    quiz: QuizInfo,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                                    Text(
                                text = quiz.category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${quiz.questionsCount} questões",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Card(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Deletar",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { 
                Text(
                    text = "Confirmar Exclusão",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text(
                    text = "Tem certeza que deseja deletar o quiz \"${quiz.category}\"? Esta ação não pode ser desfeita.",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Deletar",
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditQuizScreen(
    isEditMode: Boolean,
    existingQuiz: QuizInfo?,
    onBack: () -> Unit,
    onQuizSaved: () -> Unit
) {
    var category by remember { mutableStateOf(existingQuiz?.category ?: "") }
    var questions by remember { mutableStateOf(listOf<QuestionForm>()) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    // Carregar questões existentes se estiver em modo de edição
    LaunchedEffect(existingQuiz) {
        if (isEditMode && existingQuiz != null) {
            isLoading = true
            try {
                val questionsSnapshot = firestore
                    .collection("questions")
                    .document(existingQuiz.id)
                    .collection("perguntas")
                    .get()
                    .await()
                
                val loadedQuestions = questionsSnapshot.documents.map { doc ->
                    val options = doc.get("options") as? List<String> ?: listOf("", "", "", "")
                    QuestionForm(
                        questionText = doc.getString("questionText") ?: "",
                        options = options,
                        correctAnswer = (doc.getLong("correctAnswer") ?: 0).toInt(),
                        difficulty = doc.getString("difficulty") ?: "medium",
                        points = (doc.getLong("points") ?: 10).toInt()
                    )
                }
                
                questions = if (loadedQuestions.isEmpty()) {
                    listOf(QuestionForm()) // Pelo menos uma questão
                } else {
                    loadedQuestions
                }
                
            } catch (e: Exception) {
                errorMessage = "Erro ao carregar quiz: ${e.message}"
                questions = listOf(QuestionForm())
            }
            isLoading = false
        } else {
            // Modo criação - começar com uma questão
            questions = listOf(QuestionForm())
        }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEditMode) "Editar Quiz" else "Criar Novo Quiz",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEditMode) "Atualize perguntas e detalhes" else "Defina nome e adicione perguntas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            }
            item {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Nome do Quiz") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !isEditMode
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Questões (${questions.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            itemsIndexed(questions) { index, question ->
                QuestionFormCard(
                    questionNumber = index + 1,
                    question = question,
                    onQuestionChange = { newQuestion ->
                        questions = questions.toMutableList().also { it[index] = newQuestion }
                    },
                    onRemoveQuestion = if (questions.size > 1) {
                        { questions = questions.filterIndexed { i, _ -> i != index } }
                    } else null
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = { questions = questions + QuestionForm() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Adicionar questão")
                }
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    if (showSuccess) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text(
                                text = if (isEditMode) "✅ Quiz atualizado com sucesso!" else "✅ Quiz criado com sucesso!",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val categoryId = category.lowercase()
                                        .replace(" ", "_")
                                        .replace(Regex("[^a-z0-9_]"), "")
                                    if (isEditMode && existingQuiz != null) {
                                        val existingQuestionsSnapshot = firestore
                                            .collection("questions")
                                            .document(existingQuiz.id)
                                            .collection("perguntas")
                                            .get()
                                            .await()
                                        for (doc in existingQuestionsSnapshot.documents) {
                                            doc.reference.delete().await()
                                        }
                                    }
                                    val categoryDocData = mapOf(
                                        "name" to category,
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                    firestore.collection("questions")
                                        .document(categoryId)
                                        .set(categoryDocData)
                                        .await()
                                    for ((idx, q) in questions.withIndex()) {
                                        val questionData = mapOf(
                                            "category" to category,
                                            "questionText" to q.questionText,
                                            "options" to q.options,
                                            "correctAnswer" to q.correctAnswer,
                                            "difficulty" to q.difficulty,
                                            "points" to q.points
                                        )
                                        firestore.collection("questions")
                                            .document(categoryId)
                                            .collection("perguntas")
                                            .document("questao_${idx + 1}")
                                            .set(questionData)
                                            .await()
                                    }
                                    showSuccess = true
                                    kotlinx.coroutines.delay(2000)
                                    onQuizSaved()
                                } catch (e: Exception) {
                                    errorMessage = "Erro ao salvar quiz: ${e.message}"
                                }
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && category.isNotBlank() && questions.isNotEmpty() && questions.all { it.isValid() },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isEditMode) "Atualizando..." else "Criando...")
                        } else {
                            Text(if (isEditMode) "Atualizar Quiz" else "Criar Quiz")
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

data class QuestionForm(
    val questionText: String = "",
    val options: List<String> = listOf("", "", "", ""),
    val correctAnswer: Int = 0,
    val difficulty: String = "easy",
    val points: Int = 10
) {
    fun isValid(): Boolean {
        return questionText.isNotBlank() && 
               options.all { it.isNotBlank() } &&
               correctAnswer in 0..3
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionFormCard(
    questionNumber: Int,
    question: QuestionForm,
    onQuestionChange: (QuestionForm) -> Unit,
    onRemoveQuestion: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Questão $questionNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                onRemoveQuestion?.let { removeAction ->
                    IconButton(
                        onClick = removeAction,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remover questão",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = question.questionText,
                onValueChange = { onQuestionChange(question.copy(questionText = it)) },
                label = { Text("Pergunta") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Opções:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dificuldade:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    var expanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = when(question.difficulty) {
                                "easy" -> "Fácil"
                                "medium" -> "Médio"
                                "hard" -> "Difícil"
                                else -> "Médio"
                            },
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .width(100.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Fácil (10 pts)") },
                                onClick = {
                                    onQuestionChange(question.copy(difficulty = "easy", points = 10))
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Médio (15 pts)") },
                                onClick = {
                                    onQuestionChange(question.copy(difficulty = "medium", points = 15))
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Difícil (20 pts)") },
                                onClick = {
                                    onQuestionChange(question.copy(difficulty = "hard", points = 20))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            question.options.forEachIndexed { index, option ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = question.correctAnswer == index,
                        onClick = { 
                            onQuestionChange(question.copy(correctAnswer = index))
                        }
                    )
                    OutlinedTextField(
                        value = option,
                        onValueChange = { newOption ->
                            val newOptions = question.options.toMutableList()
                            newOptions[index] = newOption
                            onQuestionChange(question.copy(options = newOptions))
                        },
                        label = { Text("Opção ${index + 1}") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Pontuação: ${question.points} pontos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
