package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class GenericSemaphoreFair(private var permits: Int) {
    val monitor = ReentrantLock()

    @Throws(InterruptedException::class)
    fun acquire(p: Int, timeout: Duration) : Boolean {
        monitor.withLock {
            // fast path
            TODO()

            // wait path
        }

    }

    fun release(p: Int) {
        monitor.withLock {
            TODO()
        }
    }
}