package com.example.quiz

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Script one-off para criar conta admin no Firestore.
 * Uso: chame AdminBootstrap.run(application) em um ponto temporário (ex: dentro de onCreate de MainActivity)
 * Depois de confirmar no console Firestore que o documento existe, remova a chamada e delete este arquivo.
 */
object AdminBootstrap {
    private const val TAG = "AdminBootstrap"
    private const val ADMIN_DOC_ID = "admin_user"
    private const val ADMIN_EMAIL = "cccc@gmail.com"

    fun run(app: Application) {
        FirebaseApp.initializeApp(app)
        val db = FirebaseFirestore.getInstance()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val docRef = db.collection("users").document(ADMIN_DOC_ID)
                val snap = docRef.get().await()
                if (snap.exists()) {
                    Log.i(TAG, "Admin já existe – nada a fazer")
                } else {
                    docRef.set(
                        mapOf(
                            "name" to "Administrador",
                            "email" to ADMIN_EMAIL,
                            "nickname" to "Admin",
                            "role" to "admin",
                            "totalQuizzes" to 0,
                            "totalPoints" to 0,
                            "averageAccuracy" to 0.0
                        )
                    ).await()
                    Log.i(TAG, "Admin criado com sucesso")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Falha criando admin", e)
            }
        }
    }
}
