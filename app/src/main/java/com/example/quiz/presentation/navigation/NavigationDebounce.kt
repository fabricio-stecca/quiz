package com.example.quiz.presentation.navigation

import android.os.SystemClock
import androidx.navigation.NavHostController
import java.util.concurrent.atomic.AtomicLong

/**
 * Provides a simple global debounce for rapid sequential back navigations that could otherwise
 * cause state inconsistencies or crashes when multiple popBackStack() executions race.
 */
object NavigationDebounce {
    private val lastPopTime = AtomicLong(0L)
    private const val DEFAULT_INTERVAL_MS = 500L

    fun popBackStackDebounced(navController: NavHostController, intervalMs: Long = DEFAULT_INTERVAL_MS) {
        val now = SystemClock.elapsedRealtime()
        val previous = lastPopTime.get()
        if (now - previous < intervalMs) {
            return // Ignore rapid repeat
        }
        if (lastPopTime.compareAndSet(previous, now)) {
            // Only pop if there is somewhere to go back to
            if (navController.previousBackStackEntry != null) {
                navController.popBackStack()
            }
        }
    }
}
