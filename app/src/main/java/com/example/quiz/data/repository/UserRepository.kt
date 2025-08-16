package com.example.quiz.data.repository

import com.example.quiz.data.dao.UserDao
import com.example.quiz.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao
) {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef = firebaseDatabase.getReference("users")

    suspend fun getCurrentUser(): User? {
        val currentUser = firebaseAuth.currentUser ?: return null
        return userDao.getUserById(currentUser.uid) ?: createDefaultUser(currentUser.uid, currentUser.email ?: "")
    }

    private suspend fun createDefaultUser(userId: String, email: String): User {
        val user = User(
            id = userId,
            name = email.substringBefore("@"),
            email = email
        )
        userDao.insertUser(user)
        return user
    }

    fun getTopUsers(limit: Int = 10): Flow<List<User>> = userDao.getTopUsers(limit)

    suspend fun updateUserStats(userId: String, sessionStats: SessionStats) {
        val user = userDao.getUserById(userId) ?: return
        
        val updatedUser = user.copy(
            totalQuizzes = sessionStats.totalSessions,
            totalPoints = sessionStats.totalPoints,
            averageAccuracy = sessionStats.averageAccuracy
        )
        
        userDao.updateUser(updatedUser)
        
        // Sync to Firebase
        try {
            usersRef.child(userId).setValue(updatedUser)
        } catch (e: Exception) {
            // Handle Firebase error silently
        }
    }

    fun signInAnonymously(): String {
        // For now, return a default user ID
        // In a real app, you would implement proper authentication
        return "default_user_${System.currentTimeMillis()}"
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
    }
}
