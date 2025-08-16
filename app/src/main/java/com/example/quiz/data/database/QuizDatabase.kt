package com.example.quiz.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.quiz.data.dao.QuestionDao
import com.example.quiz.data.dao.QuizSessionDao
import com.example.quiz.data.dao.UserDao
import com.example.quiz.data.model.Question
import com.example.quiz.data.model.QuizSession
import com.example.quiz.data.model.User

@Database(
    entities = [Question::class, QuizSession::class, User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun quizSessionDao(): QuizSessionDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: QuizDatabase? = null

        fun getDatabase(context: Context): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "quiz_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
