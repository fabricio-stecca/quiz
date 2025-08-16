package com.example.quiz.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quiz.presentation.screen.*

@Composable
fun QuizNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
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
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("home/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            
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

        composable("quiz/{category}/{userId}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            
            QuizScreen(
                category = category,
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("history/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            
            HistoryScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("ranking") {
            RankingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("dashboard/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            
            DashboardScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                }
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
