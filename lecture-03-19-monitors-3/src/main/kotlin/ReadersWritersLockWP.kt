import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * A simple implementation of a readers/writers lock
 * using java explicit monitors (ReentrantLock plus Conditions)
 * In this implementation writers have priority, that is, no reader
 * can enter before all writers have gone
 */
class ReadersWritersLockWP {
    // TO REFACTOR!
    val monitor = ReentrantLock()
    val canRead = monitor.newCondition()
    val canWrite = monitor.newCondition()
    var nReaders = 0
    var writing = false
    var waitingWriters = 0

    @Throws(InterruptedException::class)
    fun startRead(timeout: Duration) : Boolean  {
        monitor.withLock {
            // fast path
            if (!writing && waitingWriters == 0) {
                nReaders++
                return true
            }
            // wait path
            var timeoutNanos = timeout.inWholeNanoseconds
            do {
                try {
                    timeoutNanos = canRead.awaitNanos(timeoutNanos)
                    if (!writing) {
                        nReaders++
                        return true
                    }

                    if (timeoutNanos <= 0) {
                        // give up on timeout
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    // since the readers are signaled in broadcast
                    // and we have not changed synchronization state
                    // we could omit this try catch block, since
                    // we have nothing to do here!
                    throw e
                }
            }
            while(true)
        }
    }

    fun endRead() {
        monitor.withLock {
            --nReaders
            if (nReaders == 0) {
                if (waitingWriters > 0) {
                   canWrite.signal()
                }
            }
        }
    }

    @Throws(InterruptedException::class)
    fun startWrite(timeout: Duration) : Boolean  {
        monitor.withLock {
            // fast path
            if (nReaders == 0 && !writing && waitingWriters == 0) {
                writing = true
                return true
            }
            // wait path
            var timeoutNanos = timeout.inWholeNanoseconds
            waitingWriters++
            do {
                try {
                    timeoutNanos = canWrite.awaitNanos(timeoutNanos)
                    if (nReaders == 0 && !writing) {
                        waitingWriters--
                        writing = true
                        return true
                    }
                    if (timeoutNanos <= 0) {
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    // since writers are signaled
                    // individually we need to regenerate a signal
                    // because we could been notified and
                    // interrupted simultaneously,
                    // and there is a risk of losing the notification,
                    // a thing that could never happen
                    canWrite.signal()
                }
            }
            while(true)
        }
    }

    fun endWrite() {
        monitor.withLock {
            writing = false
            // we have to awake readers and writers,
            // since we have no state to know
            // the actual situation!
            // With more information, we could do better
            if (waitingWriters > 0) {
                canWrite.signal()
            }
            else {
                canRead.signalAll()
            }

        }
    }
}