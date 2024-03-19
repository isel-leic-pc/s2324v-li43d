package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class ReadersWritersLockFair {
    // TO REFACTOR!
    val monitor = ReentrantLock()
    val canRead = monitor.newCondition()
    val canWrite = monitor.newCondition()
    var nReaders = 0
    var writing = false

    fun startRead(timeout: Duration) : Boolean  {
        monitor.withLock {
           TODO()
        }
    }

    fun endRead() {
        monitor.withLock {
            TODO()
        }
    }

    fun startWrite(timeout: Duration) : Boolean  {
        monitor.withLock {
            TODO()
        }
    }

    fun endWrite() {
        monitor.withLock {
            TODO()
        }
    }
}