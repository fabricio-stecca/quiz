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
    val accountCreated: Boolean = false,
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
                errorMessage = "Por favor, preencha todos os campos"
            )
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            

            
            try {

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
                        nickname = document.getString("nickname") ?: "",
                        role = document.getString("role") ?: "user",
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

    fun signUp(name: String, email: String, nickname: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || nickname.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _authState.value = _authState.value.copy(
                errorMessage = "Por favor, preencha todos os campos"
            )
            return
        }

        if (password != confirmPassword) {
            _authState.value = _authState.value.copy(
                errorMessage = "As senhas não coincidem"
            )
            return
        }

        if (password.length < 6) {
            _authState.value = _authState.value.copy(
                errorMessage = "A senha deve ter pelo menos 6 caracteres"
            )
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = _authState.value.copy(
                errorMessage = "Por favor, insira um email válido"
            )
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            
            try {

                val existingEmails = usersCollection
                    .whereEqualTo("email", email)
                    .get()
                    .await()
                
                if (!existingEmails.isEmpty) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Email já está em uso. Faça login."
                    )
                    return@launch
                }


                val existingNicknames = usersCollection
                    .whereEqualTo("nickname", nickname)
                    .get()
                    .await()
                
                if (!existingNicknames.isEmpty) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Apelido já está em uso. Escolha outro."
                    )
                    return@launch
                }


                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "nickname" to nickname,
                    "role" to "user",
                    "totalQuizzes" to 0,
                    "totalPoints" to 0,
                    "averageAccuracy" to 0.0,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                
                val documentRef = usersCollection.add(userData).await()
                
                val newUser = User(
                    id = documentRef.id,
                    name = name,
                    email = email,
                    nickname = nickname
                )
                
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    accountCreated = true,
                    isLoggedIn = true,
                    currentUser = newUser
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao criar conta: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }

    fun clearAccountCreated() {
        _authState.value = _authState.value.copy(accountCreated = false)
    }

    fun logout() {
        _authState.value = AuthState()
    }
}
