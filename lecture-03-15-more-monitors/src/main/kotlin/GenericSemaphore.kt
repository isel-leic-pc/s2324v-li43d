package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

// a monitor semaphore with n-ary releases and acquires
class GenericSemaphore(private var permits: Int) {
    val monitor = ReentrantLock()
    val permitsAvailable = monitor.newCondition()

    fun acquire(p: Int, timeout: Duration) : Boolean {
        monitor.withLock {
            // fast path
            if (permits >= p) {
                permits -= p
                return true
            }

            // wait path
            var timeoutNanos = timeout.inWholeNanoseconds
            do {
                try {
                    timeoutNanos = permitsAvailable.awaitNanos(timeoutNanos)
                    if (permits >= p) {
                        permits -= p
                        return true
                    }
                    if (timeoutNanos <= 0) return false
                }
                catch (e: InterruptedException) {
                    // since the acquirers are signaled in broadcast
                    // and we have not changed synchronization state
                    // we could omit this try catch block, since
                    // we have nothing to do here!
                    throw e
                }

            }
            while(true)
        }
    }

    fun release(p: Int) {
        monitor.withLock {
            permits += p
            // we must do a broadcast signal
            // since we have no information about the requested permits
            // by the waiting acquirers!
            permitsAvailable.signalAll()
        }
    }
}