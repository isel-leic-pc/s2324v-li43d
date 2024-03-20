package pt.isel.pc.monitors

import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * This implements (partially)  a ReadersWritersLock with a fair semantic
 * for readers and writers, that is:
 * A reader entering waits if there are an active writer, of course, but
 * also if there are waiting writers
 *
 * When a writer terminates it gives the lock to all existing waiting readers
 * To do this, we need to use the Kernel Style technique.
 */
class ReadersWritersLockFair {

    val monitor = ReentrantLock()
    val canRead = monitor.newCondition()
    val canWrite = monitor.newCondition()

    private class Waiter(var done : Boolean = false)

    private val pendingWriters = LinkedList<Waiter>()

    private val pendingReaders = LinkedList<Waiter>()

    private var writing = false
    private var nReaders = 0

    @Throws(InterruptedException::class)
    fun startRead(timeout: Duration) : Boolean  {
        monitor.withLock {
            // fast path
            if (!writing && pendingWriters.isEmpty()) {
                nReaders++
                return true
            }
            if (timeout == Duration.ZERO) return false
            // wait path
            val waiter = Waiter()
            pendingReaders.addLast(waiter)
            var timeoutNanos = timeout.inWholeNanoseconds
            do {
                try {
                    timeoutNanos = canRead.awaitNanos(timeoutNanos)
                    // we just observe local state to check
                    // if the operation is concluded
                    if (waiter.done) return true

                    if (timeoutNanos <= 0) {
                        //on give-up we must remove our pending node
                        pendingReaders.remove(waiter)
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    if (waiter.done) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    //on give-up we must remove our pending node
                    pendingReaders.remove(waiter)
                    throw e
                }
            }
            while(true)
        }
    }

    fun endRead() {
        monitor.withLock {
            TODO()
        }
    }

    @Throws(InterruptedException::class)
    fun startWrite(timeout: Duration) : Boolean  {
        monitor.withLock {
            // fast path
            if (!writing && pendingWriters.isEmpty() && nReaders == 0) {
                writing = true
                return true
            }
            // Complete the wait path
            TODO()
        }
    }

    fun endWrite() {
        monitor.withLock {
            writing = false
            if (pendingReaders.size > 0) {
                // Note the order of following actions is not
                // relevant, since we execute this possessing the monitor lock
                nReaders = pendingReaders.size
                for( waiter in pendingReaders) {
                    waiter.done = true
                }
                pendingReaders.clear()
                canRead.signalAll()
            }
            else if (pendingWriters.size > 0) {
                val w = pendingWriters.removeFirst()
                w.done = true
                writing = true
                // using kernel style without specific notification
                // we must awake all waiters
                canWrite.signalAll()
            }

        }
    }
}