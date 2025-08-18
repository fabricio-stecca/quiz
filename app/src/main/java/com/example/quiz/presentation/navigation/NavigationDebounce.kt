package com.example.quiz.presentation.navigation

import android.os.SystemClock
import androidx.navigation.NavHostController
import java.util.concurrent.atomic.AtomicLong

object NavigationDebounce {
    private val lastPopTime = AtomicLong(0L)
    private const val DEFAULT_INTERVAL_MS = 500L

    fun popBackStackDebounced(navController: NavHostController, intervalMs: Long = DEFAULT_INTERVAL_MS) {
        val now = SystemClock.elapsedRealtime()
        val previous = lastPopTime.get()
        if (now - previous < intervalMs) {
            return
        }
        if (lastPopTime.compareAndSet(previous, now)) {

            if (navController.previousBackStackEntry != null) {
                navController.popBackStack()
            }
        }
    }
}
