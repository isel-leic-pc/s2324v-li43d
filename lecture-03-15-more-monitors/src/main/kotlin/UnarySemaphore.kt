package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class UnarySemaphore(private var permits : Int = 0) {
    val monitor = ReentrantLock()
    val permitsAvailable = monitor.newCondition()

    fun acquire(timeout : Duration) : Boolean  {
        monitor.withLock {
            // fast path
            if (permits > 0) {
                --permits
                return true
            }

            // wait path
            var timeoutNanos = timeout.inWholeNanoseconds
            do {
                try {
                    timeoutNanos = permitsAvailable.awaitNanos(timeoutNanos)
                    if (permits > 0) {
                        --permits
                        return true
                    }
                    if (timeoutNanos <= 0) return false
                }
                catch (e: InterruptedException) {
                    // since acquirers are signaled
                    // individually we need to regenerate a signal
                    // if there permits, because we could been notified and
                    // interrupted simultaneously,
                    // and there is a risk of losing the notification,
                    // a thing that could never happen
                    if (permits > 0) {
                        permitsAvailable.signal()
                    }
                    throw e
                }
            }
            while(true)
        }
    }

    fun release() {
        monitor.withLock {
            ++permits
            permitsAvailable.signal()
        }
    }
}