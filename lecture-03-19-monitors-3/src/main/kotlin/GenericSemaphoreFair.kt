package pt.isel.pc.monitors

import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * A generic semaphore using Kernel Style technique, in order
 * to give FIFO order to Acquire operations
 */
class GenericSemaphoreFair(private var permits: Int) {
    val monitor = ReentrantLock()
    val canAcquire = monitor.newCondition()

    private class PendingAcquire(val permits : Int, var done:Boolean = false)

    private val pendingAcquires = LinkedList<PendingAcquire>()

    private fun notifyWaiters() {
        var count = 0
        while(pendingAcquires.size > 0 && permits >= pendingAcquires.first().permits) {
            val waiter = pendingAcquires.removeFirst()
            waiter.done = true
            permits -= waiter.permits
            count++
        }
        if(count > 0) {
            canAcquire.signalAll()
        }
    }

    @Throws(InterruptedException::class)
    fun acquire(p: Int, timeout: Duration) : Boolean {
        monitor.withLock {
            // fast path
            if (pendingAcquires.isEmpty() && permits >= p) {
                permits -= p
                return true
            }
            if (timeout == Duration.ZERO) {
                return false
            }
            // wait path
            var timeoutNanos = timeout.inWholeNanoseconds
            val waiter = PendingAcquire(p)
            pendingAcquires.addLast(waiter)
            do {
                try {
                    timeoutNanos = canAcquire.awaitNanos(timeoutNanos)
                    if (waiter.done) return true
                    if (timeoutNanos <= 0) {
                        pendingAcquires.remove(waiter)
                        // if we give up is possible that
                        // other acquirers can proceed.
                        // we must notify them
                        notifyWaiters()
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    if (waiter.done) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    pendingAcquires.remove(waiter)
                    // if we give up is possible that
                    // other acquirers can proceed.
                    // we must notify them
                    notifyWaiters()
                    throw e
                }
            }
            while(true)
        }

    }

    fun release(p: Int) {
        monitor.withLock {
            permits += p
            notifyWaiters()
        }
    }
}