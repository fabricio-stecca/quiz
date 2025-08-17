package com.example.quiz.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quiz.presentation.screen.*
import com.example.quiz.presentation.navigation.NavigationDebounce.popBackStackDebounced

@Composable
fun QuizNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            // Usando a nova tela de login com design moderno
            com.example.quiz.presentation.screen.LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate("signup")
                },
                onLoginSuccess = { user ->
                    if (user.isAdmin()) {
                        navController.navigate("admin") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home/${user.id}") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onNavigateToLogin = { popBackStackDebounced(navController) }
            )
        }

        /*
        ChatGPT- inicio
        Esta dando esse erro como posso resolver ? java.lang.IllegalArgumentException: Navigation destination that matches request
        NavDeepLinkRequest{ uri=android-app://androidx.navigation/quiz/category/userId }
        cannot be found in the navigation graph.
         */
        composable("home/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            
            // Usando a nova tela home com design moderno
            HomeScreen(
                userId = userId,
                onNavigateToQuiz = { category ->
                    navController.navigate("quiz/$category/$userId")
                },
                onNavigateToHistory = {
                    navController.navigate("history/$userId")
                },
                onNavigateToRanking = {
                    navController.navigate("ranking")
                },
                onNavigateToDashboard = {
                    navController.navigate("dashboard/$userId")
                },
                onSignOut = {
                    navController.navigate("login") {
                        popUpTo("home/$userId") { inclusive = true }
                    }
                }
            )
        }
        /*
        ChatGPT - final
         */
        composable("quiz/{category}/{userId}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            
            // Usando a nova tela de quiz com design moderno
            com.example.quiz.presentation.screen.QuizScreen(
                category = category,
                userId = userId,
                onNavigateBack = { popBackStackDebounced(navController) }
            )
        }

        composable("history/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            
            // Usando a nova tela de histórico com design moderno
            com.example.quiz.presentation.screen.HistoryScreen(
                userId = userId,
                onNavigateBack = { popBackStackDebounced(navController) }
            )
        }

        composable("ranking") {
            RankingScreen(
                onNavigateBack = { popBackStackDebounced(navController) }
            )
        }

        composable("dashboard/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            
            DashboardScreen(
                userId = userId,
                onNavigateBack = { popBackStackDebounced(navController) }
            )
        }

        composable("admin") {
            AdminScreen(
                onNavigateBack = {
                    // Como admin não tem tela anterior, fazer logout
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
    }
}
