package com.example.quiz.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = _authState.value.copy(
                errorMessage = "Please fill in all fields"
            )
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Busca usuário no Firestore por email
                val querySnapshot = usersCollection
                    .whereEqualTo("email", email)
                    .get()
                    .await()
                
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val user = User(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        totalQuizzes = document.getLong("totalQuizzes")?.toInt() ?: 0,
                        totalPoints = document.getLong("totalPoints")?.toInt() ?: 0,
                        averageAccuracy = document.getDouble("averageAccuracy") ?: 0.0
                    )
                    
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = user
                    )
                } else {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "User not found. Please sign up first."
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Login failed: ${e.message}"
                )
            }
        }
    }

    fun signUp(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _authState.value = _authState.value.copy(
                errorMessage = "Please fill in all fields"
            )
            return
        }

        if (password != confirmPassword) {
            _authState.value = _authState.value.copy(
                errorMessage = "Passwords do not match"
            )
            return
        }

        if (password.length < 6) {
            _authState.value = _authState.value.copy(
                errorMessage = "Password must be at least 6 characters long"
            )
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = _authState.value.copy(
                errorMessage = "Please enter a valid email address"
            )
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Verifica se usuário já existe no Firestore
                val existingUsers = usersCollection
                    .whereEqualTo("email", email)
                    .get()
                    .await()
                
                if (!existingUsers.isEmpty) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "User already exists. Please login instead."
                    )
                    return@launch
                }

                // Cria novo usuário no Firestore
                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "totalQuizzes" to 0,
                    "totalPoints" to 0,
                    "averageAccuracy" to 0.0,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                
                val documentRef = usersCollection.add(userData).await()
                
                val newUser = User(
                    id = documentRef.id,
                    name = name,
                    email = email
                )
                
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentUser = newUser
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Sign up failed: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }

    fun logout() {
        _authState.value = AuthState()
    }
}
